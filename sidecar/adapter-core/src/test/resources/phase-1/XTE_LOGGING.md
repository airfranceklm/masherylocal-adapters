# XTE LOGGING SIDECAR

* [Description](#desc)
* [Scenario 1: pass-along without modifications](#scenario-1)
  * [Post-processing](#scenario-1-post)
    * [Consumer request](#scenario-1-request)
    * [Post-processing sidecar config](#scenario-1-post-config)
    * [Post-processing origin response](#scenario-1-post-origin)
    * [Post-processing sidecar input](#scenario-1-post-input)
    * [Post-processing sidecar output](#scenario-1-post-output)
    * [Request to provider](#scenario-1-provider-request)
* [Scenario 2: rejecting delivery to API client](#scenario-2)
  * [Post-processing](#scenario-2-post)
    * [Consumer request](#scenario-2-request)
    * [Post-processing sidecar config](#scenario-2-post-config)
    * [Post-processing origin response](#scenario-2-post-origin)
    * [Post-processing sidecar input](#scenario-2-post-input)
    * [Post-processing sidecar output](#scenario-2-post-output)
    * [Expect Traffic Manager](#scenario-2-traffic-manager)
* [Scenario 3: invocation failure](#scenario-3)
  * [Post-processing](#scenario-3-pre)
    * [Consumer request](#scenario-3-request)
    * [Post-processing sidecar config](#scenario-3-post-config)
    * [Post-processing origin response](#scenario-3-post-origin)
    * [Post-processing sidecar input](#scenario-3-post-input)
    * [Post-processing sidecar output](#scenario-3-post-output)
    * [Expect Traffic Manager](#scenario-3-traffic-manager)
* [Scenario 4: pass-along with sanitization](#scenario-4)

---

##  Description <a name="desc"></a>
Security logging and inspection for the eXtenal Test Environment (XTE). The sidecar inspects, logs, and, if
    necessary, will override the response with sanitized inputs.

---

## Scenario 1: pass-along without modifications <a name="scenario-1"></a>


### Post-processing <a name="scenario-1-post"></a>
##### Consumer request <a name="scenario-1-request"></a>
```curl
curl -X GET \
   https://api.airfranceklm.com/sampleApi/v2/controlled-operation/param/search?myQuery=p123 \
  -H 'Authorization: ==B=='
```

##### Post-processing sidecar config <a name="scenario-1-post-config"></a>
```
synchronicity: request-response
elements: remoteAddress,operation,token,+responseHeaders,responsePayload
```

##### Post-processing api origin response <a name="scenario-1-post-origin"></a>

```
statusCode: 201
headers:
 	RA: RB
	Content-Type: text/plain
body: 
	==PAYLOAD==
```     

##### Post-processing sidecar input <a name="scenario-1-post-input"></a>
```json
{
    "point": "PostProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "1.2.3.4",
    "operation": {
    	"uri": "https://api.airfranceklm.com/sampleApi/v2/controlled-operation/param/search?myQuery=p123",
        "httpVerb": "GET",
        "path": "/controlled-operation/param/search",
        "query": {
        	"myQuery": "p123"
        }
	},
    "response": {
    	"code": "201",
        "headers": {
        	"ra": "RB",
            "content-type": "text/plain"
        },
        "payload": "==PAYLOAD=="
	}
}
```

##### Post-processing sidecar output <a name="scenario-1-post-output"></a>
```json
{}
```
empty object, indicating that no action is needed

##### Request to provider <a name="scenario-1-provider-request"></a>
```curl
curl -X GET \
  https://docker.kml/backend/url?myQuery=ffff
```
---


## Scenario 2: rejecting delivery to API client <a name="scenario-2"></a>


### Post-processing <a name="scenario-2-post"></a>
##### Consumer request <a name="scenario-2-request"></a>
```curl
curl -X PUT \
  https://api.airfranceklm.com/sampleApi/v2/controlled-operation/param/search?myQuery=p123 \
  -H 'Authorization: ==B=='
```

##### Post-processing sidecar config <a name="scenario-2-post-config"></a>
```
synchronicity: request-response
elements: remoteAddress,operation,token,+responseHeaders,responsePayload
```

##### Post-processing api origin response <a name="scenario-2-post-origin"></a>

```
statusCode: 201
headers:
 	RA: RB
	Content-Type: text/plain
body: 
	==PAYLOAD==
```    

##### Post-processing sidecar input <a name="scenario-2-post-input"></a>
```json
{
    "point": "PostProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    "remoteAddress": "1.2.3.4",
    "operation": {
    	"uri": "https://api.airfranceklm.com/sampleApi/v2/controlled-operation/param/search?myQuery=p123",
        "httpVerb": "GET",
        "path": "/controlled-operation/param/search",
        "query": {
        	"myQuery": "p123"
        }
	},
    "response": {
    	"code": "201",
        "headers": {
        	"ra": "RB",
            "content-type": "text/plain"
        },
        "payload": "==PAYLOAD=="
	}
}
```

##### Post-processing sidecar output <a name="scenario-2-post-output"></a>
```json
{
    "code" : "465",
    "message" : "Payload violates defined confidentiality policy"
}
```

##### Expect traffic manager <a name="scenario-2-traffic-manager"></a>
```
set complete: true
status code: 465
headers:
	content-type: application/xml
body: "<h1><![CDATA[Payload violates defined confidentiality policy]]></h1>"
```

----


## Scenario 3: invocation failure <a name="scenario-3"></a>


### Post-processing <a name="scenario-3-post"></a>
##### Consumer request <a name="scenario-3-request"></a>
```curl
curl -X PUT \
  https://api.airfranceklm.com/sampleApi/v2/controlled-operation/param/search?myQuery=p123 \
  -H 'Authorization: ==B=='
```


##### Post-processing sidecar config <a name="scenario-3-post-config"></a>
```
synchronicity: request-response
elements: remoteAddress,operation,token,+responseHeaders,responsePayload
```

##### Post-processing api origin response <a name="scenario-3-post-origin"></a>

```
statusCode: 201
headers:
 	RA: RB
	Content-Type: text/plain
body: 
	==PAYLOAD==
```    


##### Post-processing sidecar input <a name="scenario-3-post-input"></a>
```json
{
    "point": "PostProcessor",
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

##### Post-processing sidecar output <a name="scenario-3-post-output"></a>
```
{
    throw error: Unexpected return code
}
```

##### Expect traffic manager <a name="scenario-3-traffic-manager"></a>
```
set complete: true
status code: 500
headers:
	content-type: application/xml
body: "<h1><![CDATA[Internal server error before sending the response, code 0x000003BB]]></h1>"
```


----


## Scenario 4: pass-along with sanitization <a name="scenario-4"></a>


TO BE DEFINED