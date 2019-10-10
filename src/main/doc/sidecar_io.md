# Sidecar Input

Functionally, the sidecars can receive information about:
- Parameters the developer specified;
- Headers (included by default, but could be filtered or suppressed),
- Remote address of the API client
- Package key;
- EAVs of the package key or of the application
- Token information
- Called resource
- Payload or their excerpts
- Route to the API origin host selected by Mashery
- Relay information (for post-processor only)

A sidecar input is a complex and large (JSON) object that accommodates all those data elements. The section below lists the 
complete structure a sidecar input can have.

The implementation strategy at Air France/KLM expects that a sidecar at hand will need only fraction of these data elements. For
example, so sidecars may require only specific excerpts form the JSON objects being passed through. For others,
the entire payload will be required.

**As a developer of a sidecar, you are encouraged to select only the elements that you actually need.** The extraction
of elements takes processing time and incurs additional network latency. Additionally, elements may contain personal 
data, so such setups should be either adequately protected or unnecessary data be removed.

### Technical limitations about expanding post-processor inputs

Due to the limitation of Mashery API, it is not possible to extract the data that was sent to the API origin from the
Mashery post-processing event. Only the *original* client request will be available. The original request will 
frequently contain elements, such as access tokens, that are not desired to be sent to the sidecar.

The processor provides a possibility to extract *excerpts* that will select small pieces of the payload.

It is the responsibility of the deployer to ensure that all sensitive elements are *dropped* in the event the 
sidecar will require the combination of the request and response data.

# Full Sidecar Input
A fully blown Sidecar object is present in the example below.
```json
{
  "masheryMessageId" : "mashery-message-uuid",
  "point" : "PreProcessor",
  "synchronicity" : "RequestResponse",
  "packageKey" : "PackageKey",
  "serviceId" : "serviceId",
  "endpointId" : "endpointId",
  "params" : {
    "StringParam" : "StringValue",
    "NumberParam" : 42,
    "BooleanParam" : true,
    "Object" : {
      "A" : "lorem",
      "B" : "ypsum"
    }
  },
  "request" : {
    "headers" : {
      "content-type" : "application/json",
      "x-markets" : "NL|FR"
    },
    "payloadExcerpts" : {
      "Exp-A" : "Extracted portion"
    },
    "payloadLength" : 5674,
    "payload" : "This is a short version of string having 5674 bytes",
    "payloadBase64Encoded" : false
  },
  "response" : {
    "headers" : {
      "x-operational-costs" : "B835650-1",
      "content-type" : "application/json"
    },
    "payloadExcerpts" : {
      "Exp-R" : "Extracted portion from response"
    },
    "payloadLength" : 43964,
    "payload" : "This is a short version of response having 43964 bytes",
    "payloadBase64Encoded" : false,
    "responseCode" : 201
  },
  "eavs" : {
    "Eav_A" : "Value_A",
    "Eav_B" : "Value_B",
    "Eav_C" : "Value_C"
  },
  "packageKeyEAVs" : {
    "Eav_D" : "Value_D",
    "Eav_E" : "Value_E",
    "Eav_F" : "Value_F"
  },
  "operation" : {
    "httpVerb" : "GET",
    "path" : "path/to/op",
    "query" : {
      "m" : "b737-800",
      "depT" : "LFPG",
      "dest" : "EHAM"
    },
    "uri" : "https://api-unittest.airfranceklm.com/travel/unittest/path/to/op?m=b737-800&dept=LFPG&dest=EHAM"
  },
  "token" : {
    "bearerToken" : "adfalkdsfjakjfdajlkjrer",
    "scope" : "Role1 Role2 Role3",
    "userContext" : "User Context Value",
    "expires" : 1570537787078,
    "grantType" : "password"
  },
  "routing" : {
    "httpVerb" : "GET",
    "uri" : "http://klm-backend.departure.klm.cpom/backend-route/path/to/op?m=b737-800&dept=LFPG&dest=EHAM"
  },
  "remoteAddress" : "192.168.145.32"
}
```

# Sidecar Input JSON Composition

## Basic Inputs to the sidecar
Sidecars receives always the following minimum input:
```json
{
  "synchronicity": "RequestResponse",
  "point": "PreProcessor",
  "packageKey": "aPackageKey",
  "serviceId": "kdjfldjfsc",
  "endpointId": "ajdlfkjda",
  "params": {
      "parameter_x": "value_x"
   }
}
```

The parameters shown in the example above are:
- `synchronicity`: indicates functional invocation mode, either `RequestResponse` or `Event`:
    - if `RequestResponse` is specified, the Lambda processor is expecting a response that will describe what should 
      be done with the request data at hand;
    - if `Event` is specified, the Lambda processor is *not* expecting any response, i.e. the data was passed to the
      function for asynchronous processing. The return value from the function is not required.
- `point`: `PreProcessor` or `PostProcessor`, or `Preflight`. Indicates the point at which the sidecar is invoked. In case
   the synchronicity is specified as `RequestResponse`, this field indicates where the output of the sidecar
   will be used:
   - in case of `PreProcessor`, the response will modify the request that will be sent to the API provider;
   - in case of `PostProcessor`, the response will modify the response that will be returned to the API client.
   - in case of `Preflight`, the response will apply to all calls of this client. The response **should** also be 
     marked as idempotent.
- `packageKey`: the package key representing the application that Mashery has authenticated;
- `serviceId`: Mashery-internal identifier of the Mashery API Service.
- `endpointId`: Mashery-internal identifier of the endpoint within the Mashery API Service identified by `serviceId`;
- `params`: parameters to this sidecar invocation. The element will appear under these conditions:
    - the endpoint specifically mentions the parameters in the endpoint configuration;
    - the sidecar invocation is scoped filtered; the parameters capture the values of why the invocation is filteted;
    - the *post-processor* sidecar receives relay parameters from a pre-processor sidecar or an API back-end.
    The latter two are further explained in the respective sections below.
    
    The interpretation and the use of these parameters is up to the sidecar logic.
   

   
## Pre-Flight Inputs

TODO: This needs a revision as the expansion needs to be explicitly specified.

The pre-flight inputs will is equivalent of expanding the basic input with:
- token information (if specified), 
- Application EAVs (if listed in the `preflight-eavs`), and
- Package key EAVs (if listed in `preflight-packageKey-eavs`).

By default, only `authorization` header is expanded. If an endpoint implements a scheme that requires further headers,
these are added according to `preflight-headers` configuration.

The complete input may look like the example below:
```json
{
  "synchronicity": "RequestResponse",
  "point": "Preflight",
  "packageKey": "aPackageKey",
  "serviceId": "kdjfldjfsc",
  "endpointId": "ajdlfkjda",
  "params": {
      "preflight_param_x": "value_x"
   },
  "request": {
    "headers": {
      "authorization": "super_custom_authorization_header"
    } 
  },
  "token": {
    "scope": "a-scope",
    "userContext": "aUserContext",
    "expires": "2020-01-01T13:39:45Z",
    "grantType": "AC"
  }
}
```
In some configurations, the deployer may choose to drop the `Authorization` header (e.g. containing the bearer token) 
from being forwarded to the API origin. For bearer tokens, the `request.headers.authorizatoin` element will not be
provided. The pre-flight checks should rely on the information supplied in the `token` field.

For schemes that do not supply bearer information, the `token` field will be absent, so only `request.headers.authorizatoin`
 element will be provided by default. 

## Scope Filters Match Results

Getting the results of the scope filtering offers a great way to communicate parameters to the function without 
requiring additional configuration
directives for the explicit inclusion of the respective elements. 

If the pre- or post-processor defines the scope filters, then the values picked up by these filters is included into
the `params` data structure under keys following the pattern dependant on the value type: 
```json
{
  "params": {
    "<StringElement>": "StringValue",
    "<IntElement>": 200,
    "<MultiValueElement>": {
      "<ElementName_1>": "Value",
      "<ElementName_2>": "Value"
    }
  }
}
```
The name that will be assigned to the element will identical to the group specified in the configuration. In case
the data element was matched on an inclusive that also defines a label, an additional element formatted 
as `${groupName}Label` will be added.

| Filter Group | Labelled field name | Type and value notes| 
|-------|------|-----| 
| `resourcePath` | `resourcePathLabel` | String |
| `httpVerb` | `httpVerbLabel` | String, the HTTP verb, lowercase. | 
| `requestHeader` | Applicable only to individual headers | Key-value map, with the header name being lowercase. | 
| `responseHeader` | Applicable only to individual headers | Key-value map, with the header name being lowercase. | 
| `responseCode` | `responseCodeLabel` | Number |
| `packageKey` | `packageKeyLabel` | String |
| `scope` | `scopeLabel` | String |
| `userContext` | `userContextLabel` | String |
| `eav` | Applicable only to individual EAVs | String |
| `packageKeyEAV` | Applicable only to individual EAVs | String |

To illustrate, in case where a function scope:
- include only `application/json` or `application/json+hal` content type, and
- exclude `AFKLM-Market` header equal to `US`, 'GR', and `BR`, then this input parameter will look like:
```json
{
  "params": {
    "requestHeader": {
      "content-type": "application/json+hal",
      "afklmt-market": "FR"
    }
  }
}
```

The following gives the example of the labelled matching statements. Suppose, the configuration defines:
```properties
filter-userContext-pax: .*role:pax.*
filter-userContext-bax: .*role:bax.*
expand-input: -headers
```
If a request is associated with an access token that has a user scope matching the `-pax` line, then the following
parameter will be submitted:
```json
{
  "params": {
    "userContext": "a user context....",
    "userContextLabel": "pax"
  }
}
```

### Should sidecar use the supplied filter element labels?
The decision for whether to use the labelled fields or not in the sidecar logic is down to the problem the 
respective function needs to solve and the details of the chosen implementation.

In the complete stack, there are three elements:
- Mashery endpoint configuration (which can be *infrequently* changed), and
- Processor code (which we can consider unchangeable on a short notice),
- sidecar code (where the frequency of change is controlled by the labmda function's author)
that work together to produce the intended traffic handling.

In some situations it might be actually *beneficial* to leverage the Mashery endpoint configuration. A good example is
specifying a "short-list" of package keys requiring a special treatment by the sidecar, where the label
will be used to flag the required treatment. The reason why this may be preferred is because in this case such lists 
will be embedded in Mashery endpoint configuration, which will remove the requirement of arranging e.g. S3 bucket storage 
and thus will contribute to *simplifying* the sidecar code that needs to be written.

However, if such list will become either rather long or rather volatile, then it will become the responsibility of the 
author of the sidecar to address the size and volatility. This can be solved, inter alia, by maintaing the 
mapping from package keys to the desired treatment e.g. in the Dynamo DB. 

      

   
### Request and Response Headers
Depending on the point and set configuration, the input to the pre-processor may contain data from both API request
and API response as part of the same input. Since both request and response data contain, essentially, headers and 
payload, these will be included under `request` and `response` data structures. 

At the pre-processor point, the `respose` element will alway be empty. At post-processor point, the `request` will 
be filled only if the post-processor configuration requests this specially.

The default minimum information supplied about the request and response is the headers and the response code for the
post-processor point. Thus, `request.headers` will 
refer always to the headers that are sent towards the API origin, and `response.headers` will refer to the 
headers *returned from* the API origin. The basic input is sufficient for the 2nd factor authentication cases where 
a client has to send headers to authenticate itself or provider has including headers that the lambda provider will 
need to process.

```json
{
  "request": {
    "headers": {
       "header_a": "Value_A"
    }
  }
}
```

```json
{
  "response": {
    "code": 201,
    "headers": {
      "header_b": "Value_B"
    }
  }
}
```

The `headers` element contains the list of headers that will be sent to the API origin (in the `request` structure) or 
to the API client (in the `response` structure). The headers are sent to the sidecar as *lowercase*.

Unless specified otherwise, the default behaviour is to send all headers to the sidecar. Functions that require
only specific headers can reduce the list of headers that are included in the pre- and post-processor configuration
respectively by specifying `include-request-headers` and `include-response-headers` respectively.

In some cases, functions may seek to improve the positive effect of idempotence processing by removing headers that
are not necessary and cause unnecessary variation, such as e.g. a `User-Agent` header.

### Called Operation
A first obvious expansion that a function may need is identification of the resource the caller is trying to use and
the parameters of this operation. The operation includes multiple fields and is rolled up into a separate JSON field:
```json
{
  "operation": {
    "httpVerb": "POST",
    "path": "a/path/to/myresource",
    "query": {
      "queryParam1": "queryParamValue"
    },
    "uri": "https://the.host.name/api_path/a/path/to/myresource?queryParam1=queryParamValue"
  }
  
}
```
These fields are:
- `httpVerb`: the HTTP verb that the client has sent;
- `path`: the path behind the publicly facing endpoint path;
- `query`: individual query parameters if a query was specified;
- `uri`: the full URI including the scheme, port, host, and endpoint path that the client has specified.

### Remote address
Remote address will rarely have a useful significance on the internet, therefore this field is omitted to save
the unnecessary traffic.

Nevertheless, some applications (e.g. security logging) may find this parameter useful. In case enabled for the 
particular lambda, then it will be filled with the Mashery-known IP address:
```json
{
  "remoteAddr": "123.456.789.012"
}
```

### Token information
The scope and user context would be typically available as headers that will be made available the the lambda
anyway as `x-mashery-scope` and `x-mashery-user-context`. The token information supplies two additional fields, the
expiry date of the token as well as the grant type. This information will be needed by functions that want to 
attach the significance ot the grant type.

```json
{
  "token": {
    "scope": "a-scope",
    "userContext": "aUserContext",
    "expires": "2020-01-01T13:39:45Z",
    "grantType": "AC"
  }
}
```
> TODO: extract the values for the grant type which Mashery is reporting.
### Routing
Routing information gives the HTTP verb and the URI of the back-end, to which it will be sent. This information may
be interested to the functions that aim optimizing the traffic to the back-ends which are overused. 

A typical busines scenario could be that an application is not generating enough conversions while using a back-end that incurs a
very high running costs for AFKLM. sidecars could do several things with this information:
- implement selective caching for named application keys;
- override changeRoute information for the specific request, e.g. re-route the client request to the back-end having
  lesser running costs;
- or, even, send a message to the client indicating that the servic ewill not be provided

```json
{
  "changeRoute": {
    "uri": "https://the-backend-uri",
    "httpVerb": "POST"
  }
}
```

The fields are:
- `uri`: the URI to which the request is to be sent, and
- `httpVerb`: the HTTP Verb that will be passed to the API origin server.

### Extended Application Values
Mashery defined two types of extended application attributes (called *EAVs*):
- package key attributes, and
- application attributes
An application may own multiple keys, therefore this difference may be significant. The requied EAVs must be specifically
listed in the pre- or post-processor configuration. These can be included as:
    - required to be filled, or
    - optional to be filled.
    
In case a required EAV is missing, the calling API client will receive a 400 message with an opaque statement that
the pre-condition for the call has not been met. 

Application-level attributes are included under the `eav` field, listing the values of all EAVs:

```json
{
  "eavs": {
    "eav_name": "eav_value"
  }
}
```

The requested package key-level EAVs are provided under the `packageKeyEAVs` field as follows:
```json
{
  "packageKeyEAVs": {
    "eav": "eav_value"
  }
}
```

### Pre-processor relay message and parameters
In some cases, a pre-processor may need to communicate important parameters to the post-processing. For example,
a pre-processor may establish that a request exhibits an unusual behavior pattern. A post-processor which is 
sanitizing the outputs, may choose to remove or obfuscate data elements that are considered sensitive.

There are two options for the pre-processor to pass the information to the post-processor:
- by passing a relay message, and
- by passing a relay parameters.

The relay message, like it's name implies, is a string that can be interpreted by the post-processor. It is meant to
pass light-weight information. Relay message is passed as a parameter:
```json
{
  "params": {
    "relayMessage": "the-passed-message"
  }
}
```

In more complicated scenario, a pre-processor will communicate named parameters that will be included in the
`params` section of the sidecar input:
```json
{
  "params": {
    "__l_RelayParameter": {
      "audit": false,
      "level": 3
    } 
  }
}
```
The above example shows that the pre-processor has passed a relay parameter `__l_RelayParameter`, which is, actually,
a complex object.

A pre-processor is free to pass any parameters as long as they fit in the JSON notation.

The deployer should specify which expansion is needed, either for `relayMessage`, or for `relayParams`.

### Payload Body
The processor passes the payload to the sidecar, as is has arrived at the API gateway, without applying 
any form of modification. The motivation for this decision is that the payload can be compressed with various 
compression schemes that are not necessarily known to the processor. Or, the client and application can agree
a protocol that will use specially composed JSON-like structures that cannot be correctly parsed by the
default JSON parser.

```json
{
  "request": {
    "payload": "a payload string"
  }
}
```

If the API client has applied a compression algorithm, e.g. gzip, then the content of the `payload` field
will be the gzipped body as received by Mashery. For this matter, the labmda function needs to ensure that 
it receives the `content-type` and `content-encoding` headers.
```json
{
  "request": {
    "headers": {
      "content-type": "text",
      "content-encoding": "gzip"
    },
    "payload": "H4sIAAA2aF0AA3N0JAo4IQFnZOCCBlzRgBsWAABKYxehdAAAAA=="
  }
}
```

