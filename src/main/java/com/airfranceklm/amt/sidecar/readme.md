[func]: ../../../../../doc/sidecar-functional-flow.png "sidecar flow"

# Air France/KLM Mashery sidecar Processor for Mashery Local

The Air France/KLM Mashery Local processor allows injecting pre- and post-processing logic from the function running in
AWS, local HTTP servers, or any other supported communication stack.

## What is a sidecar?

The Air France/KLM Mashery sidecar (referred to hereinafter as *sidecar*) is a piece of a logic running externally 
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

The design of this mechanism is largely inspired by the AWS Lambda service. However, it is *not* called (at least, in 
Air Frane/KLM documentation) "lambda" because the implementation of the pre-processor is not limited to the AWS Lambda.
A pluggable transport interfaces is supported. It allows 
integrating any network source that provide compatible responses as a sidecar. If you run Mashery Local on-premise, then 
HTTP stack (and eventually OpenFAAS stack) is provided out-of-the-box. Organization-specific transports 
[can be added](../../../../../doc/extending_transport.md)
via extending the provided codebase, such as e.g. to include RMI or Kafka interfaces.

## Supported stacks
Most of the sidecars will be reachable via HTTP calls, with an option to make call-outs to
AWS Lambda functions. HTTP is the default invocation stack.

See the [stacks page](../../../../../doc/stacks.md) for the information about currently supported stacks. 

# Jump-starting into the sidecar development
Sidecars are simple and quick to develop. Yet, as a component in a distributed system, it requires a good understnading
from the developer on what he is doing and why he is doing things. Please following this track to get started:

1. Familiarize yourself with [key concepts](../../../../../doc/key_concepts.md). After reading this note, you should
   understand terms *idempotent call*, *pre-flight check*, and *error handling policy*. These are essential for the 
   next step:
2. Read our [design guidelines](../../../../../doc/designing-sidecar.md). This page explains why you do need and,
   most importantly, why you **do not** need a sidecar;
3. Get inspired with our [solution showcases](../../../../../doc/showcases.md). If you are trying to solve problem
   that is similar to the one we illustrate, you can use these examples for your inspiration.
4. You still believe sidecar is a good choice for you? Awesome! You need to [install](../../../../../doc/install.md)
   so that you can [play around and do some PoC's](../../../../../doc/first_steps.md).
5. Now, finally, it's time for the heavy stuff:
   - [Sidecar input and output](../../../../../doc/sidecar_io.md) describes inputs the sidecar can receive and
     the outputs it will provide;
   - Code and deploy your use case for testing. 
   - Configure the endpoint using [in-Mashery configuration](../../../../../doc/mashery_config.md). Additionally,
     if you need a hybrid deployment, you need to figure out how to [override Mashery configuration locally](../../../../../doc/local_config.md).
   - Test your setup.
   - Fix bugs, test again, promote to live.
   - Repeat.
6. Get in touch with Air France/KLM API Management Team via TIBCO Customer Success Management to share your story.
   We are really curious what you've built with your development.

# Sidecar working explained 

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
  2.  The processor checks if the request is in the sidecar scope (explained below). If the request is outside 
      of scope, no action is taken. This measure is applied to enhance the
      response time by avoiding calling functions on requests that are not necessary (as well as to conserve any associated
      costs of running unnecessary workloads in the cloud);
  3. The processor checks if need to assert the request meeting the condition. If the condition is not met, 
      then an HTTP error 400 is returned back to the client.
      If the pre-conditions are met, then the sidecar pre-processing is invoked.

    When the conditions explained at the Point B are met, then the processor will:
    1. Invoke the specified sidecar according to the desired invocation mode (request-response or asynchronous);
    2. Receive response from the sidecar stack hand handle the invocation error, should it occur (either according to fail-safe 
   or sure-fire error handling policy).
    3. In request-response API origin is modified, according ot the instructions returned from the sidecar. 

- At this point, the pre-processing ends.
    4. Mashery send the (modified) request to the API origin host;
    5. API origin host sends a reply;

- Point C: Mashery checks if the post-processing is enabled for this API. If it is, it will request the processor
  to perform the post-processing of the request. At this point, the following actions will be pefromed:
   1. The processor ensures that the configuration of the endpoint post-processing is valid. An invalid configuration (e.g. not 
     supplying parameters to invoke the sidecar stack) will result in an `500 Internal Server Error` being sent to the 
     calling client.
   2. The processor checks if the request is in the sidecar scope (explained below). If the request is outside 
   of scope, no action is taken (for the same reasoning as in pre-processing).
   
   If the API call is in scope, then process will:
   1. Invoke the specified sidecar according to the desired invocation mode (request-response or asynchronous);
   2. Receive response from the sidecar stack hand handle the invocation error, should it occur (either according to fail-safe 
   or sure-fire error handling policy).
   3. In request-response, the response sent to the API client is modified, according ot the instructions returned 
   from the sidecar.
   
- Point D: a response is returned to the calling client, with or without modifications from the sidecar(s).
   

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
