# Mashery Local Adapter Request Mocking Helper

Setting up the unit test for Mashery Local requires a lot of code, which makes writing the unit testing of the 
mashery local adapter very laborious. This guide provides the guide how to set the testing up.

## The Anatomy of the Pre-Processor Request

## YAML Configuration File
The tests are assisted with creation of the YAML file.

```yaml
A set name:
    desc: Description of the set and what it tries to achieve
    
    endpoint:
      serviceId: a service Id
      endpointId: an endpoint Id
      endpointName: an endpoint name
      
      pre-processor configuration:
        key: value
        key1: value1
        
       post-processor configuration:
        key: value
        key1: value1
        
    cases:
      A test case:
        event: pre-processor
        desc: A short description of the case
        requirement: A reference to the requirement.
        
        # What a client has sent, a full version.
        client:
          # Copy the client request from another case
          as in: the test case
          
          # Otherwise specify all applicable parameters
          remote address: 127.0.0.1
          http verb: GET
          uri: https://api-unitttest.airfranceklm.com/fff
          query string: "myQuery=123"
          addHeaders:
            A: B
            # Multiple addHeaders with the same name. 
            C:
            - C1
            - C2  
           payload: AAAA
           
        # The package key that mashery has identified.
        key:
          package key: dfgf
          application:
            name: the-app
            extended attributes:
              a: b
              
        # Authorization context that Mashery has built.
        authorization context:
          scope: "12345"
          user context: "456"
          grant type: CC
          
        # This is what would have been sent to the provider, having the 
        # necessary modification of the client's request. As such, Mashery will be used
        # mainly add or drop addHeaders while the payload of the request isn't really modified.
        request to the api provider:
          provider uri: https://docker.kml/backend/url?myQuery=ffff
          dropped addHeaders:
            - C
            - D
          added addHeaders:
            E: F
            G:
              - H
              - I
              - J
        
        # The above would be long to copy-paste. A shorter form is to define the request
        # once and copy the data from it. 
        inherit request from: name of test case
        
      #processor-specific directives can be freely mised
      # Put here anything you specifically need for your testing
        
      # Modifications required
      expect api origin request modifications:
        dropped addHeaders:
          - one
          - two
        
        added addHeaders:
          h1: value
        
        set http erb: httpVerb
        set uri: aURI
        override payload: true
      
      expect traffic manager:
        set complete: false
        set complete with: "My Message"
        set failed: "Reason my I have failed"
        status code: 403
        status message: "The Status Message"
        payload: "This is a payload to be set"
```