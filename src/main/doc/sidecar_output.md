# Sidecar outputs
The output of the sidecar specifies is the Mashery gateway can proceed with processing an API call at hand. Functionally,
the output answers the question “Can I send this to call to the API Origin backend or to the API Client?” with 
either of three options: 
-	Yes, send in the current form;
-	Yes, applying the modifications listed in the response;
-	No, terminate processing with and provide the error code and message to the API Client.


The output of the sidecar is only meaningful if the function is invoked in the
request/response fashion. The response communicates only what needs to be *changed* before being forwarded to  the 
API origin request (for pre-processor) or  in the API client response (for post-processor).  

For some stacks, the processor cannot technically receive the output of the function if the function is invoked as 
an event. For other stacks, there may be no out-of-the-box support for the asynchronous event invocation. 
In such cases, it is the responsibility of the sidecar functions themselves to observe the 
`synchronicity` parameter.

There are two flavours of the sidecar responses: the pre-processor output and post-processor output.

# Pre-processor Sidecar Output

On a high level, the sidecar can send 4 data elements back to the Mashery gateway at the pre-processor stage:
- `unchangedUntil`: optional field; if present specifies the time until which further calls with exactly the same
     inputs will be *idempotent*;
- `terminate`: instruction to terminate the processing, *if* it is required. The object will contain further data
  that the sidecar processor will use to construct the error message to the API client. The field should be omitted
  if termination of the API call is not required;
- `modify`: instruction to modify the call to the API origin host.
- `relay`: parameters, a key-value map to be relayed to the post-processor. The relay functionality is described
  in the respective guide.

The options `terminate` and `modify` are mutually exclusive. If the sidecar provides one, it should not provide
another. In case both are provided, then the Sidecar process with prefer `terminate` over `modify`.

Below you find the complete JSON schema and explanation about supported elements.  
    
## Complete JSON Schema

```json
{
  "unchangedUntil" : "2019-10-08T12:59:57+0000",
  "terminate" : {
    "payload" : "Custom payload",
    "json" : {
      "a" : "b",
      "c" : "d"
    },
    "base64Encoded" : false,
    "code" : 403,
    "message" : "Termination message",
    "headers" : {
      "x-afklm-error" : "B0-832-J"
    }
  },
  "modify" : {
    "payload" : "Set replacement payload",
    "json" : {
      "a" : "b",
      "c" : "d"
    },
    "base64Encoded" : false,
    "addHeaders" : {
      "x-afklm-level" : "44",
      "x-afklm-bearing" : "326 degrees of inner turbulence"
    },
    "dropHeaders" : [ "authorization", "x-afklm-market" ],
    "changeRoute" : {
      "host" : "newHost",
      "file" : "file?queryString",
      "httpVerb" : "POST",
      "uri" : "http://new-uri:432455/travel/custom/backend",
      "port" : 3455
    },
    "completed": false
  },
  "relay" : {
    "cache-key" : "324kfknkdjkjk5j5",
    "cache-region" : "EU"
  }
}
```

> Note: this schema above is provided to illustrate the position of all fields. Message like this cannot appear.

## Overriding payload convention

In several places in the response, the data structure will communicate the requirement to replace the
body being sent to the destination. On such data structures, the following fields will be used:
- `payload` and `base64Encoded` specify the payload to be sent as an error message, where `base64Encoded` indicates
   whether sidecar processor should Base-64 decode this string to receive the intended byte stream. If the latter
   is omitted or set to `false`, the the processor will treat `payload` as a unicode string.
- `json`: a convenience structure indicating that a JSON string with `content-type` `application/json` needs to be
   sent to the API client (unless `headers` specify other content-type).
   
The fields `payload` and `json` are logically mutually exclusive. If both supplied, the `json` will be 
selected over `payload`.

## The `terminate` object

The terminate object communicates:
- `code`: error code to be sent;
- `headers`: headers to be added to the response message;
- customized error message selected from `message`, `payload`, and `json` fields.

The sidecar should observe these rules for constructing the termination object: 
- the `code` *must* be filled;
- `headers` are optional;
- the sidecar *should* specify customized error message by specifying either:
    - a `message` field, or
    - a `json` field in case the sidecar wants to respect `Accept: application/json` header, or
    - a `payload` field in combination with `base64Encoded`. 
      
## The `modify` object
           
As its name implies, the data in this object is meant to modify the API call before it will be transmitted 
to the destination. The fields in this object are interpreted as follows:
 
- `dropHeaders`: the key-value of addHeaders to be dropped from the request;
- `addHeaders`: the list of addHeaders that need to be set/overridden in the
   communication to the API back-end (in case of pre-processor) or to the API client
   (in case of post-processor);
- `changeRoute`: change the changeRoute to the API origin. 
- `payload` and `base64Encoded` (see the convention above);
- `json`: if specific the following operations:
   - Set the `content-type` header to `application/json`. (If the `addHeader` specifies any other `content-type`,
     the latter will be used.);
   - Set the `conent-encoding` to `gzip`;
   - Render the JSON to the string and will send it to the client.
   
   The motivation to have this supported is to alleviate the sidecar developers from performing a 
   boilerplate operations of encoding JSON objects into correct payload.
- `completed` indicates that the payload specified by the response has to be sent to
  the API client, rather than to the API origin.   
   
## The `changeRoute` element

This element communicates the parts of the original route that need to be changed. When either
`host`, `file`, `port`, or `httpVerb` are specified, these mean that the corresponding part of the 
routing calculated by Mashery needs to be replaced.

The following illustrates this. Let's assume that Mashery has calculated to forward the call to 
`https://api.origin.backend.klm.com/travel/api/method` URI. With the response of the sidecar as
```json
{
  "modify" : {
    "changeRoute" : {
      "host" : "apex.salesforce.com"   
    }
  }
}
```
the URI will become `https://apex.salesforce.com/travel/api/method`

# Post-Processor Sidecar Output

The post-processor sidecar output is largely analogous to the pre-processor sidecar output, with the
difference that it:
- cannot modify routing (anymore),
- it cannot `relay` any information, has the post-processor is the last component in the chain, and
- it cannot `complete` the request (because it is already completed), and
- it *can* specify the response code that needs to be sent back to the API client without affecting headers and payload.

Although, for the post-processor, the `terminate` and `modify` structures are *nearly* identical, these
are kept for the clarity of the sidecar code and to avoid the confusion what the sidecar output will result in.

## Complete JSON Schema

```json
{
  "unchangedUntil" : "2019-10-08T14:01:15+0000",
  "terminate" : {
    "payload" : "Custom payload",
    "json" : {
      "a" : "b",
      "c" : "d"
    },
    "base64Encoded" : false,
    "code" : 403,
    "message" : "Termination message",
    "headers" : {
      "x-afklm-error" : "B0-832-J"
    }
  },
  "modify" : {
    "payload" : "Set replacement payload",
    "json" : {
      "a" : "b",
      "c" : "d"
    },
    "base64Encoded" : false,
    "addHeaders" : {
      "x-afklm-level" : "44",
      "x-afklm-bearing" : "326 degrees of inner turbulence"
    },
    "dropHeaders" : [ "authorization", "x-afklm-market" ],
    "code" : 299
  }
}
```

## Overriding response code vs Terminating with error code
As the post-processor point, the sidecar may choose to terminate the processing or simply override the 
response code while leaving the headers and the bosy as supplied by the API origin. The follwing demonstrates
how this is achieved.

To terminate a request with an opaque error message, the sidecar should produce the following output:
```json
{
  "terminate" : {
    "code" : 403,
    "message" : "Termination message"
  }
}
```
To override a status code, the  following data stricture is required:
```json
{
  "modify" : {
    "code" : 299
  }
}
```

# Application examples
## Terminate call with an custom code and message
```json
{
  "terminate" : {
    "code" : 453,
    "message" : "Access is denied due to an ACL on a resource"
  }
}
```
If this structure is provided, the the client will receive:
- status code: 453;
- `content-type`: `application/xml`
- content: `<h1><![CDATA[Access is denied due to an ACL on a resource]]></h1>`

## Terminate call with JSON error message
```json
{
  "terminate" : {
    "code" : 454,
    "json" : {
          "a" : "b",
          "c" : "d"
        }
  }
}
```
If this structure is provided, the the client will receive:
- status code: 454;
- `content-type`: `application/json`
- content: `{"a":"b","c":"d"}`.

## Adding headers 
In this example, two headers will be added. The `unchangedUntil` is indication that the repose can be considered
idempotent until 12:59:57 on the 8th of October, 2019. 
```json
{
  "unchangedUntil" : "2019-10-08T12:59:57+0000",
  "modify" : {
    "addHeaders" : {
      "x-afklm-level" : "44",
      "x-afklm-bearing" : "326 degrees of inner turbulence"
    }
  }
}
```

## Dropping headers
Headers can be dropped by providing a list 
```json
{
  "unchangedUntil" : "2019-10-08T12:59:57+0000",
  "modify" : {
    "dropHeaders" : [ "authorization", "x-afklm-market" ]
  }
}
```

## Omitting the call to the API back-end
This example is used to implement use cases where the payload is already known or pre-calculated.
```json
{
  "modify" : {
    "json" : {
      "a" : "b",
      "c" : "d"
    },
    "addHeaders" : {
      "x-afklm-level" : "44",
      "x-afklm-bearing" : "326 degrees of inner turbulence"
    },
    "completed": false
  }
}
```

## Replacing binary payload
This example shows how a binary payload may be replaced by the sidecar.
```json
{
  "modify" : {
    "payload" : "YWZrbG1ydWxlcwo=",
    "base64Encoded" : true,
    "addHeaders" : {
      "x-afklm-modified-by" : "S-Scanning-Sidecar"
    }
  }
}
```
   
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

