# Sidecar's output handling at request pre-processing
Where the sidecar is invoked from a pre-processor in a request-response way, then it is the presence of the
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
If the processor is configured to work in the sure-fire mode, and the invocation of the sidecar will fail,
the following response will be returned to the API client:
- Status code: 500
- Content-Type: `application/xml`
- Message: 
    - for pre-process point: `<h1>Internal server error before processing the call, code 0x000003BB</h1>`; 
    - for post-process point: `<h1>Internal server error before sending the reseponse, code 0x000003BB</h1>`; 
   
 
 ## sidecar output examples
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
In this example, a sidecar is deployed to control a combination of parameters
of specific 
 ```json
{
  "code": 403,
  "payload": "<h1>Combination of parameters is not allowed<h1>"
}
```         

### Stop data being sent at post-processing
In this case, a sidecar tells to send a 500 code to the user while using the
JSON object as the custom message.
```json
{
  "code": 500,
  "json": {
    "message": "Malformed data in response"
  }
}
```
