# Configuration Sidecar Processor on Endpoint Call Transformation tab

The configuration of the pre-processor via Mashery's Endpoint Call Transformation tab includes:
1. Setting `com.airfranceklm.amt.lambda.AFKLMLambdaSidecarProcessor` as the processing adapter;
2. Derive necessary `key:value` set for pre-processor (explained below) and fill it into the edit field
   `Data to make available for pre-processing`. The pre-processing *must* be enabled for the adapter to  perform
    the requested post-processing work.
3. Configure necessary `key: value` set for the post-processor in a similar way as above as fill it into the edit field
   `Data to make available for post-processing`. The post-processing *must* be enabled for the adapter to  perform
   the requested post-processing work.

Determining the necessary configuration parameters depends on what inputs the sidecar needs to receive:
1. You configure key operational parameter, then
2. You configure the necessary data elements the sidecar needs to perform;
3. Configuring maximum body size
4. If applicable to the use case, the the scope is configured;
5. If applicable to the use case, the pre-flight processing is configured;
6. If applicable to the use case, the relay is configured for post-processor.

# Configuring key operational parameters

## Configuring synchronicity
Each sidecar must be designed for the specific *synchronicity* invocation pattern. The synchronicity is configured by 
passing the `synchronicity` parameter. The parameter can be set either to `event`, `request-response` or `non-blocking`.
Passing no synchronicity parameter explicitly implies the `event` value.

Attempt setting a different value will raise an error.

## Enabling fail-safe, if required
By default, the processor expects that a sidecar invocation must be successful as far as the invocation stack is able
to tell it. (For clarity, that the sidecar was invoked and returned data implying a successful completion.) Most
sidecars should observer *sure-fire* error handling policy.

The *fail-safe* error handling policy tell the processor to ignore sidecar invocation faults. The reasons for doing
this are discussed in the [sidecar desing guidelines](./designing-sidecar.md).

To enable fail-safe processing, the `failsafe` option must be set to `true`. 

## Enabling idempotent calls support
Where the design and the nature of operation make idempotent call support advantageous, this needs to be explicitly
specified by supplying the `idempotnet-aware` property and setting it to `true`. Setting this to `true` will tell
processor to cache in runtime memory responses that are indicated as idempotent.

**Mindlessly** enabling idempotent support **can, and will** result in degraded Mashery performance. **Do not enable**
this option unless you are really sure that idempotence is benefitial as [sidecar design guidelines](./designing-sidecar.md)
explain. 

## Configuring stack
The sidecar will be reachable via a certain stack. The implementation provides multiple possibilities, and more
possibilities could be added. The stack configuration follows this convention:
```properties
stack: <stack>
${stack}.<parameter>: specified value
```

The following will give a minimal configuration required for local HTTP stack:
```properties
stack: http
http.uri: http://localhost:8080/
```

For the list of supported stacks, their capabilities and configuration reference, please consult [stacks page](./stacks.md).

## Configuring (fixed) sidecar parameters

The processor will attempt minimal type conversion for the parameters:
- values matching the `/\d+/` regular expression will be converted to integer numbers,
- values matching the `/\d+\.\d+/` regular expression will be converted to floating pointer numbers,
- values matching the `/true|false/` regular expression will be convert to booleans,
- values reading `null` will be converted into null values;
- anything else will be considererd a string.

## Allowing local configuration override

If the deployment requires that local configuration to take precedence over SaaS configuration, then 
`honour-local-configuration` property must be set to `true`. Refer to [local configuration guide](./local_config.md)
document describing steps to override SaaS-stored configuration with on-machine configuration.

# Configuring Necessary Data Elements
By now, we have chosen for our sidecar the correct synchronicity, error handling policy, and invocation stack. The
next phase is to indicate the required data elements the sidecar needs to receive. Since the data elements
of the sidecar may be sensitive, the design to explicitly indicate the required data elements ensures that the
sidecar cannot accidentally receive (and leak) an unintended data element. 

The expansion of the data elements is controlled with `expand-input` value, listing configuration tokens
corresponding to the respective data elements. There are several exceptions to this rule, which are described 
in the sub-sections below. Refer to [sidecar input composition](./sidecar_input_composition.md) describing how these
data elements appear in the input data structure. 

| Data Element | Configuration token | Pre-flight | Pre-Processor | Post-Processor |
| -----------  | --------------------| -----------|  -----------   | ------------ |
| Fixed parameters | *Not required* | Yes | Yes | Yes |
| Remote IP address | `remoteAddress` | Discouraged | Yes | Yes |
| HTTP Verb | `verb` | Yes | Yes | Yes |
| Operation | `operation` | No | Yes | Yes | 
| Headers, API client -> Mashery | *See configuration below* | Yes | Yes | Yes |
| Headers,  Mashery -> API origin | *See configuration below* | Yes | Yes | *Via relay* |
| Headers,  API origin -> Mashery | *See configuration below* | No | No | Yes + *via relay* |
| Package key EAVs | *See configuration below* | Yes | Yes | Yes |
| Application EAVs | *See configuration below* | Yes | Yes | Yes |
| Token grant type | grantType | Yes | Yes | Yes |
| Token scope | `tokenScope` | Yes | Yes | Yes |
| User token (a.k.a. user context) | `token` | No | Yes | Yes |
| Full token (including bearer token) | `+token` | No | Yes | Yes |
| Mashery method | `method` | No | Yes; not implemented. | Yes; not implemented. |
| Routing | `routing` | Yes | Yes | *Via relay* |
| Request payload, API Client -> Mashery | `requestPayload` | No | Yes | Yes |
| Request payload, Mashery -> API Origin |  | No | No  | No |
| Response payload, API Origin -> Mashery | `responsePayload` | No | No  | Yes |


Notes:
1. Operation, Request Payload and Response Payload  is not supported for pre-flight checks as it is inherently 
highly volatile operation that a (misbehaving) consumer can exploit.
2. For the same reasons, it is recommended using require/include headers in the idempotent calls rather that
  using skip-header.
3. Remote address is discouraged for the idempotent calls as it is unlikely to have a useful semantic.
4. User token and full token are not supported for pre-flight checks, as these would make them indistinguishable
   from the regular calls.
   
## Configuring Request Headers
Since an API client can send any arbitrary selection of headers, the sidecar needs to choose which headers it
needs. 
### Require / Include pair
Most sidecar information needs will run down to a well-defined list of headers that the need to be included in the input
to the sidecar. There are two directives that influence this: `require-request-headers` and `include-request-headers`.
Both supply a list of headers that need to be included. The difference is that for all headers listed in
`require-request-headers`, the `400 Bad Request`-type error will be returned to the client if the mentioned
headers are not present in the request.

The following gives the configuration example:
```properties
require-request-headers:authorization
include-request-headers:x-afklm-market|x-aflkm-salespoint
``` 
So the client *must* specify the `authorization` header. `x-afklm-market` and `x-aflkm-salespoint` will be included
if the client has specified them, however they are optional.

Note: Mashery configuration may drop header from the **Mashery -> API Origin** request. At the pre-processor stage,
both **API Client -> Mashery** and **Mashery -> API Origin** data structures are checked to extract the listed
headers. The rationale here is that the deloyer may choose to:
- use Mashery to remove authorization automatically, while
- the sidecar is attached to process exactly this authorization information. 

### Skip request headers
The `skip-request-headers` option lists the headers in the **Mashery -> API Origin** request that the sidecar
does **not** need to receive.
 
This example:
```properties
skip-request-headers:autorozation|x-afklm-market
```
indicates that out of all headers that Mashery will use to request the API origin, `authorization` and 
`x-afklm-market` should not be communicated to the sidecar.

### All headers
It is also possible for the sidecar to receive all headers in the **Mashery -> API Origin** request by
providing `+requestHeaders` option to `expand-input` parameters.

## Configuring Response Headers
### Include response headers
The most common case is to provide the list of response headers that the sidecar needs, in the 
`include-response-headers` parameter. In this example:
```properties
include-response-headers: x-afklm-market|x-afklm-querycost
```
only two headers, `x-afklm-market` and `x-afklm-querycost` will be sent to the sidecar.

### Skip response headers
Similarly to skipping request headers, it is possible to provide exclusion list for response headers in the
`skip-response-headers` parameter.

### Including API client request headers
At the request stage, it is possible to include **API Client -> Mashery** headers. These are included using either
`include-request-headers` or `skip-request-headers` configuration properties, similarly to the request
headers above.

Note: at the post-processor stage, the `require-request-headers` setting will be ignored as it is essentially
pointless at the post-processor stage.

## Configuring EAVs
The list of the EAVs supported by the Mashery area is fixed at any point in time. If a sidecar needs an EAV
from a package key or from an associated application, the information needs to be specified in a fashion
that is similar to how headers are configured. These properties follow pattern:
`(require|include)-(packageKey|application)-eavs`. 

The logic is that if:
- the package key EAV or an application EAV is required, but it is not set to a non-empty value, then the
  client should recieve `400 Bad Request` error, meaning that he attempts accessing the API before the
  necessary configuration is completed;
- otherwise the respective EAV is supplied, if set to a non-empty value.

The following gives an example of configuration:
```properties
require-packageKey-eav: Public_Key
include-applicatoin-eav: Consumer_Id
```
In this example, it is required to have `Public_Key` EAV for the package key set to a value, while the
`Consumer_Id` of the corresponding application may be blank.     

# Configuring maximum payload size
If the sidecar requires request or response payload, then maximum payload may need to be adjusted to match the pattern
of the API traffic. It is advisable to set the limit to the reasonably expected sizes to prevent submittion of 
"payload bombs".

**Payload size constraint does not apply to API calls payload if either request or response 
 payload is not required by the sidecar**.  

The maximum payload size that could be processed is configured at the endpoint level
using two properties, `max-payload-size` and `max-payload-condition`, which determines the maximum payload size that 
can be sent to the sidecar.


Specifying `max-payload-condition` allows changing the default value of the default condition filtering from
default `blocking` to `filtering`.

The `max-payload-size` is formatted as number, extent, and optionally condition, as e.g. `60kb,blocking`. The extent
can be either `mb`, or `kb`, or `b` meaning mega-bytes, kilo-bytes, or bytes respectively.
```properties
max-payload-size:<size>[k|m]b,([blocking|filtering])
``` 

The maximum size of payload is 1 megabyte. Attempt setting higher payload size will raise an error.

### Example 1: Setting custom blocking limit
To set a custom blocking limit of 55 kilo-bytes, either of two expressions are possible:

```properties
max-payload-size: 55kb,blocking
```

```properties
max-payload-size: 55kb
max-payload-condition: blocking
```

### Example 1: Setting custom filtering limit
To set a custom filtering limit of 55 kilo-bytes, either of two expressions are possible:

```properties
max-payload-size: 55kb,filtering
```

```properties
max-payload-size: 55kb
max-payload-condition: filtering
```

### Example 2: Setting default limit to be blocking
```properties
max-payload-condition: filtering
```

# Configuring Sidecar Scope
To optimize the API response time, it might be necessary to limit the requests that the sidecar should process.
This is referred to as "sidecar Scope". 

The following request data elements can be considered for inclusion in the scope:
1. The API resource path;
2. The HTTP verb;
3. An HTTP header (of a request to API provider or as returned by the API response);
4. An API response code;
5. Package key;
6. OAuth Scope;
7. OAuth User Context;
8. Application EAV;
9. Package key EAV.

The selection logic can be expressed as an *inclusive* condition (i.e. the Lambda shall be called if the data element 
of the 
request matches the defined condition) as well as an *exclusive* condition (i.e. the Lambda shall be called if the data 
element of the request
*does not* match the defined filter condition).

## Sidecar scope design limitations
The design purpose of the sidecar scope mechanism, as well as idempotent calls handing, is to optimize the 
use of the API gateway resources (memory, network, cache storage, etc). This optimization is achieved by avoiding
calling the sidecar where it will do nothing and yield no instructions to modify the request. However, this 
mechanism **does not** pursue **removing** such calls completely.

A sidecar **must** be written in such a way that it functions correctly if:
- no scope filtering is implemented, and
- the API Gateway is not considering idempotency indication. 

## Sidecar scope configuration

The configuration parameter are declared as `<filtering>`-`<datum>`-`label` combination:
- the `<filering>` element can be either `filter` or `filterout`, indicating the inclusion or exclusion scope 
  respectively;
- the `<datum>` element indicate the datum to consider. Some datums, like headers, and EAVs, require being specific
  about which exactly element is required. Such expression will be then written as `<datum>(<parameter>)`;
- the `label` element is optional, it allows splitting multiple options into simpler, less error prone
  better readable and maintainable sequence. 

Let's consider the example where a sidecar has to apply on the user context with a rather complex criteria.
```properties
filter-userContext-regular=.*"user-tier":"regular".*|.*"level-of-assurance":"(silver|gold|partner)".*
```

Using `label` allows achieving the same by splitting this expression into logical parts:
```properties
filter-userContext-regular  =.*"user-tier":"regular".*
filter-userContext-advanced =.*"level-of-assurance":"(silver|gold|partner)".*
```


The `label` field can contain dot-suffix
> The example below is technically inefficient. It is intentionally chosen to demonstrate the or the
> dot-suffixes in the processing of the request.

The following configuration 
```properties
filter-userContext-advanced.v1 =.*"level-of-assurance":"silver".*
filter-userContext-advanced.v2 =.*"level-of-assurance":"gold".*
filter-userContext-advanced.v3 =.*"level-of-assurance":"partner".*
```

will be interpreted by the processor as the following pseudo-code illustrates:
```javascript
if (userContext.matches(/.*"level-of-assurance":"silver".*/) ||
    userContext.matches(/.*"level-of-assurance":"gold".*/) ||
    userContext.matches(/.*"level-of-assurance":"partner".*/)) {
    
    recordMatchingLabel("advanced");
}
```
 
### Supported filter datums 

The following tables gives the overview of the data elements supported for the filtering

|Datum |Configuration name |Parameterized elements supported?|Configuration value format|
|-------------|-----------|---------------|------------|
| API resource path | `resoucePath` | No | Path expression: tokens `{elemName}` matches any alpha-numberic token, the asterisk `*` will match any path|
| HTTP verb | `httpVerb` | No | List of HTTP verbs, case-insensitive |
| HTTP header | `requestHeader` | Yes, parameter is the name of the header in request.| Regular expression pattern |
| HTTP header | `responseHeader` | Yes, parameter is the name of the header in response. Meaningful only in Post-processor configuration. | Regular expression pattern |
| API response code | `responseCode` | No | Comma-separated list of numbers |
| Package key | `packageKey` | Yes | Comma-separated list of package keys |
| OAuth Scope | `scope` | No | Regular expression pattern |
| OAuth user context | `userContext` | No | Regular expression pattern |
| Application EAV | `eav` | Yes, parameter is the name of the EAV | Regular expression pattern |
| Package key EAV | `packageKeyEAV` | Yes, parameter is the name of the EAV | Regular expression pattern |

Groups `resourcePath`, `header`, `eav`, and `packageKeyEAV` can support different inclusion and exclusion condition for
individual labels. For example:
```properties
filter-header(content-type)=application/json.*
filterout-header(afklm-market)=US|BR|DE
```

### Include-Exclude combinations with `resoucePath`
It is possible to apply include and exculde filters for `resourcePath`, however a care should be taken to ensure that the combination
will match the requests. If set incorrectly, it might lead either to unexpected high traffic towards the sidecar,
or it might result in function being inapplicable.

Here's the example of the configuration which will always *exclude* the function from applying:
```properties
filter-resourcePath=secure/*
filterout-resourcePath=public/*
```
Although being technically correct, this configuration will always be `false`: paths selected by the first line will be
excluded by the second line, and vice versa.

An example where similar configuratoin might make sense is:
```properties
filter-resourcePath=*/order/*
filterout-resourcePath=public/*
```
This configuration will scope to all operations path that contain `order` anywhere in it's operation path and the
operation doesn't begin with `public/` prefix.

### Scoping sidecars to several paths
sidecars can be scoped to be applied on several resources of the API.
The configuration the can define the names for these paths as well as the labels:
```properties
filter-resourcePath-op1=path/to/op1/*
filter-resourcePath-op2=jump/across/op2/*
```

The intention of such configuration is that the sidecar applies when the request path matches *either* of 
these paths.

This condition can also be specified in an exclusive fashion, e.g. where the function has to apply *unless*
the reousce path will match either of these paths.
```properties
filterout-resourcePath-public=public/*
filterout-resourcePath-content=content/*
```

# Pre-flight call configuration
TODO

# Relay configuration
TODO


# Configuration parameters reference table
Lambda processor supports the following parameters.

| Parameter | Applicability | Description | 
| --------- | ------------- | ------------|
| `synchronicity` | Pre and Post | Specifies the synchronicity of the request: `request-response`, `event`, or `non-blocking` | 
| `require-request-headers` | Pre | Specifies the list of headers that must be present in the client's request |
| `include-request-headers` | Pre and Post | Specifies API origin request headers to be included in the sidecar input. |
| `skip-request-headers` | Pre and Post | specifies API origin request headers to be skipped in the sidecar input. This option  is mutually exclusive with `include-request-headers`; |
| `include-response-headers` | Post | specifies the API client response headers to be included in the sidecar input. |
| `skip-response-headers` | Post | specifies the API client response headers to be skipped in the sidecar input. It is mutually exclusive with `include-response-headers` |
|  `require-eavs` | Pre and Post | Specifies the mandatory application EAVs that must be set to a non-empty value before    the request can be handled. These values are also included in the sidecar input |
| `include-eavs` | Pre and Post | Specifies the application EAVs to be included in the sidecar input, if they are present |
| `require-packageKey-eavs` | Pre and Post | specifies the mandatory package key EAVs that must be set to a non-empty value before    the request can be handled. These values are also included in the sidecar input |
| `include-packageKey-eavs` | Pre and Post | specifies the package key EAVs to be included in the labmda function input, if they are present; |
| `stack` | Pre and Post | Specifies the  invocation stack to use. |
| `expand-input` | Pre and Post | specifies data elements that are required to be specified in the sidecar input |
|  `failsafe` | Pre and Post | specifies whether or not it is safe to proceed with the handling of the request or response if a failure   would occur during invoking the sidecar |
| `max-payload-size` | Pre and Post |  specifies the maximum payload size |
| `max-payload-condition` | Pre and Post | specifies the payload condition, either `filtering` or `blocking` |
| `${stack}.<parameter>` | Pre and Post | specifies the configuration parameter for the sidecar stack. The `${stack}` token should be  identical to the value set if `stack` parameter |
|  `sidecar-param-<parameter>` | Pre and Post | specifies the parameter to be passed to the sidecar|
| `<filtering>`-`<datum>`-`label` | Pre and Post |  Specifies  scope condition (explained in section sidecar Scope Configuration above) |
| `idempotent-aware` | Pre and Post | specifies whether sidecar invocation should be honour the idempotent responses. The field can    be set to `true` or `false`. It should be enabled where processing idempotent requests is a part of the sidecar    design, and storing idempotent response will get a desired effect on the traffic. |
| `enable-preflight` | Pre |  whether the processor should be doing pre-flight checks. |
| `preflight-require-headers` | Pre | Headers that client must send and need to be included in the pre-flight request. |
| `preflight-include-headers` | Pre | Headers to be included in the pre-flight request. |
| `preflight-require-eavs` | Pre | EAVs to be required in the pre-flight call |
| `preflight-include-eavs` | Pre | EAVs to be included in the pre-flight call |
| `preflight-require-packageKey-eavs`| Pre | EAVs of the package key to be included in the pre-flight call. |   
| `preflight-include-packageKey-eavs`| Pre | EAVs of the package key to be included in the pre-flight call. |   
| `preflight-params` | Pre | optional parameters to be passed to the pre-flight call |   
| `expand-preflight` | Pre |  whether the token information needs to be included in the pre-flight call. Defaults to `false`. If a pre-flight check requires a user token information, this parameter needs to be set to `true`. |
| `honour-local-configuration` | Pre and Post | whether Mashery Local instance should honor the specific local configuration. |      
| `relay-routing` |  Post | Whether to relay routing information. |      
| `relay-request-headers` |  Post | List of request headers sent  **Mashery ->  API Origin ** that needs to be relayed to the sidecar. |      
| `relay-response-headers` |  Post | List of request headers sent  **API Origin -> Mashery ** that needs to be relayed to the sidecar. |      
| `relay-preprocessor` |  Post | Enables receiving the relayed output of the pre-processor. |      
   