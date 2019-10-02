# AWS Lambda Stack
AWS Lambda stack, like it's name implies, will call functions deployed in the AWS Lambda service. This stack natively supports
request-response as well as synchronous event invocations. 

### Input and output locations
With the AWS Lambda, the input will be translated (by the AWS) into the `event` object. The following gives a 
brief overview of a Lambda handler in Node.js that prints the passed synchronicity. The output, if expected,
shall be an object having a JSON representation compatible with the Lambda Output JSON structure described in this
guide below.

```js
exports.handler = (event) => {
    console.log(`Function called with ${event.synchronicity} settting.`);
    return {};
}
```  

### AWS Stack Configuration
The AWS Lambda stack is a default stack that will be selected. It can also be explicitly designated
by specifying
```properties
stack=aws
```
in the pre- or post-processor configuration parameters.

Additionally, AWS lambda stack requires 4 parameters:
```properties
aws.functionARN=an_Amazon_Resource_Name_of_the_function
# Example of the above: arn:aws:lambda:eu-west-1:190954361614:function:LambdaBasicAsync

# The key and secret will be read from test properties.
aws.key=client_key
aws.secret=client_secret
aws.region=eu-west-1
```

Failure to provide at these will result in the error 500 being returned for invalid configuration.

The deployer should check that the function is accessible in the specified region with the specified parameters 
before the deployment.

To configure running the AWS lambda behind the proxy, add the following settings:
```properties
aws.proxyHost=proxy_host
aws.proxyPort=443
```
To specify the HTTP proxy the server to use in order to connect to the Lambda functions.
