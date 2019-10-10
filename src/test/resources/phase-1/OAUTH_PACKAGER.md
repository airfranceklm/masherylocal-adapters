# OAUTH PACKAGER SIDECAR

* [Description](#desc)
* [Scenario 1: Access is granted](#scenario-1)
  * [Pre-processing](#scenario-1-pre)
    * [Consumer request](#scenario-1-request)
    * [Pre-processing sidecar config](#scenario-1-pre-config)
    * [Pre-processing sidecar input](#scenario-1-pre-input)
    * [Pre-processing sidecar output](#scenario-1-pre-output)
    * [Request to provider](#scenario-1-provider-request)
* [Scenario 2: Access is granted with additional headers](#scenario-2)
  * [Pre-processing](#scenario-2-pre)
    * [Consumer request](#scenario-2-request)
    * [Pre-processing sidecar config](#scenario-2-pre-config)
    * [Pre-processing sidecar input](#scenario-2-pre-input)
    * [Pre-processing sidecar output](#scenario-2-pre-output)
    * [Request to provider](#scenario-2-provider-request)
* [Scenario 3: Access is denied](#scenario-3)
  * [Pre-processing](#scenario-3-pre)
    * [Consumer request](#scenario-3-request)
    * [Pre-processing sidecar config](#scenario-3-pre-config)
    * [Pre-processing sidecar input](#scenario-3-pre-input)
    * [Pre-processing sidecar output](#scenario-3-pre-output)
    * [Response to consumer](#scenario-3-response)
* [Scenario 4: Sidecar throws an error](#scenario-4)
  * [Pre-processing](#scenario-4-pre)
    * [Consumer request](#scenario-4-request)
    * [Pre-processing sidecar config](#scenario-4-pre-config)
    * [Pre-processing sidecar input](#scenario-4-pre-input)
    * [Pre-processing sidecar output](#scenario-4-pre-output)
    * [Response to consumer](#scenario-4-response)

---

##  Description <a name="desc"></a>
This sidecar allows for AFKL to perform OAuth checks on package, plan, or application level. 
It is a pre-processing sidecar that may block the consumer from calling the provider.

---

## Scenario 1: Access is granted <a name="scenario-1"></a>
The sidecar is able to obtain the necessary authentication and the request will be forwarded by Mashery as normal.

### Pre-processing <a name="scenario-1-pre"></a>
##### Consumer request <a name="scenario-1-request"></a>
```curl
curl -X GET \
  http://api.afkl.com/airplane \
  -H 'apiKey: myKey'
```

##### Pre-processing sidecar config <a name="scenario-1-pre-config"></a>
```
synchronicity: request-response
expand-input: verb,grantType
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
    "operation": {
        "httpVerb": "GET"
    },
    "token": {
        "grantType": "client_credentials"
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
  http://provider.afkl.com/airplane \
  -H 'apiKey: myKey'
```
---


## Scenario 2: Access is granted with additional headers <a name="scenario-2"></a>
The sidecar grants access; however it also flags how the request should be handled by passing the additional headers in the response. 
Mashery will add these header to the request forwarded to the provider.

### Pre-processing <a name="scenario-2-pre"></a>
##### Consumer request <a name="scenario-2-request"></a>
```curl
curl -X PUT \
  http://api.afkl.com/airplane \
  -H 'apiKey: myKey'
```

##### Pre-processing sidecar config <a name="scenario-2-pre-config"></a>
```
synchronicity: request-response
expand-input: verb,grantType
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
    "operation": {
        "httpVerb": "PUT"
    },
    "token": {
        "grantType": "client_credentials"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-2-pre-output"></a>
```json
{
    "modify" : {
         "addHeaders" : {
              "x-afklm-allow" : "PNR,LastName,Email",
              "x-afklm-security-logging" : "FULL"
         }
    }
}
```

##### Request to provider <a name="scenario-2-provider-request"></a>
```curl
curl -X PUT \
  http://provider.afkl.com/airplane \
  -H 'apiKey: myKey' \
  -H 'x-afklm-allow: PNR,LastName,Email' \
  -H 'x-afklm-security-logging: FULL'
```

----


## Scenario 3: Access is denied <a name="scenario-3"></a>
The sidecar denies access, e.g. because the POST method is not supported for this endpoint. 
Mashery will return an error to the consumer and not forward the call to the provider.

### Pre-processing <a name="scenario-3-pre"></a>
##### Consumer request <a name="scenario-3-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  -H 'apiKey: myKey'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-3-pre-config"></a>
```
synchronicity: request-response
expand-input: verb,grantType
```

##### Pre-processing sidecar input <a name="scenario-3-pre-input"></a>
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "operation": {
        "httpVerb": "POST"
    },
    "token": {
        "grantType": "client_credentials"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-3-pre-output"></a>
```json
{
    "terminate" : {
        "code" : 403,
        "message" : "POST not permitted for your plan"
    }
}
```

##### Response to consumer <a name="scenario-3-response"></a>
```http request
HTTP/1.1 403 FORBIDDEN
Date: Mon, 1 Jan 2000 12:00:00 GMT
Content-Length: 49
Content-Type: application/xml
Connection: Closed
<h1>
 Operation not permitted for your plan
</h1>
```


----


## Scenario 4: Sidecar throws an error <a name="scenario-4"></a>
The sidecar faces an unknown error and returns an Internal Server Error to the pre-processor.
As the synchronicity is request-response, Mashery will return an error to the consumer and not forward the call to the provider.

### Pre-processing <a name="scenario-4-pre"></a>
##### Consumer request <a name="scenario-4-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  -H 'apiKey: myKey'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-4-pre-config"></a>
```
synchronicity: request-response
expand-input: verb,grantType
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
    "operation": {
        "httpVerb": "POST"
    },
    "token": {
        "grantType": "client_credentials"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-4-pre-output"></a>
```http request
HTTP/1.1 500 BAD REQUEST
Date: Mon, 1 Jan 2000 12:00:00 GMT
Connection: Closed

```

##### Response to consumer <a name="scenario-4-response"></a>
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
