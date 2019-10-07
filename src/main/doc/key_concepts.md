# Sidecar functionality motivation

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
  
Providing the generic sidecar processor that allows making the call-outs to the sidecars (or similar
resources) achieves the best compromise.

Using the sidecar processing, AFKLM will be able to achieve via a centralized process:
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

# Idempotent sidecar calls
Certain sidecars outputs can be considered idempotent. Idempotence means that:
- calling this sidecar  multiple times with the same input will not change the state after the first invocation was made; and also
- if called repeatedly with identical inputs, the same output will be produced.

The concept of idempotence can be seen as loosely related to caching, yet it has a crucial difference. Caching seeks
to improve the performance of application by shortening the access time to frequently used data. The idempotence
allows the processor avoiding calling sidecars where it is assured that the same response will be received.

In some scenarios, e.g. if the purpose of a sidecar is to implement access logging, a sidecar must be
always called. In others, e.g. where the function authenticates caller, the response might be considered as
*idempotent* for some time, as the authentication with the same credentials should repeatedly succeed.

It is the responsibility of the sidecar to indicate whether an individual response is idempotent, and for how
long. A processor *may* use the indication of idempotence to optimize the running cost of the sidecar, but it does not
guarantee that it *will* actually use it.

# Sidecar invocation modes
The sidecar can be invoked in three different modes:
1. As a request/response invocation, which is useful where the output of the sidecar call will be used 
   to modify a content that is sent to the API provider or to the API consumer. The response time as seen
   by the API consumer will incur communication overhead with the sidecar as well as
   any processing time taken  by the function. This mode ensures that the sidecar has successfully processed
   the necessary input.;
2. As a synchronous event invocation, where is useful where the output of a sidecar will not be used to
   modify the request sent to API provider and/or a response returned to the API consumer. In this mode, the processor 
   will invoke the stack indicating that it is not expecting to receive
   a response.  The response time as seen by the API 
   consumer will incur only the communication overhead over the stack, without waiting for the response from
   the sidecar. It is the responsibility of the stack to execute the sidecar call asynchronously.
   
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
the purpose of the sidecar fits into [eventual consistency](https://en.wikipedia.org/wiki/Eventual_consistency) 
semantics (e.g. usage analytics).

# Sure-fire vs Fail-safe error handling policy
By default, the processor assumes that a successful invocation of a function is critical
for the processing of the API call (a sure-fire error handling policy). Then, should a function fail,
the call processing should also fail.

Such behaviour may be too strong for deployments that implement auxiliary features in
lambdas. To accommodate the integrations with the sidecar that are not critical
for processing the API call, the processor supports the notion of a *fail-safe* error handling policy.
In a fail-safe mode, a failure
of the sidecar will not impact serving the traffic (albeit an inevitable response
time degradation will be observed by the clients).

Where the invocation is *not* fail-safe, then the fault of the lambda invocation will result
in 500 Internal Server Error being sent to the client.

The choice of whether or not to mark an endpoint a fail-safe depends on the nature the
function implements. 

# Pre-flight checks
A pre-flight check is a special type of the sidecar invocation that aims establishing the parameters that need to
apply on all requests going through the given endpoint. Because of the very nature, the pre-flight will occur only
within the scope of the pre-processor.

The  motivation for pre-flights is to:
- retrieve (and cache) the attributes describing the API client application and, possibly, a user of an application. This feature
  is used when two things need to happen with the API call:
    1. A specific information needs to be added to all requests forwarded to the API origin; in combination with
    2. Specific pre-processing (e.g. custom authorization) of particular requests. Pre-flight checks allow optimizing
    the calls to the sidecar by relying on the idempotence indication of the pre-flight checks;
- in case the user must be denied the access to the endpoint across the board, then this can be achieved with a single
  pre-flight invocation, saving multiple successive calls to the sidecars.
  
The pre-flight check is does not consider the specific of the operation.  
 