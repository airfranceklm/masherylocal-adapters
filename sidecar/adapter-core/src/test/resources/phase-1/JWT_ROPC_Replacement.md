# JWT Replacing ROPC and AC tokens

* [Description](#desc)
* [Scenario 1: Authorization granted](#scenario-1)
  * [Pre-processing](#scenario-1-pre)
    * [Consumer request](#scenario-1-request)
    * [Pre-processing sidecar config](#scenario-1-pre-config)
    * [Pre-processing sidecar input](#scenario-1-pre-input)
    * [Pre-processing sidecar output](#scenario-1-pre-output)
    * [Request to provider](#scenario-1-provider-request)
* [Scenario 2: Authorization granted with additional headers](#scenario-2)
  * [Pre-processing](#scenario-2-pre)
    * [Consumer request](#scenario-2-request)
    * [Pre-processing sidecar config](#scenario-2-pre-config)
    * [Pre-processing sidecar input](#scenario-2-pre-input)
    * [Pre-processing sidecar output](#scenario-2-pre-output)
    * [Request to provider](#scenario-2-provider-request)
* [Scenario 3: Authorization denied  due to Incomplete request](#scenario-3)
  * [Pre-processing](#scenario-3-pre)
    * [Consumer request](#scenario-3-request)
    * [Pre-processing sidecar config](#scenario-3-pre-config)
    * [Response to consumer](#scenario-3-response)
* [Scenario 4: Authorization denied due to Invalid token](#scenario-4)
  * [Pre-processing](#scenario-4-pre)
    * [Consumer request](#scenario-4-request)
    * [Pre-processing sidecar config](#scenario-4-pre-config)
    * [Pre-processing sidecar input](#scenario-4-pre-input)
    * [Pre-processing sidecar output](#scenario-4-pre-output)
    * [Request to provider](#scenario-4-provider-request)
* [Scenario 5: Sidecar throws an error](#scenario-5)
  * [Pre-processing](#scenario-5-pre)
    * [Consumer request](#scenario-5-request)
    * [Pre-processing sidecar config](#scenario-5-pre-config)
    * [Pre-processing sidecar input](#scenario-5-pre-input)
    * [Pre-processing sidecar output](#scenario-5-pre-output)
    * [Response to consumer](#scenario-5-response)

---

##  Description <a name="desc"></a>
This sidecar allows for AFKL to replace ROPC and AC tokens with JWT tokens that are issued by the authorization servers.
It is a pre-processing sidecar that may block the consumer from calling the provider.

---

## Scenario 1: Authorization granted <a name="scenario-1"></a>
The sidecar is able to perform  the necessary authentication and then the request will be forwarded by Mashery as normal.

### Pre-processing <a name="scenario-1-pre"></a>
##### Consumer request <a name="scenario-1-request"></a>
```curl
curl -X GET \
  http://api.afkl.com/airplane \
  -H 'Authorization: token'
```

##### Pre-processing sidecar config <a name="scenario-1-pre-config"></a>
```
synchronicity: request-response
elements: remoteAddress
require-request-headers: Authorization
require-eavs: Public_Key
sidecar-param-B2C: "false"
sidecar-param-B2E: "true"
sidecar-param-depts: "KLC|ITDCC"
```

##### Pre-processing sidecar input <a name="scenario-1-pre-input"></a>
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "127.0.0.1",
    "request":{
        "headers":{
            "Authorization": "token"
        }
    },
    "eavs":{
        "Public_Key":"a"
    },
    "params": {
        "B2C": "false",
        "B2E":"true",
        "depts":"KLC|ITCDCC"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-1-pre-output"></a>
```json
{}
```

##### Request to provider <a name="scenario-1-provider-request"></a>
```curl
curl -X GET \
  http://provider.afkl.com/airplane 
```
---


## Scenario 2: Authorization granted with additional headers <a name="scenario-2"></a>
The sidecar grants access; however it also flags how the request should be handled by passing the additional headers in the response. 
Mashery will add these header to the request forwarded to the provider.

### Pre-processing <a name="scenario-2-pre"></a>
##### Consumer request <a name="scenario-2-request"></a>
```curl
curl -X PUT \
  http://api.afkl.com/airplane \
  -H 'Authorization: token'
```

##### Pre-processing sidecar config <a name="scenario-2-pre-config"></a>
```
synchronicity: request-response
elements: remoteAddress
require-request-headers: Authorization
require-eavs: Public_Key
sidecar-param-B2C: "false"
sidecar-param-B2E: "true"
sidecar-param-depts: "KLC|ITDCC"
```

##### Pre-processing sidecar input <a name="scenario-2-pre-input"></a>
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "127.0.0.1",
    "request":{
        "headers":{
            "Authorization": "token"
        }
    },
    "eavs":{
        "Public_Key":"a"
    },
    "params": {
        "B2C": "false",
        "B2E":"true",
        "depts":"KLC|ITCDCC"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-2-pre-output"></a>
```json
{
    "modify" : {
         "addHeaders" : {
            "x-afklm-assurance": "3",
            "x-afklm-channel": "trusted"
         }
    }
}
```

##### Request to provider <a name="scenario-2-provider-request"></a>
```curl
curl -X PUT \
  http://provider.afkl.com/airplane \
  -H 'x-afklm-assurance": "3"' \
  -H 'x-afklm-channel": "trusted'
```

----


## Scenario 3: Authorization denied  due to Incomplete request<a name="scenario-3"></a>
The Pre-Processor rejects the request due to missing EAV or Authorization header
Mashery will return an error to the consumer and not forward the call to the provider.

### Pre-processing <a name="scenario-3-pre"></a>
##### Consumer request <a name="scenario-3-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  H 'Authorization: token'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-3-pre-config"></a>
```
synchronicity: request-response
elements: remoteAddress
require-request-headers: Authorization
require-eavs: Public_Key
sidecar-param-B2C: "false"
sidecar-param-B2E: "true"
sidecar-param-depts: "KLC|ITDCC"
```

##### Response to consumer <a name="scenario-3-response"></a>
```http request
HTTP/1.1 403 FORBIDDEN
Date: Mon, 1 Jan 2000 12:00:00 GMT
Content-Length: 49
Content-Type: application/xml
Connection: Closed
<h1>
 <![CDATA[CDATA[Request pre-condition not met, code 0x000003BB]]>
</h1>
```


----


## Scenario 4: Authorization denied due to Invalid token <a name="scenario-4"></a>
The sidecar denies the Authorization due to JWT validaiton failure

### Pre-processing <a name="scenario-4-pre"></a>
##### Consumer request <a name="scenario-4-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  -H 'Authorization: token'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-4-pre-config"></a>
```
synchronicity: request-response
elements: remoteAddress
require-request-headers: Authorization
require-eavs: Public_Key
sidecar-param-B2C: "false"
sidecar-param-B2E: "true"
sidecar-param-depts: "KLC|ITDCC"
```

##### Pre-processing sidecar input <a name="scenario-4-pre-input"></a>
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "127.0.0.1",
    "request":{
        "headers":{
            "Authorization": "token"
        }
    },
    "eavs":{
        "Public_Key":"a"
    },
    "params": {
        "B2C": "false",
        "B2E":"true",
        "depts":"KLC|ITCDCC"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-4-pre-output"></a>
```json
{
    "terminate" : {
        "code" : 445,
        "message" : "Your department is not authorized to access this data"
    }
}
```

##### Response to consumer <a name="scenario-4-response"></a>
```http request
HTTP/1.1 445 
Date: Mon, 1 Jan 2000 12:00:00 GMT
Content-Length: 49
Content-Type: application/xml
Connection: Closed
<h1>
  <![CDATA[Your department is not authorized to access this data]]><
</h1>
```


----

## Scenario 5: Sidecar throws an error <a name="scenario-5"></a>
The sidecar faces an unknown error and returns an Internal Server Error to the pre-processor.
As the synchronicity is request-response, Mashery will return an error to the consumer and not forward the call to the provider.

### Pre-processing <a name="scenario-5-pre"></a>
##### Consumer request <a name="scenario-5-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  -H 'Authorization: token'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-5-pre-config"></a>
```
synchronicity: request-response
elements: remoteAddress
require-request-headers: Authorization
require-eavs: Public_Key
sidecar-param-B2C: "false"
sidecar-param-B2E: "true"
sidecar-param-depts: "KLC|ITDCC"
```

##### Pre-processing sidecar input <a name="scenario-5-pre-input"></a>
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "127.0.0.1",
    "request":{
        "headers":{
            "Authorization": "token"
        }
    },
    "eavs":{
        "Public_Key":"a"
    },
    "params": {
        "B2C": "false",
        "B2E":"true",
        "depts":"KLC|ITCDCC"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-5-pre-output"></a>
```http request
HTTP/1.1 500 BAD REQUEST
Date: Mon, 1 Jan 2000 12:00:00 GMT
Connection: Closed

```

##### Response to consumer <a name="scenario-5-response"></a>
```http request
HTTP/1.1 500 BAD REQUEST
Date: Mon, 1 Jan 2000 12:00:00 GMT
Content-Length: 49
Content-Type: application/xml
Connection: Closed
<h1>
  Internal server error before processing the call, code 0x000003BB
</h1>
```


----

