# Air France/KLM Mashery Configuration Dialect

| Property name or pattern                  | Pre-flight (`preflight-` prefix)   | Pre-processor    | Post-Processor |
| -------------------------                 | -----                              | ----             | ---            |
| --- Prefixable keys  ---                  | -----                              | ----             | ---            |
| `deny-service`                            | Yes                                | Yes              | Yes |
| `param-{paramName}`                       | Yes                                | Yes              | Yes |
| `failsafe`                                | Yes                                | Yes              | Yes |
| `elements`                                | Yes                                | Yes              | Yes |
| `eavs`                                    | Yes                                | Yes              | Yes |
| `packageKey-eavs`                         | Yes                                | Yes              | Yes |
| `request-headers`                         | Yes                                | Yes              | Yes |
| `skip-request-headers`                    | Yes                                | Yes              | Yes |
| `reqiure-eavs`                            | Yes                                | Yes              | Yes |
| `require-packageKey-eavs`                 | Yes                                | Yes              | Yes |
| `reqiure-request-headers`                 | Yes                                | Yes              | Yes |
| --- Same for all  ---                     | -----                              | ----             | --- |
| `stack`                                   | Yes                                | Yes              | Yes |
| `stack-{param-name}`                      | Yes                                | Yes              | Yes |
| `timeout`                                 | Yes                                | Yes              | Yes |
| `aclp.alg`                                | Yes                                | Yes              | Yes |
| `aclp.alg.{param}`                        | Yes                                | Yes              | Yes |
| `aclp.sidecar.identity`                   | Yes                                | Yes              | Yes |
| `aclp.sidecar.pk`                         | Yes                                | Yes              | Yes |
| `aclp.sidecar.salt`                       | Yes                                | Yes              | Yes |
| `aclp.sidecar.sk`                         | Yes                                | Yes              | Yes |
| `max-payload-size`                        | Yes                                | Yes              | Yes |
| `max-request-size`                        | Yes                                | Yes              | Yes |
| --- Type-specific ---                     | -----                              | ----             | --- |
| `synchronicity`                           | always Request/Response            | Yes              | Yes |
| `when-{element}[(param)][.{label}]`       | N/A (executed for each call)       | Yes              | Yes |
| `unless-{element}[(param)][.{label}]`     | N/A (executed for each call)       | Yes              | Yes |
| `postprocess-after-routing-change`        | N/A (by design)                    | Yes              | N/A (by design) |
| `max-response-size`                       | N/A (by design)                    | N/A (by design)  | Yes |

