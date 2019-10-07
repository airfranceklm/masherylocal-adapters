```yaml
configuration:
  serviceId:
    endpointId:
      pre-process:
        synchronicity: Event
        stack: http
        stack parameters:
          param1: value1
          param2: value2
        
        pre-flight:
          headers:
            require:
            - Header_A
            - Header_B
            include:
            - Header_C
            -  Header_D
          application eavs:
            require:
              - EAV_1
              - EAV_2
            include:
              - EAV_3
              - EAV4
                
          package key eavs:
            require:
              - EAV_1
              - EAV_2
            include:
              - EAV_3
              - EAV4
          expand:
          - Grant type
          params:
            p1: v1
            p2L v2




        sidecar params:
          param1: value1
          param2: value2

        size:
          max-size: 1024m
          mode: filtering
      
        
        failsafe: false
        expand:
          - Token
        
        request headers:
          require:
            - Header_A
            - Header_B
          include:
          - Header_C
          - Header_D
          skip:
          - Header_E
          - Header_F
      
        response headers:
          include:
            - Header A
            - Header B
          skip:
            - Header C
            - Header D



        
        application eavs:
          require:
            - EAV_1
            - EAV_2
          include:
            - EAV_3
            - EAV4
      
        package key eavs:
          require:
            - EAV_1
            - EAV_2
          include:
            - EAV_3
            - EAV4

        static modification:
                  


      post-process:
In-memory storage:
  serviceId:
    endpoint:
       input:
       output:
```