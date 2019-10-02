# Sidecar processor configuration
The processor applies separate configurations for pre-processing and post-processing. This
allows the deployer to choose appropriate strategies depending on the functional need 
of an endpoint and the performance considerations. Also, pre- and post-processing can be enabled separately, depending
on the needs.

A pre- and post-processing requires specifying the stack, necessary configuration parameters of the stack, and 
identification of the sidecar that has to be invoked.

Where no other specific parameters are specified, the following settings are assumed:
- The processor will observe the sure-fire error handling policy for this invocation (a call function call must succeed
  in order for the API client to receive a non-error response);
- The sidecar is invoked as a synchronous event invocation;
- Maximum payload size will be set to 50mb.

The deployer can choose between two options of how a sidecar can be configured:
- via centralized Mashery configuration, by providing key-value properties in the pre- and post-processor window. This
  configuration is preferred where the configuration paramaters are rather simple;
- via distributing a set of configuration files to the Mashery machines handling the traffic. This will be preferred
  where the configuration has to supply a lot of parameters 

Configuring these steps is explained in the following sections.

---


If a sidecar requires processing the payload, then the design of the processor requires setting
the maximum payload size foe the endpoint which the API client should not reasonably exceed. The purpose of this is 
to prevent service degradation arising from submitting the "payload bombs" to the sidecar. By default, 
this limit is set at 50 
kilo-bytes. This limit can be configured per endpoint to reflect the sidecar's purpose.

The format of the configuration property is as follows:
```properties
max-payload-size=size[k|m]b,([blocking|filtering])
```

The limit can be configured to apply either:
- as filtering condition: the sidecar shall not be executed on such payloads, while the request itself should 
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


The condition determines what the processor should do if the a payload at hand will exceed the defined limit. There
are two mode:
- in `blocking` mode, the processor should block the call and return an error to the API client, while in 
- `filtering` mode, the processor will ignore payloads that exceed the maximum size.

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
