# INSTANT LOGOFF SIDECAR

* [Description](#desc)
* [Scenario 1: Access is granted](#scenario-1)
  * [Pre-processing](#scenario-1-pre)
    * [Consumer request](#scenario-1-request)
    * [Pre-processing sidecar config](#scenario-1-pre-config)
    * [Pre-processing sidecar input](#scenario-1-pre-input)
    * [Pre-processing sidecar output](#scenario-1-pre-output)
    * [Request to provider](#scenario-1-provider-request)
* [Scenario 2: Access is denied](#scenario-2)
  * [Pre-processing](#scenario-2-pre)
    * [Consumer request](#scenario-2-request)
    * [Pre-processing sidecar config](#scenario-2-pre-config)
    * [Pre-processing sidecar input](#scenario-2-pre-input)
    * [Pre-processing sidecar output](#scenario-2-pre-output)
    * [Response to consumer](#scenario-2-response)
* [Scenario 3: Sidecar throws an error](#scenario-3)
  * [Pre-processing](#scenario-3-pre)
    * [Consumer request](#scenario-3-request)
    * [Pre-processing sidecar config](#scenario-3-pre-config)
    * [Pre-processing sidecar input](#scenario-3-pre-input)
    * [Pre-processing sidecar output](#scenario-3-pre-output)
    * [Response to consumer](#scenario-3-response)

---

##  Description <a name="desc"></a>
This sidecar allows for AFKL to  to stop all traffic to the API endpoint with the tokens that were created before a
cutover point. This may be necessary e.g. to ensure the critical downtime for system migration.

It is a pre-processing sidecar that may block the consumer from calling the provider.

---

## Scenario 1: Access is granted <a name="scenario-1"></a>
The sidecar checks the token expiry time and grants access if the token is operable outside of the cutover window.

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
expands-input: token
sidecar-param-upToTier: 2
sidecar-param-cutoverStart: 20200202T14:00:00Z
sidecar-param-cutoverAllowAfter: 20200202T15:00:00Z
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
    "params": {
        "cutoverStart": "20200202T14:00:00Z",
        "cutoverAllowAfter": "20200202T15:00:00Z",
        "upToTier": 2
    },
    "token": {
        "grantType": "password",
        "userContext": {"level-of-assurance": 4, "name": "john", "lastName:" "doe"},
        "expires": "2020-03-24T13:48:09Z",
        "scope": "roles"
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



## Scenario 2: Access is denied <a name="scenario-2"></a>
The sidecar denies access if the token expiry time is within the cutover time. 
Mashery will return an error to the consumer and not forward the call to the provider.

### Pre-processing <a name="scenario-2-pre"></a>
##### Consumer request <a name="scenario-2-request"></a>
```curl
curl -X POST \
  http://api.afkl.com/airplane \
  -H 'apiKey: myKey'
  -d '{ "code": "PH-ABC" }'
```

##### Pre-processing sidecar config <a name="scenario-2-pre-config"></a>
```
synchronicity: request-response
expands-input: token
sidecar-param-upToTier: 2
sidecar-param-cutoverStart: 20200202T14:00:00Z
sidecar-param-cutoverAllowAfter: 20200202T15:00:00Z
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
    "params": {
        "cutoverStart": "20200202T14:00:00Z",
        "cutoverAllowAfter": "20200202T15:00:00Z",
        "upToTier": 2
    },
    "token": {
        "grantType": "password",
        "userContext": {"level-of-assurance": 4, "name": "john", "lastName:" "doe"},
        "expires": "2020-03-14T13:48:09Z",
        "scope": "roles"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-2-pre-output"></a>
```json
{
    "terminate" : {
        "code" : 456,
        "message" : "Service is closed for maintenance"
    }
}
```

##### Response to consumer <a name="scenario-2-response"></a>
```http request
HTTP/1.1 456
Date: Mon, 1 Jan 2000 12:00:00 GMT
Content-Length: 49
Content-Type: application/xml
Connection: Closed
<h1>
 Service is closed for maintenance
</h1>
```


----


## Scenario 3: Sidecar throws an error <a name="scenario-3"></a>
The sidecar faces an unknown error and returns an Internal Server Error to the pre-processor.
As the synchronicity is request-response, Mashery will return an error to the consumer and not forward the call to the provider.

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
expands-input: token
sidecar-param-upToTier: 2
sidecar-param-cutoverStart: 20200202T14:00:00Z
sidecar-param-cutoverAllowAfter: 20200202T15:00:00Z
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
    "params": {
        "cutoverStart": "20200202T14:00:00Z",
        "cutoverAllowAfter": "20200202T15:00:00Z",
        "upToTier": 2
    },
    "token": {
        "grantType": "password",
        "userContext": {"level-of-assurance": 4, "name": "john", "lastName:" "doe"},
        "expires": "2020-03-14T13:48:09Z",
        "scope": "roles"
    }
}
```

##### Pre-processing sidecar output <a name="scenario-3-pre-output"></a>
```http request
HTTP/1.1 500 BAD REQUEST
Date: Mon, 1 Jan 2000 12:00:00 GMT
Connection: Closed

```

##### Response to consumer <a name="scenario-3-response"></a>
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
