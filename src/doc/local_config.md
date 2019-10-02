# Configuring Sidecar Processor Locally

The processor can load sidecar configuration from the files that are located on the Mashery Local machine itself. In 
this scenarios, the deployer shall:
- Distribute a file(s) describing the sidecar invocation parameters to all Mashery Local machines and set these in
  `/etc/mashery/sidecar` directory;
- Within Mashery:
  - Specify the processor for the desired endpoints in the Call Transformation tab;
  - Set pre- and post-processing enabled as desired;
  - Set `honor-local-configuration` to `true` in the Mashery configuration properties.
  
**Providing a local configuration overrules all SaaS-based configuration**  

The motivation for this mechanism is to provide a consistent configuration for the hybrid deployments. Depending
on the situation, different side cars may need to be invoked in SaaS (e.g. AWS-based sidecars) and in Local (e.g. using
HTTP stack). Furthermore, a particular local machine may require specific stack configuration due to the networking
requirements that would not be applicable to all Mashery Local machines in the same cluster.

The requirement to set `honour-local-configuration` is to ensure that the deployer is **explicitly** aware that he is 
 implementing the Mashery Local-specific configurations to achieve the desired sidecar pre- and post-processing.

# Location
# YAML Structure
# Examples