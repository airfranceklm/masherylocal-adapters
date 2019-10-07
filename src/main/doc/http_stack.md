# HTTP Stack
The HTTP stack is used to deliver the sidecar input as a JSON-encoded payload to the URL that is provided in the 
end point's configuration. 

## Lambda input location

The input is submitted in the HTTP request body is with the following headers:
```text
Accept: application/json
Accept-Charset: utf-8
Accept-Encoding: gzip
Content-Type: application/json; charset=UTF-8
Content-Encoding: gzip
```
  
The response to be returned shall meet the following parameters:
- the `content-type` will be `application-json`. A `gzip` `content-ecoding` will be supported;
- the payload shall be JSON-encoded object compatible with sidecar output.

The following Open API specification gives a formal description to input and output parameters. (You can also
see the specification on the [Swagger Hub](https://app.swaggerhub.com/apis/aleks.amt/afklm-lambda-sidecar-httpstack/1.0)
```yaml
openapi: 3.0.0
info:
  version: '1.0'
  title: 'AFKLM sidecar HTTP Stack API'
  description: 'Air France/KLM HTTP sidecar API'


components:
  schemas:
    AnyValue:
      description: Any simple or complex object
      nullable: true
      
    SidecarOutputRouting:
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
          
    SidecarOutput:
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
          $ref: '#/components/schemas/SidecarOutputRouting'
        
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
      description: AFKLM sidecar invocation.
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LambdaRequest'
      responses:
        '200':
          $ref: '#/components/schemas/SidecarOutput'
        '202':
          description: Function invoked asynchronously.
          
```


## Configuring HTTP stack
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