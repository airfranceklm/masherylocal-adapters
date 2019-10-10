# Sidecar Showcase: Controlling Token Usage Pattern

## The context
As API Management Team, you've exposed an API wherein it is possible to retrieve company-sensitive data. The operators
in the field work with an API in an on-and-off mode: the operator has to travel to a physical location and, upon arrival,
he has to directly perform activity where he rapidly needs company's data being provided to him.

To meet the requirement for rapid availability of data, the time-to-live of the access access token has been set to be
a considerable period of time. (This example purposefully doesn't specify the exact amount.)

## The problem to solve
As a token is valid for the extended period of time, there is no way to know if the token is actually born by the
intended user. If a user's session crashes and the user will re-login, the token that was previously assigned to him
will remain recorded as valid within Mashery.

The desired state of affairs is to ensure that the tokens is actually born by the users they associate with (i..e such
tokens were not leaked). Tokens that do not produce traffic that is commensurate with the operator's assignment
must be revoked.

## The solution mode
An obvious proposal to solve this problem could be shorten the access token time-to-live and have the tokens refreshed 
by the application. Although possible, this option is not the solution. The operator in the field may be requesting the 
refresh token in the area with intermittent wireless or Wi-Fi coverage. In this case, the refresh token will be lost 
and the operator will be asked to re-login. Login process requires two factors, which is cumbersome to do in the field.

Since shortening TTL is not possible, a sidecar integration is implemented on such API that will be checking the usage
pattern of the token.

# Simple activity counting solution

The simple activity counting solution, access logs indicate that an operator will use the access token at least once
every two hours. Hence, in order to meet the problem, the sidecar will:
- record the latest actual access time of a token in the database;
- check the time difference between latest actual access and current timestamp. If access will exceed 2 hours, the 
access will not be granted.

The data element necessary for this operation is a full token. The sidecar should be configured on the pre-processor
with the following configuration:
```properties
synchronicity: request-response
expand-input: +token
```
## Inputs to the sidecar
Whenever a user will attempt accessing the API, the following input will be produced to the sidecar:
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    
    "token": {
        "bearerToken" : "adfalkdsfjakjfdajlkjrer",
        "scope" : "Role1 Role2 Role3",
        "userContext" : "User Context Value",
        "expires" : "2019-10-08T12:59:57Z",
        "grantType" : "password"
    }
}
```

## Outputs from the sidecar

### Granting access output

To grant access, the sidecar should yield an empty object indicating that no modification is necessary.
```json
{}
```

### Denying access output

To deny access, the sidecar should yield an appropriate terminate command data structure:
```json
{
  "terminate": {
    "code": 403,
    "message": "You are logged out due to extended inactivity" 
  } 
}
```

# Improving simple activity counting with idempotent calls

The solution proposed by the [simple activity counting](#simple-activity-counting-solution) will generate a call
each time the user is accessing the API. In the situation where the operator will be making rapid calls, frequently
re-validating the same token is not desired. To minimize the number of repeated validation calls, the sidecar may
choose to implement *idempotent* response for the first 15 minutes for successful outputs.

Assuming it is a the moment `"2019-10-08T12:44:57Z"`, the response

```json
{
  "unchangedUntil": "2019-10-22T14:59:57Z"
}
```
means that this operator is authorized to access the service for the next 15 minutes without Mashery having to call
the sidecar. This optimises number of calls ot the sidear and improves the overall performance of the API as observed
by the operator. However, this solution will only be benefitial **if** the operator is expected to make a lot of calls
in this time frame.

# Removing the jitter on unauthorized responses

Similarly to [idempotent successful authorizations](#improving-simple-activity-counting-with-idempotent-calls), the 
sidecar can choose to make the unauthorized decisions idempotent. To achieve this, the `unchangedUntil` data element
has to be added to the data structure the section [Denying data access](#denying-access-output) describes:
```json
{
  "unchangedUntil": "2019-10-22T14:59:57Z",
  "terminate": {
    "code": 403,
    "message": "You are logged out due to extended inactivity" 
  } 
}
```

# Operation heuristics solution
In case sidecar logic requires also checking the operations that are actually called (e.g. to ensure that the operator
is accessing the data that is pertinent to his/her assignment), the also the operation is required to be added
to the inputs the sidecar has to receive.

It is achieved by configuring the pre-processor as:
```properties
synchronicity: request-response
expand-input: +token,operation
```

Whenever a user will attempt accessing the API, the following input will be produced to the sidecar:
```json
{
    "point": "PreProcessor",
    "synchronicity": "RequestResponse",
    "packageKey": "myKey",
    "serviceId": "serviceId",
    "endpointId": "endpointId",
    "masheryMessageId": "masheryMessageId",
    
    "operation" : {
        "httpVerb" : "GET",
        "path" : "path/to/op",
        "query" : {
          "m" : "b737-800",
          "depT" : "LFPG",
          "dest" : "EHAM"
        },
        "uri" : "https://api-unittest.airfranceklm.com/travel/unittest/path/to/op?m=b737-800&dept=LFPG&dest=EHAM"
    },

    "token": {
        "bearerToken" : "adfalkdsfjakjfdajlkjrer",
        "scope" : "Role1 Role2 Role3",
        "userContext" : "User Context Value",
        "expires" : "2019-10-08T12:59:57Z",
        "grantType" : "password"
    }
}
```

The output is identical as the above-mentioned examples [1](#granting-access-output), [2](#denying-access-output),
[3](#improving-simple-activity-counting-with-idempotent-calls), and [4](#removing-the-jitter-on-unauthorized-responses)
describe, depending on the expected behaviour.