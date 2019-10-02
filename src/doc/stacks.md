# Sidecar Stacks

The following stacks are supported:
- HTTP; the default protocol;
- AWS Lambda;
- AWS SNS;
- HTTPS, where the support for the mutual SSL authentication is required;
- In-memory storage;
- Mashery local cache;

The in-memory an Mashery local cache are mainly used for the local implementation. 

The AWS stack is the stack that will be implied by default. In order to use the stack, the Mashery Local machine
must be able to make calls to the AWS Lambda gateway. A firewall or a transparent proxy needs to be configured to
allow this traffic forwarding.

The HTTP stack is provided as an alternative for the situation where the travel to the AWS Lambda is either not possible
(due to the closed firewall) or not desired (e.g. due to a high volume of calls the service will have to process).
The HTTP stack assumes that the connection from Mashery Local and the receiving endpoint runs over the trusted and
protected network where strong forms of security are not required. 

