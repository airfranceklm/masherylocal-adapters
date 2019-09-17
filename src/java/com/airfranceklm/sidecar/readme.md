[func]: ../../../../../doc/sidecar-functional-flow.png "Lambda sidecar flow"

# Air France/KLM Mashery Lambda Sidecar Processor for Mashery Local

The AFKLM Mashery Local processor allows injecting pre- and post-processing logic from the function running in
AWS, local HTTP servers, or any other supported communication stack.

# What is lambda sidecar?

The AFKLM Mashery Lambda sidecar (referred to hereinafter as lambda sidecar) is a piece of a logic running externally 
from the API gateway that can do two things with an API call:
- do something with an API call data that an API origin server cannot do or should not do (such as e.g. security logging 
  and gathering data for traffic analytics), and/or
- provide instruction to the API gateway on what should be done with the API request or response.

The instruction can largely fall into three options:
1. The request or response should be sent to the intended recipient in its original form;
2. Some parameters of the request should be modified; or
3. A different response should be sent to back to the API origin or to the API client.

## Why it is called sidecar?

It is called sidecar, because the design follows the [sidecar pattern](https://www.oreilly.com/library/view/designing-distributed-systems/9781491983638/ch02.html),

It is *not* called "lambda sidecar" because the implementation of the pre-processor is not limited to the AWS Lambda.
A pluggable transport interfaces is supported within the pre-processor core. It allows 
integrating any network source that provide compatible responses as a sidecar. If you run Mashery Local on-premise, then 
HTTP stack (and eventually OpenFAAS stack) is provided out-of-the-box. Organization-specific transports can be added
via extending the provided codebase, such as e.g. RMI (TODO).

Most of the sidecars will be reachable via HTTP calls, with an option to make call-outs to
AWS Lambda functions. HTTP is also the default invocation stack.

## Motivation for the sidecar processor

The motivation for building the sidecar processor is to acquire the means to deal with the undesired effects of the 
API user behaviour. Externalising processing logic to a sidecar lowers the barrier for the product team to 
rapidly extending the pre- and post-processor functionality available in Mashery without requiring to learn the
specifics of the pre- and post-processing.

The design reason why the logic is externalized into the sidecar is that it intends addressing user behaviour, which
is *temporary* in nature and can change without a warning. In other words, externalizing the pre- and post-processing
is preferred over other alternatives when the following conditions are met:
- a *rapid* implementation is required to solve a problem at hand by means of pre- and post-processing;
- the  pre- and/or post-processing solution is meant to deal with a situation that is *temporary* in nature;
- the solution is both:
  - impractical to be a structural part of the API origin design (mainly because it is of a temporary measure); and
  - impractical to be embedded as part of the API gateway code as the problem is API-specific.
  
Providing the generic lambda sidecar processor that allows making the call-outs to the lambda sidecars (or similar
resources) achieves the best compromise.

Using the lambda sidecar processing, AFKLM will be able to achieve via a centralized process:
1. Implement custom authentication schemes;
2. Implement custom (as well as temporary) authorization schemes;
3. Add incoming content inspection e.g. for the presence of SQL injections;
4. Add AFKLM-specific application information that should be used by the API provider;
5. Manage the load on the API back-end by providing custom caching for selected resources;
6. Implement response content filtering on a custom condition;
7. Collect data for usage pattern identification and abnormal traffic pattern detection;
8. Collect data for business analytics;
9. Collect data for the security monitoring;
10. Sanitize user input. 

## Idempotent sidecar calls
Certain lambda sidecars can process operations that can be considered idempotent. Idempotence means that:
- calling a function multiple times will not change the state after the first invocation was made; and also
- if called repeatedly with identical inputs, the same output will be produced.

The concept of idempotence can be seen as loosely related to caching, yet it has a crucial difference. Caching seeks
to improve the performance of application by shortening the access time to frequently used data. The idempotence
allows the processor avoiding calling lambda sidecars where it is assured that the same response will be received.

In some scenarios, e.g. if the purpose of a lambda sidecar is to implement access logging, a lambda sidecar must be
always called. In others, e.g. where the function authenticates caller, the response might be considered as
*idempotent* for some time, as the authentication with the same credentials should repeatedly succeed.

It is the responsibility of the lambda sidecar to indicate whether an individual response is idempotent, and for how
long. A processor *may* use the indication of idempotence to optimize the running cost of the lambda sidecar, but it does not
guarantee that it *will* actually use it.

## Lambda sidecar invocation modes
The lambda sidecar can be invoked in three different modes:
1. As a request/response invocation, which is useful where the output of the lambda sidecar call will be used 
   to modify a content that is sent to the API provider or to the API consumer. The response time as seen
   by the API consumer will incur communication overhead with the Lambda sidecar as well as
   any processing time taken  by the function. This mode ensures that the sidecar has successfully processed
   the necessary input.;
2. As a synchronous event invocation, where is useful where the output of a lambda sidecar will not be used to
   modify the request sent to API provider and/or a response returned to the API consumer. In this mode, the processor 
   will invoke the stack indicating that it is not expecting to receive
   a response.  The response time as seen by the API 
   consumer will incur only the communication overhead over the stack, without waiting for the response from
   the Lambda sidecar. It is the responsibility of the stack to execute the sidecar call asynchronously.
   
   This mode, if the API consumer receives a successful response, then the processor was able to post necessary
   message to the lambda stack successfully. However, it does not guarantee that the sidecar will successfully process
   the data.
   
   It is the preferred configuration fo cases such as logging, analytics, quality control,
   anomaly alerting. 
3. As a non-blocking event invocation. This mode is analogous to the synchronous event invocation
   mode describe above, with the difference that the processor will queue an API call data for
   the invocation outside of API call processing. The response time as seen by the API will incur
   no additional time introduced. In order to prevent the memory exhaustion at run-time, 
   the processor observes the capping limit on queue length. 
     
   If the API traffic is such that this limit is exceed, then the processor will silently skip 
   queueing API calls data until the queue length will decrease below the capping limit.
   
The difference between mode (2) and mode (3) is that mode (3) *guarantees* no visible changes to
the response times observed by the clients at the cost of:
 - skipping invocation where the capping limit is exceeded, and
 - accepting the possibility that API client will receive a successful response even if the processor cannot reach
   the intended sidecar over the indicated stack.
    
This option will be preferred by the scenarios where the response time is critical while
the purpose of the Lambda sidecar fits into [eventual consistency](https://en.wikipedia.org/wiki/Eventual_consistency) 
semantics (e.g. usage analytics).

## Sure-fire vs Fail-safe error handling policy
By default, the processor assumes that a successful invocation of a function is critical
for the processing of the API call (a sure-fire error handling policy). Then, should a function fail,
the call processing should also fail.

Such behaviour may be too strong for deployments that implement auxiliary features in
lambdas. To accommodate the integrations with the lambda sidecar that are not critical
for processing the API call, the processor supports the notion of a *fail-safe* error handling policy.
In a fail-safe mode, a failure
of the lambda sidecar will not impact serving the traffic (albeit an inevitable response
time degradation will be observed by the clients).

Where the invocation is *not* fail-safe, then the fault of the lambda invocation will result
in 500 Internal Server Error being sent to the client.

The choice of whether or not to mark an endpoint a fail-safe depends on the nature the
function implements. 

## Pre-flight checks
A pre-flight check is a special type of the sidecar invocation that aims establishing the parameters that need to
apply on all requests going through the given endpoint. Because of the very nature, the pre-flight will occur only
within the scope of the pre-processor.

The  motivation for pre-flights is to:
- retrieve (and cache) the attributes describing the API client application and, possibly, a user of an application. This feature
  is used when two things need to happen with the API call:
    1. A specific information needs to be added to all requests forwarded to the API origin; in combination with
    2. Specific pre-processing (e.g. custom authorization) of particular requests. Pre-flight checks allow optimizing
    the calls to the lambda sidecar by relying on the idempotence indication of the pre-flight checks;
- in case the user must be denied the access to the endpoint across the board, then this can be achieved with a single
  pre-flight invocation, saving multiple successive calls to the sidecars.
  
The pre-flight check is does not consider the specific of the operation. The  
 

## Supported stacks
Currently, the AWS and HTTP stack are supported. 

The AWS stack is the stack that will be implied by default. In order to use the stack, the Mashery Local machine
must be able to make calls to the AWS Lambda gateway. A firewall or a transparent proxy needs to be configured to
allow this traffic forwarding.

The HTTP stack is provided as an alternative for the situation where the travel to the AWS Lambda is either not possible
(due to the closed firewall) or not desired (e.g. due to a high volume of calls the service will have to process).
The HTTP stack assumes that the connection from Mashery Local and the receiving endpoint runs over the trusted and
protected network where strong forms of security are not required. 

# Lambda sidecar working explained 

![Functinal Flow][func] 

The picture above gives the overview of the flow.
- Point A: the client has to be in possession of a valid key, and be able to authenticate himself according to the
  existing Mashery configuration. Mashery processes the request and prepares the proxied request to be forwarded to the
  API origin host.
- Point B: Mashery routes the prepared data structure to the AFKLM Lambda processor. At this point, three things happen:
  1. The processor ensures that the configuration of the endpoint pre-processing is valid. An invalid configuration (e.g. not 
  supplying parameters to invoke sidecar stack) will result in an `500 Internal Server Error` being sent to the 
  calling client.
    - P/1: if enabled for the endpoint, the processor will:
        - check if it has the pre-flight results already in cache. In available, the pre-flight call will not be made;
        - otherwise, a pre-flight check is done in a request-response invocation mode;
    - P/2: the sidecar replies with the response, which **should** be marked as idempotent. After it:
        - The processor will cache the results for the duration the sidecar specified, avoiding making successive pre-flight
          calls;
        - Interprets the results of the pre-flight check similarly to that of pre-processor;
  2.  The processor checks if the request is in the Lambda sidecar scope (explained below). If the request is outside 
      of scope, no action is taken. This measure is applied to enhance the
      response time by avoiding calling functions on requests that are not necessary (as well as to conserve any associated
      costs of running unnecessary workloads in the cloud);
  3. The processor checks if need to assert the request meeting the condition. If the condition is not met, 
      then an HTTP error 400 is returned back to the client.
      If the pre-conditions are met, then the Lambda sidecar pre-processing is invoked.

    When the conditions explained at the Point B are met, then the processor will:
    1. Invoke the specified lambda sidecar according to the desired invocation mode (request-response or asynchronous);
    2. Receive response from the sidecar stack hand handle the invocation error, should it occur (either according to fail-safe 
   or sure-fire error handling policy).
    3. In request-response API origin is modified, according ot the instructions returned from the lambda sidecar. 

- At this point, the pre-processing ends.
    4. Mashery send the (modified) request to the API origin host;
    5. API origin host sends a reply;

- Point C: Mashery checks if the post-processing is enabled for this API. If it is, it will request the processor
  to perform the post-processing of the request. At this point, the following actions will be pefromed:
   1. The processor ensures that the configuration of the endpoint post-processing is valid. An invalid configuration (e.g. not 
     supplying parameters to invoke the sidecar stack) will result in an `500 Internal Server Error` being sent to the 
     calling client.
   2. The processor checks if the request is in the Lambda sidecar scope (explained below). If the request is outside 
   of scope, no action is taken (for the same reasoning as in pre-processing).
   
   If the API call is in scope, then process will:
   1. Invoke the specified lambda sidecar according to the desired invocation mode (request-response or asynchronous);
   2. Receive response from the sidecar stack hand handle the invocation error, should it occur (either according to fail-safe 
   or sure-fire error handling policy).
   3. In request-response, the response sent to the API client is modified, according ot the instructions returned 
   from the lambda sidecar.
   
- Point D: a response is returned to the calling client, with or without modifications from the sidecar(s).
   

# Lambda sidecar processor configuration
The processor applies separate configurations for pre-processing and post-processing. This
allows the deployer to choose appropriate strategies depending on the functional need 
of an endpoint and the performance considerations. Also, pre- and post-processing can be enabled separately, depending
on the needs.

A pre- and post-processing requires specifying the stack, necessary configuration parameters of the stack, and 
identification of the sidecar that has to be invoked.

Where no other specific parameters are specified, the following settings are assumed:
- The processor will observe the sure-fire error handling policy for this invocation (a call function call must succeed
  in order for the API client to receive a non-error reponse);
- The sidecar is invoked as a synchronous event invocation;
- Maximum payload size will be set to 50mb.


The deployer can choose between two options of how a sidecar can be configured:
- via centralized Mashery configuration, by providing key-value properties in the pre- and post-processor window. This
  configuration is preferred where the configuration paramaters are rather simple;
- via distributing a set of configuration files to the Mashery machines handling the traffic. This will be preferred
  where the configuration has to supply a lot of parameters 

Configuring these steps is explained in the following sections.

## Configuration Parameters for in-Mashery configuration
Lambda processor supports the following parameters:
- `synchronicity` specifies the synchronicity of the request;
- `require-headers` specifies the list of headers the client should send;
- `include-request-headers` specifies API origin request headers to be included in the lambda sidecar input. If omitted, 
   all request headers are included;
- `skip-request-headers` specifies API origin request headers to be skipped in the lambda sidecar input. This option
   is mutually exclusive with `include-request-headers`;
- `include-response-headers` specifies the API client response headers to be included in the lambda sidecar input.
   if omitted, all response headers are provided;
- `skip-response-headers` specifies the API client response headers to be skipped in the lambda sidecar input.
   It is mutually exclusive with `include-response-headers`;
- `require-eavs` specifies the mandatory application EAVs that must be set to a non-empty value before
   the request can be handled. These values are also included in the lambda sidecar input;
- `include-eavs` specifies the application EAVs to be included in the labmda function input, if they are present;
- `require-packageKey-eavs` specifies the mandatory package key EAVs that must be set to a non-empty value before
   the request can be handled. These values are also included in the lambda sidecar input;
- `include-packageKey-eavs` specifies the package key EAVs to be included in the labmda function input, if they are present;
- `stack` specifies the lambda invocation stack to use. Currently supported is `aws` and `http`.
- `expand-input` specifies data elements that are required to be specified in the lambda sidecar input
- `failsafe` specifies whether or not it is safe to proceed with the handling of the request or response if a failure 
   would occur during invoking the lambda sidecar;
- `max-payload-size` specifies the maximum payload size;
- `${stack}.<parameter>` specifies the configuration parameter for the sidecar stack. The `${stack}` token should be 
  identical to the value set if `stack` parameter;
-  `lambda-param-<parameter>` specifies the parameter to be passed to the lambda sidecar.
- `<filtering>`-`<datum>`-`label` specifies lambda scope condition (explained in section Lambda Sidecar Scope 
   Configuration below);
- `idempotent-aware` specifies whether sidecar invocation should be honour the idempotent responses. The field can
   be set to `true` or `false`. It should be enabled where processing idempotent requests is a part of the sidecar
   design, and storing idempotent response will get a desired effect on the traffic.
- `enable-preflight`: whether the processor should be doing pre-flight checks. Implies that `idempotent-aware` is set
   to `true`.
- `preflight-headers`: headers to be included in the pre-flight request. If omitted, only `authorization` header is
   provided as an input;
- `preflight-eavs`: EAVs to be included in the pre-flight call;
- `preflight-packageKey-eavs`: EAVs of the package key to be included in the pre-flight call.   
- `preflight-params`: optional parameters to be passed to the pre-flight call.   
- `preflight-include-token`: whether the token information needs to be included in the pre-flight call. Defaults to
   `false`. If a pre-flight check requires a user token information, this parameter needs to be set to `true`.
- `preflight-include-httpVerb`: whether the HTTP verb is to be included in the pre-flight check.
- `honour-local-configuration`: whether Mashery Local instance should honor the specific local configuration.      
   
## Configuring Lambda Processor

### Configuring maximum payload size
The maximum payload size that could be processed is configured at the endpoint level
by `max-payload-size`, which determines the maximum payload size that can be sent
to the lambda sidecar.

### Configuring synchronicity
The synchronicity is configured by the `synchronicity` parameter which can be set to
either `request-response` or `non-blocking`.

### Enabling fail-safe
To enable fail-safe processing, the `failsafe` option must be set to `true`. This is done
specifically to ensure that the deployer is aware which endpoints are configured as such.

### Configuring EAVs
When specified, the processor will include values of the EAVs listed in the `include_eavs`. 

## Configuring processor by distributing configuration file
The processor can load sidecar configuration from the files that are located on the Mashery Local machine itself. In 
this scenarios, the deployer shall:
- Distribute a file(s) describing the sidecar invocation parameters to all Mashery Local machines and set these in
  `/etc/mashery/afklm-lambda` directory;
- Within Mashery:
  - Specify the processor for the desired endpoints in the Call Transformation tab;
  - Set pre- and post-processing enabled as desired;
  - Set `honor-local-configuration` to `true` in the Mashery configuration properties.
  
**Providing a local configuration overrules all SaaS-based configuration**  

The motivation for this mechanism is to provide a consistent configuration for the hybrid deployments. Depending
on the situation, different side cars may need to be invoked in SaaS (e.g. AWS-based sidecars) and in Local (e.g. using
HTTP stack). Furthermore, a particular local machine may require specific stack configuration due to the networking
requirements that would not be applicable to all Mashery Local machines in the same cluster.

The requirement to set `honour-local-configuration` is to ensure that the deployer is **explicitly** aware that he is 
 implementing the Mashery Local-specific configurations to achieve the desired sidecar pre- and post-processing.

**TODO**


## Lambda Sidecar Scope
To optimize the API response time, it might be necessary to limit the requests that the lambda sidecar should process.
This is referred to as "Lambda Sidecar Scope". 

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

## Lambda sidecar scope design limitations
The design purpose of the lambda sidecar scope mechanism, as well as idempotent calls handing, is to optimize the 
use of the API gateway resources (memory, network, cache storage, etc). This optimization is achieved by avoiding
calling the lambda sidecar where it will do nothing and yield no instructions to modify the request. However, this 
mechanism **does not** pursue **removing** such calls completely.

A lambda sidecar **must** be written in such a way that it functions correctly if:
- no scope filtering is implemented, and
- the API Gateway is not considering idempotency indication. 

## Lambda sidecar scope configuration

The configuration parameter are declared as `<filtering>`-`<datum>`-`label` combination:
- the `<filering>` element can be either `filter` or `filterout`, indicating the inclusion or exclusion scope 
  respectively;
- the `<datum>` element indicate the datum to consider. Some datums, like headers, and EAVs, require being specific
  about which exactly element is required. Such expression will be then written as `<datum>(<parameter>)`;
- the `label` element is optional, it allows splitting multiple options into simpler, less error prone
  better readable and maintainable sequence. 

Let's consider the example where a lambda sidecar has to apply on the user context with a rather complex criteria.
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
will match the requests. If set incorrectly, it might lead either to unexpected high traffic towards the lambda sidecar,
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

### Scoping lambda sidecars to several paths
Lambda sidecars can be scoped to be applied on several resources of the API.
The configuration the can define the names for these paths as well as the labels:
```properties
filter-resourcePath-op1=path/to/op1/*
filter-resourcePath-op2=jump/across/op2/*
```

The intention of such configuration is that the lambda sidecar applies when the request path matches *either* of 
these paths.

This condition can also be specified in an exclusive fashion, e.g. where the function has to apply *unless*
the reousce path will match either of these paths.
```properties
filterout-resourcePath-public=public/*
filterout-resourcePath-content=content/*
```

## Request Conditions Assertion
The design of the API may require that the request shall meet certain conditions. Where these conditions would not
be met, then the server should respond with the status code ``400 Bad Request`` to indicate that this is the client
error.

The adapter supports checking two conditions are supported:
- The client must include a non-empty header (or headers) with the request;
- The client's application must contain a non-empty EAV attribute (or attributes).

For each endpoint, a desires combination of readers and EAVs can be specified as follows:
```properties
require-headers=Header_1,Header_2,Header_3
require-eavs=eav_1,eav_2,eav_3
```
- The headers are *case insensitive*, whereas
- The EAVs **ARE** case-sensitive, and must be included exactly as they are known in Mashery.


## Maximum Payload Size
If a lambda sidecar requires processing the payload, then the design of the processor requires setting
the maximum payload size foe the endpoint which the API client should not reasonably exceed. The purpose of this is 
to prevent service degradation arising from submitting the "payload bombs" to the lambda sidecar. By default, 
this limit is set at 50 
kilo-bytes. This limit can be configured per endpoint to reflect the lambda sidecar's purpose.

The format of the configuration property is as follows:
```properties
max-payload-size=size[k|m]b,([blocking|filtering])
```

The limit can be configured to apply either:
- as filtering condition: the lambda sidecar shall not be executed on such payloads, while the request itself should 
  not be blocked by the processor;
- as a blocking, i.e. request condition assertion: if an API client will sends a request that is too large (or an API 
  returns a response that is too large), an error should be sent back to the user.
  
By default, the maximum payload size applies as a request condition assertion.

In case the endpoint configuration providers a value to `max-payload-size` property that doesn't match this pattern,
then the default maximum payload size is applied (50 kilo-bytes, blocking).
  
### Configuring Maximum Payload Size as a Filter
To configure as a filter of 57 kilo-bytes, specify
```properties
max-payload-size=57kb,filtering
```  

The lambda will apply only to the request that submit less than 57 kilo-bytes of payload.

### Configuring maximum payload size as a request condition assertion
To configure the adapter to ensure that the client will send less than 1 mega-byte, specify
```properties
max-payload-size=1mb,blocking
```

If the API client will send a request with the payload exceeding this limit, then the the pre-process will return the 
response matching the following criteria:
- HTTP response code: 400
- Content-Type: application/xml
- Content of the response: 
```xml
<h1>Request pre-condition not met, code 0x000003BB</h1>
```

If the API origin will generate a response with the payload exceeding this limit, then the the pre-process will return the 
response matching the following criteria:
- HTTP response code: 500
- Content-Type: application/xml
- Content of the response: 
```xml
<h1>Response pre-condition not met, code 0x000003BB</h1>
```
   
# Sidecar stack
A lambda sidecar is invoked through a communication channels commonly referred to as "Sidecar Stack". Each stack
has its specifics of how the Lambda sidecar input (discussed in the separate section below) will appear to the 
function code.

## AWS Stack
AWS stack, like it's name implies, will call functions deployed in the AWS Lambda service. This stack natively supports
request-response as well as synchronous event invocations. 

### Input and output locations
With the AWS Lambda, the input will be translated (by the AWS) into the `event` object. The following gives a 
brief overview of a Lambda handler in Node.js that prints the passed synchronicity. The output, if expected,
shall be an object having a JSON representation compatible with the Lambda Output JSON structure described in this
guide below.

```js
exports.handler = (event) => {
    console.log(`Function called with ${event.synchronicity} settting.`);
    return {};
}
```  

### AWS Stack Configuration
The AWS Lambda stack is a default stack that will be selected. It can also be explicitly designated
by specifying
```properties
stack=aws
```
in the pre- or post-processor configuration parameters.

Additionally, AWS lambda stack requires 4 parameters:
```properties
aws.functionARN=an_Amazon_Resource_Name_of_the_function
# Example of the above: arn:aws:lambda:eu-west-1:190954361614:function:LambdaBasicAsync

# The key and secret will be read from test properties.
aws.key=client_key
aws.secret=client_secret
aws.region=eu-west-1
```

Failure to provide at these will result in the error 500 being returned for invalid configuration.

The deployer should check that the function is accessible in the specified region with the specified parameters 
before the deployment.

To configure running the AWS lambda behind the proxy, add the following settings:
```properties
aws.proxyHost=proxy_host
aws.proxyPort=443
```
To specify the HTTP proxy the server to use in order to connect to the Lambda functions.

## Local HTTP Stack
The HTTP stack is used to deliver the Lambda input as a JSON-encoded payload to the URL that is provided in the 
end point's configuration. 

### Lambda input location

The intput is submitted in the HTTP request body is with the following headers:
```text
Accept: application/json
Accept-Charset: utf-8
Accept-Encoding: gzip
Content-Type: application/json; charset=UTF-8
Content-Encoding: gzip
```
  
The response to be returned shall meet the following parameters:
- the `content-type` will be `application-json`. A `gzip` `content-ecoding` will be supported;
- the payload shall be JSON-encoded object compatible with Lambda sidecar output.

The following Open API specification gives a formal description to input and output parameters. (You can also
see the specification on the [Swagger Hub](https://app.swaggerhub.com/apis/aleks.amt/afklm-lambda-sidecar-httpstack/1.0)
```yaml
openapi: 3.0.0
info:
  version: '1.0'
  title: 'AFKLM Lambda Sidecar HTTP Stack API'
  description: 'Air France/KLM HTTP Lambda sidecar API'


components:
  schemas:
    AnyValue:
      description: Any simple or complex object
      nullable: true
      
    LambdaSidecarOutputRouting:
      nullable: true
      type: object
      properties:
        host:
          type: string
          nullable: true
        file:
          type: string
          nullable: true
        httpVerb: 
          type: string
          nullable: true
        port:
          type: integer
          nullable: true
          
    LambdaSidecarOutput:
      type: object
      description: Request processing instruction
      properties:
        dropHeaders:
          nullable: true
          type: array
          items:
            type: string
        
        addHeaders:
          nullable: true
          type: object
          additionalProperties:
            type: string
            
        unachangedUntil:
          type: string
          nullable: true
          description: Timestamp until which further requests with the same input will be idempotent
        
        changeRoute:
          $ref: '#/components/schemas/LambdaSidecarOutputRouting'
        
        payload:
          type: string
          nullable: true
        json:
          $ref: '#/components/schemas/AnyValue'
          
        code:
          type: integer
          nullable: true
        message:
          type: string
          nullable: true
          
    
    LambdaSidecarInputRouting:
      nullable: true
      type: object
      properties:
        httpVerb:
          type: string
          nullable: false
        uri:
          type: string
          nullable: false
  
    LambdaSidecarInputToken:
      nullable: true
      type: object
      properties:
        scope: 
          type: string
          nullable: true
        userContext:
          type: string
          nullable: true
        expires:
          type: string
          nullable: false
        grantType:
          type: string
          nullable: false
    
    LambdaSidecarOperation:
      type: object
      properties:
        httpVerb:
          type: string
          nullable: false
        path:
          type: string
          nullable: false
        query:
          type: object
          nullable: true
          additionalProperties:
            type: string
        uri:
          type: string
          nullable: false
          
    LambdaHTTPInputRequest:
      nullable: true
      type: object
      properties:
        headers:
          type: object
          nullable: true
          additionalProperties:
            type: string
        payloadLength:
          type: number
        payload:
          type: string
          nullable: true
          
    LambdaHTTPInputResponse:
      nullable: true
      type: object
      properties:
        code:
          type: integer
        headers:
          type: object
          nullable: true
          additionalProperties:
            type: string
        payloadLength:
          type: number
        payload:
          type: string
          nullable: true
          
    LambdaRequest:
      type: object
      properties:
        point:
          type: string
          nullable: false
        synchronicity:
          type: string
          nullable: false
        packageKey:
          type: string
          nullable: false
        serviceId:
          type: string
          nullable: false
        endpointId:
          type: string
          nullable: false
        params:
          type: object
          nullable: true
          additionalProperties:
            type: string
        request:
          $ref: '#/components/schemas/LambdaHTTPInputRequest'
        response:
          $ref: "#/components/schemas/LambdaHTTPInputResponse"
        eavs:
          type: object
          nullable: true
          additionalProperties:
            type: string
        packageKeyEAVs:
          type: object
          nullable: true
          additionalProperties:
            type: string
        operation:
          $ref: '#/components/schemas/LambdaSidecarOperation'
        token:
          $ref: '#/components/schemas/LambdaSidecarInputToken'
        routing:
          $ref: '#/components/schemas/LambdaSidecarInputRouting'
        remoteAddress:
          type: string
          nullable: true
      

    
paths:
  /.:
    post:
      operationId: lambda-sidecar-op
      description: AFKLM Lambda Sidecar invocation.
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LambdaRequest'
      responses:
        '200':
          $ref: '#/components/schemas/LambdaSidecarOutput'
        '202':
          description: Function invoked asynchronously.
          
```


### Configuring HTTP stack
The configuration of the supports the following parameter parameters:
```properties
stack=http
http.uri=http://server:port/file
http.compression=true
http.<header_name>=<header_value>
```
The mandatory parameter is `http.uri`, to which the server should be posted.

Optional parameters are:
- `compression`: boolean, could be either `true` or `false`, and determine whether the GZip compression
  should be applied on the posts. Default value is yes; however if the server doesn't understand gzip compression.
- `http.<header_name>`: headers that must be additionally included in the request. These parameters could be used
  e.g. to pass a custom authorization headers. E.g: `http.authz=FFFDEWSNMNHJSJ` will add the `Authz` header to each
  request having the value `FFFDEWSNMNHJSJ`.

# Lambda sidecar

## Basic Inputs to the lambda sidecar
Lambda sidecars receives always the following minimum input:
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
- `point`: `PreProcessor` or `PostProcessor`, or `Preflight`. Indicates the point at which the lambda sidecar is invoked. In case
   the synchronicity is specified as `RequestResponse`, this field indicates where the output of the lambda sidecar
   will be used:
   - in case of `PreProcessor`, the response will modify the request that will be sent to the API provider;
   - in case of `PostProcessor`, the response will modify the response that will be returned to the API client.
   - in case of `Preflight`, the response will apply to all calls of this client. The response **should** also be 
     marked as idempotent.
- `packageKey`: the package key representing the application that Mashery has authenticated;
- `serviceId`: Mashery-internal identifier of the Mashery API Service.
- `endpointId`: Mashery-internal identifier of the endpoint within the Mashery API Service identified by `serviceId`;
- `params`: optionally, if specified in the configuration, a set of additional parameters that a function could 
   consider. The interpretation of these parameters is function-specific. If no parameters are specified, this field
   will be omitted.
   
The processor will attempt minimal type conversion for the parameters:
- values matching the `/\d+/` regular expression will be converted to integer numbers,
- values matching the `/\d+\.\d+/` regular expression will be converted to floating pointer numbers,
- values matching the `/true|false/` regular expression will be convert to booleans,
- values reading `null` will be converted into null values;
- anything else will be considererd a string.
   
## Pre-Flight Inputs

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

### Should Lambda sidecar use the supplied filter element labels?
The decision for whether to use the labelled fields or not in the lambda sidecar logic is down to the problem the 
respective function needs to solve and the details of the chosen implementation.

In the complete stack, there are three elements:
- Mashery endpoint configuration (which can be *infrequently* changed), and
- Processor code (which we can consider unchangeable on a short notice),
- Lambda sidecar code (where the frequency of change is controlled by the labmda function's author)
that work together to produce the intended traffic handling.

In some situations it might be actually *beneficial* to leverage the Mashery endpoint configuration. A good example is
specifying a "short-list" of package keys requiring a special treatment by the lambda sidecar, where the label
will be used to flag the required treatment. The reason why this may be preferred is because in this case such lists 
will be embedded in Mashery endpoint configuration, which will remove the requirement of arranging e.g. S3 bucket storage 
and thus will contribute to *simplifying* the lambda sidecar code that needs to be written.

However, if such list will become either rather long or rather volatile, then it will become the responsibility of the 
author of the lambda sidecar to address the size and volatility. This can be solved, inter alia, by maintaing the 
mapping from package keys to the desired treatment e.g. in the Dynamo DB. 

      
## Expanding lambda sidecar inputs
In order to optimize the traffic to the lambda sidecar
and to reduce the latency, the configuration of the pre- and post-processor for each endpoint must indicate which 
additional data elements it actually needs. The function can choose to receive:
- Headers (included by default, but could be filtered or suppressed),
- Called resource
- Remote address of the API client
- Token information
- EAVs of the package key or of the application

### Technical limitations about expanding post-processor inputs

Due to the limitation of Mashery API, it is not possible to extract the data that was sent to the API origin from the
Mashery post-processing event. Only the *original* client request will be available. The original request will 
frequently contain elements, such as access tokens, that are not desired to be sent to the Lambda sidecar.

It is the responsibility of the deployer to ensure that all sensitive elements are *dropped* in the event the 
lambda sidecar will require the combination of the request and response data.
   
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
to the API client (in the `response` structure). The headers are sent to the lambda sidecar as *lowercase*.

Unless specified otherwise, the default behaviour is to send all headers to the Lambda sidecar. Functions that require
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
very high running costs for AFKLM. Lambda sidecars could do several things with this information:
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
`params` section of the Lambda sidecar input:
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
The processor passes the payload to the lambda sidecar, as is has arrived at the API gateway, without applying 
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

## Lambda sidecar outputs
The output of the Lambda sidecar is only meaningful if the function is invoked in the
request/response fashion and specifies what needs to be *changed* in the API origin request (for pre-processor) 
or  in the API client response (for post-processor). For some stacks, the processor cannot technically receive
the output of the function if the function is invoked as an event. (That's why lambda sidecars should observe
the `synchronicity` parameter.)

The return value is a json object containing the following fields:
- `unchangedUntil`: optional field; if present specifies the time until which further calls with exactly the same
   inputs will be *idempotent*. Refer to the concept of the idenpotency above;
- `dropHeaders`: the list of addHeaders to be dropped from the request;
- `addHeaders`: the list of addHeaders that need to be set/overridden in the
   communication to the API back-end (in case of pre-processor) or to the API client
   (in case of post-processor);
- `changeRoute`: change the changeRoute to the API origin. Meaningful only in the context of the pre-processor;
- `payload`: the content to be sent to the API back-end (in case of pre-processor) or to 
   the API client (in case of post-processor);
- `json`: A convenience field that will perform the following operations:
   - Set the `content-type` header to `application/json`. (If the `addHeader` specifies any other `contnet-type`,
     the latter will be used.);
   - Set the `conent-encoding` to `gzip`;
   - Render the JSON to the string and will send it to the client.
   
   The motivation to have this supported is to alleviate the lambda sidecar developers from performing a 
   boilerplate operations of encoding JSON objects into correct payload.
   
   If both `payload` and `json` fields  
   are provided in the same response, then the value supplied in the `payload` field is taken to send to the 
   API origin/API client, and the `json` field is ignored;
-  `code`: the response code to be sent. At the pre-processor stage, can also be accompanied by
- `message`: an error message to be printed.
   
### `dropHeaders` element
The `dropHeaders` element is a case-insensitive list of headers that need to be removed from the API request to
the API provider or form the response to the API client.

```json
{
  "dropHeaders": [
    "x-header-1", "x-header-2"
  ]
}
```

### `addHeaders` element
The `addHeaders` element specifies the headers to be added to the API origin (in case of pre-processor) or
to the API client's response in the event of the post-processor.

```json
{
  "addHeaders": {
    "X-Filtered-By": "Lambda.V23.R33"
  }
}
```

### `changeRoute` element
The `changeRoute` element supports two elements:

```json
{
  "changeRoute": {
    "uri": "https://the-full-host-name/a/path/to/op?qeury#andFragment",
    "host": "the-full-host-name",
    "file": "/a/path/to/op?withQueryString=true",
    "port": 8443,
    "httpVerb": "post"
  }
}
```

The lambda sidecar should return only the fields that it requires changing: either the URI of the API origin or an
HTTP verb that should be used to make a call.

The purpose of `host`, `port`, and `file` elements is to allow the functoin to easily override individual elements
witout reuqesting receiving the details of the complete operation. For example, if the intention of the lambda
function is to route certain applications to a specific back-end, then the function can return a short-cut as
```json
{
  "changeRoute": {
    "host": "a.super.custom.host"
  }
}
```
   
   
## Lambda sidecar's output handling at request pre-processing
Where the lambda sidecar is invoked from a pre-processor in a request-response way, then it is the presence of the
`code` field determines if the processing of the request should be stopped and an error returned.

If the response provides the `code` field, then the body of the response is formed according to the following rules:
1. Only `code` is provided. In this case, the message that will be sent to the API client will read `<h1>Service cannot
   be provided, code 0x000003BB</h1>`;
2. If `code` and `message` are provided, then the response sent to the API client will read `<h1>${message}</h1>`;
3. If `code` and `payload` are provided, then the payload is sent;
4. If `code` and `json` are provided, then the value of the JSON response is sent.

If `code` field is not present, then the fields `payload` and `json` tell that the payload going to the API origin
has to be replaced with the supplied one (e.g. after user input has been sanitized).    
 
## Error message if configuration is incorrect
In case the endpoint configuration contains errors that prevents it from being parsed correctly, then
the following response should be given to all calls of the endpoint:

- Status code: 596
- Content-Type: `application/xml`
- Message: `<h1>Service not ready, code 0x000003BB</h1>`; 

## Error Messages in Sure-Fire mode
If the processor is configured to work in the sure-fire mode, and the invocation of the lambda sidecar will fail,
the following response will be returned to the API client:
- Status code: 500
- Content-Type: `application/xml`
- Message: 
    - for pre-process point: `<h1>Internal server error before processing the call, code 0x000003BB</h1>`; 
    - for post-process point: `<h1>Internal server error before sending the reseponse, code 0x000003BB</h1>`; 
   
 
 ## Lambda sidecar output examples
 ### Do nothings response
 In case no modification of data to be forwarded by Mashery is required, an empty 
 response should just be returned.
 ```json
{ }
```

### Idempotent response
 To indicate that further calls will be idempotent, supply the `unchangedUntil` field indicating the exact time.
 ```json
{ 
  "unchangedUntil": "2019-10-23T23:35:06Z"
}
```
 ### Add Headers for Back-End
 In this example, addHeaders are dropped and added.
 ```json
{
  "dropHeaders": ["Authorization"],
  "addHeaders": {
    "X-LBackend-Mode": "Preventive",
    "X-LBackend-Grade": "Warning"
  }
}
```         
### Returning error to the client
 ```json
{
  "code": 403
}
``` 

### Returning error to the client with custom payload
In this example, a Lambda sidecar is deployed to control a combination of parameters
of specific 
 ```json
{
  "code": 403,
  "payload": "<h1>Combination of parameters is not allowed<h1>"
}
```         

### Stop data being sent at post-processing
In this case, a lambda sidecar tells to send a 500 code to the user while using the
JSON object as the custom message.
```json
{
  "code": 500,
  "json": {
    "message": "Malformed data in response"
  }
}
```

# API Client Troubleshooting Instructions
The error message generated by the processor are kept obscure to minimize the potential surface of attack and to make 
probing into the pre-conditions as difficult as possible.



The documentation given to the supported clients should indicate the following steps the API client should follow:
1. The request should be checked to be below the required maximum payload size;
2. The request should include all headers the documentation describes;
3. Where the above conditions are not met, the API client should contact the AFKLM application manager stating:
- The URL where the fault is occurring,
- A package key used to make the API call;
- A curl request illustrating the above.

Upon receipt of such support case, a community manager would typically update the EAVs of the associated application
to match the requirements. 
