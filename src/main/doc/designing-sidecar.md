# Designing sidecars with the AFKLM Sidecar Processor

Do I need a sidecar? What should my sidecar do? These is, perhaps, the first and the
most difficult question to answer. The rest is relatively straight-forward. 

**The design of the sidecar should begin with the end in mind and understanding why sidecar is the best alternative.**

With the goal in mind, you need to decide:
- Do you need a pre-processor, post-processor, or both?
- Which data elements are needed to fulfil the goal?
- Will sidecar yield an output to modify the request?

We have prepared [several examples](./examples.md) showing the problem we had to solve and how the sidecar delivers
on these solutions. 
If you are an existing Mashery customer, please contact your CSM and arrange the knowledge sharing sessions.

# Motivation for the sidecar

At Air France/KLM, we see the three motivation for attaching the sidecars is to Mashery API processing chain. We use
sidecars to:
1. implement custom authentication and authorization,
2. deal with the undesired effects of the API user behaviour, and 
3. implement traffic analytics and/or mediation features that span multiple API endpoints that are maintained by
multiple teams. This way, for instance, we collect traffic analytics for our marketing complains and hook up additional
security inspections -- such as e.g. JSON injections. 

Externalising processing logic to a sidecar -- especially when the sidecar is assembled from the pre-fabricated 
processing steps such as e.g. in a Project Flog application -- radically lowers the time-to-operation for these
features. 

## Should all of the above be part of the API backend?

Short answer is: maybe. 

The design reason to externalize the logic into the sidecar is that implements measures which
are *temporary* in nature and, maybe, can change without a warning. In other words, externalizing the pre- and post-processing
is preferred over other alternatives when the following conditions are met:
- purely technically, your organization has skills to program a sidecar: 
    - you *can* program the low-latency, high-volume sidecars for the scale of your traffic; 
    - you can accept observing increase of the response time which will be incurred when Mashery gateway will be
      communicating with the sidecar, as well as
    - your support is skilled enough to tell sidecar failures from Mashery errors from API backend failures apart;
- a *rapid* implementation is required to solve a problem at hand by means of pre- and post-processing;
- the  pre- and/or post-processing solution is meant to deal with a situation that is *temporary* in nature;
- the solution is both:
  - impractical to be a structural part of the API origin design (mainly because it is of a temporary measure); and
  - impractical to be embedded as part of the API gateway code as the problem is API-specific;
- last, but not the least, the exact logic is too specific for your company to wait for TIBCO Mashery to have a similar
  feature available as a native part of the product.

Using the sidecar processing, you can, inter alia via a centralized process, achieve use cases like:
1. Implement custom authentication schemes;
2. Implement custom (as well as temporary) authorization schemes;
3. Add incoming content inspection e.g. for the presence of SQL injections;
4. Add AFKLM-specific application information that should be used by the API provider;
5. Manage the load on the API back-end by providing custom caching for selected resources;
6. Implement response content filtering on a custom condition;
7. Collect data for usage pattern identification and abnormal traffic pattern detection;
8. Collect data for business analytics;
9. Collect data for the security monitoring;
10. Sanitize user input. 

# Data Elements

| Data Element | Purpose | Pre-Processor | Post-Processor |
| -----------  | ------ | ------  | -------------|
| Remote Address | Remote address where Msahery has received the request from | Yes | Yes |