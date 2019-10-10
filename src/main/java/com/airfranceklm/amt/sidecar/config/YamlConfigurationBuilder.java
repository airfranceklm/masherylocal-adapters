package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.impl.model.*;
import com.airfranceklm.amt.sidecar.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.JsonHelper.parseJSONDate;
import static com.airfranceklm.amt.sidecar.config.ConfigRequirement.Included;
import static com.airfranceklm.amt.sidecar.config.ConfigRequirement.Required;
import static com.airfranceklm.amt.sidecar.config.MasheryConfigSidecarConfigurationBuilder.*;

/**
 * Class that will build the sidecar configuration from the specific YAML configuration.
 */
public class YamlConfigurationBuilder {

    private static Pattern jsonPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?(Z|[+-]\\d{2}(:)?\\d{2})");

    public static SidecarConfiguration getSidecarConfiguration(SidecarInputPoint point, Map<String, Object> cfg) {
        SidecarConfiguration retVal = new SidecarConfiguration(point);
        retVal.setStack("http");

        if (cfg == null) {
            return retVal;
        }

        forDefinedString(cfg, CFG_SYNCHRONICITY, (val) -> {
            switch (val.toLowerCase().trim()) {
                case "requestresponse":
                case CFG_VAL_REQUEST_RESPONSE:
                    retVal.setSynchronicity(SidecarSynchronicity.RequestResponse);
                    break;
                case CFG_VAL_NON_BLOCKING:
                case "non-blocking-event":
                case "nonblocking":
                case "nonblockingevent":
                    retVal.setSynchronicity(SidecarSynchronicity.NonBlockingEvent);
                    break;
                case CFG_VAL_SYNC_EVENT:
                default:
                    retVal.setSynchronicity(SidecarSynchronicity.Event);
                    break;
            }
        });

        // Parse the timeout value.
        forDefinedString(cfg, CFG_TIMEOUT, (str) -> parseSidecarTimeout(str, retVal));
        forDefinedInteger(cfg, CFG_TIMEOUT, retVal::setSidecarTimeout);


        // Parse the idempotent configuration setting
        forDefinedBoolean(cfg, CFG_IDEMPOTENT_AWARE, retVal::setIdempotentAware);

        forDefinedObjectMap(cfg, "stack", (stackCfg) -> {
            forDefinedString(stackCfg, "type", retVal::setStack);
            forDefinedStringMap(stackCfg, "params", retVal::setStackParams);
        });

        forDefinedObjectMap(cfg, "sidecar-params", retVal::setSidecarParams);

        forDefinedObjectMap(cfg, "size", (map) -> {
            MaxSizeSetting req = new MaxSizeSetting(50 * 1024, MaxSizeComplianceRequirement.Blocking);
            forDefinedLong(map, "max-size", req::setMaxSize);
            forDefinedString(map, "mode", (setting) -> {
                if ("filtering".equalsIgnoreCase(setting)) {
                    req.setCompliance(MaxSizeComplianceRequirement.Filtering);
                }
            });

            retVal.setMaxSize(req);
        });

        forDefinedBoolean(cfg, CFG_FAILSAFE, retVal::setFailsafe);
        forDefinedStringList(cfg, CFG_EXPAND, (v) -> {
            String[] arr = new String[v.size()];
            arr = v.toArray(arr);

            parseExpansionList(arr, retVal::expandTo);
        });

        forDefinedObjectMap(cfg, "request-headers", (reqCfg) -> {
            forDefinedStringList(reqCfg, "require", v -> {
                retVal.processRequestHeaders(Required, v);
            });
            forDefinedStringList(reqCfg, "include", v -> {
                retVal.processRequestHeaders(Included, v);
            });

            forDefinedStringList(reqCfg, "skip", retVal::skipRequestHeaders);
        });

        forDefinedObjectMap(cfg, "response-headers", (reqCfg) -> {
            forEachInDefinedStringList(reqCfg, "include", retVal::includeResponseHeader);
            forDefinedStringList(reqCfg, "skip", retVal::skipResponseHeaders);

        });

        forDefinedObjectMap(cfg, "eavs", (reqCfg) -> {
            forEachInDefinedStringList(reqCfg, "require", (v) -> {
                retVal.processApplicationEAV(Required, v);
            });
            forEachInDefinedStringList(reqCfg, "include", (v) -> {
                retVal.processApplicationEAV(Included, v);
            });

            if (retVal.requiresApplicationEAVs()) {
                retVal.expandTo(InputScopeExpansion.ApplicationEAVs);
            }
        });

        forDefinedObjectMap(cfg, "pacakgeKey-eavs", (reqCfg) -> {

            forEachInDefinedStringList(reqCfg, "require", (v) -> {
                retVal.processPackageKeyEAV(Required, v);
            });
            forEachInDefinedStringList(reqCfg, "include", (v) -> {
                retVal.processPackageKeyEAV(Included, v);
            });

            if (retVal.requiresPreflightPackageKeyEAVs()) {
                retVal.expandTo(InputScopeExpansion.PackageKeyEAVS);
            }
        });

        forDefinedObjectMap(cfg, "preflight", (preFlight) -> {
            forDefinedBoolean(preFlight, "enabled", retVal::setPreflightEnabled);

            forDefinedObjectMap(preFlight, "headers", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (h) -> {
                    retVal.processPreflightHeaders(Required, h);
                });

                forEachInDefinedStringList(hmap, "include", (h) -> {
                    retVal.processPreflightHeaders(Included, h);
                });
            });

            forDefinedObjectMap(preFlight, "eavs", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (eav) -> {
                    retVal.processPreflightEAVs(Required, eav);
                });

                forEachInDefinedStringList(hmap, "include", (eav) -> {
                    retVal.processPreflightEAVs(Included, eav);
                });
            });

            forDefinedObjectMap(preFlight, "packageKey-eavs", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (eav) -> retVal.processPreflightPackageKeyEAVs(Required, eav));

                forEachInDefinedStringList(hmap, "include", (eav) -> retVal.processPreflightPackageKeyEAVs(Included, eav));
            });

            forDefinedObjectMap(preFlight, "params", retVal::setPreflightParams);

            forDefinedStringList(preFlight, CFG_EXPAND, (v) -> {
                String[] arr = new String[v.size()];
                arr = v.toArray(arr);

                parsePreflightExpansionList(arr, retVal);
            });
        });

        forDefinedObjectMap(cfg, "staticModification", (rMap) -> {
            retVal.setStaticModification(buildSidecarPreProcessorOutputFromYAML(rMap));
        });


        forDefinedObjectMap(cfg, "scopeFilters", (filtersMap) -> {
            forEachNamedArrayIn(filtersMap, (groupName, groupYaml) -> {
                forEachInArray(groupYaml, (entryYaml)-> {
                    forObjectMap(entryYaml, (yaml) -> {
                        String param = getDefinedString(yaml, "param");
                        String label = getDefinedString(yaml, "label");
                        String value = getDefinedString(yaml, "value");
                        Boolean inc = getDefinedBoolean(yaml, "inclusive");

                        if (param != null || label != null || value != null) {

                            if (inc == null) {
                                inc = true;
                            }
                            retVal.addScopeFilter(new SidecarScopeFilterEntry(groupName, param, label, value, inc));
                        } else {
                            // Increment an error in the configuration.
                            retVal.incrementError();
                        }
                    });
                });
            });

        });

        return retVal;
    }

    @SuppressWarnings("unchecked")
    private static void forEachInArray(Object pList, Consumer<Object> c) {
        if (pList instanceof List) {
            ((List)pList).forEach(c);
        }
    }

    /**
     * Executes the consumer if the passed object is a non-null instance of {@link Map}
     * @param obj Object to check
     * @param c consumer to be called
     */
    @SuppressWarnings("unchecked")
    private static void forObjectMap(Object obj, Consumer<Map<String, Object>> c) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map) {
            c.accept((Map<String,Object>)obj);
        }
    }

    static void forDefinedLong(Map<String, Object> cfg, String key, Consumer<Long> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Long) {
            lambda.accept((Long) t);
        }
    }

    public static void forDefinedInteger(Map<String, Object> cfg, String key, Consumer<Integer> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Number) {
            lambda.accept(((Number) t).intValue());
        }
    }

    public static void forDefinedString(Map<String, Object> cfg, String key, Consumer<String> lambda) {
        Object t = cfg.get(key);
        if (t instanceof String) {
            lambda.accept((String) t);
        }
    }


    public static void forDefinedBoolean(Map<String, Object> cfg, String key, Consumer<Boolean> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Boolean) {
            lambda.accept((Boolean) t);
        }
    }

    static void forEachInObjectList(Object list, Consumer<Object> lambda) {
        if (!(list instanceof List)) {
            return;
        }
        List<Object> l = (List<Object>)list;
        l.forEach(lambda);
    }

    @SuppressWarnings("unchecked")
    static void forEachInDefinedStringList(Map<String, Object> cfg, String key, Consumer<String> lambda) {
        Object t = cfg.get(key);
        if (t instanceof List) {
            for (Object o: (List)t) {
                if (!(o instanceof String)) {
                    return;
                }
            }

            List<String> l = (List<String>) t;
            l.forEach(lambda);
        }
    }

    @SuppressWarnings("unchecked")
    public static void forDefinedStringList(Map<String, Object> cfg, String key, Consumer<List<String>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof List) {
            for (Object o: (List)t) {
                if (!(o instanceof String)) {
                    return;
                }
            }
            lambda.accept((List<String>) t);
        }
    }

    /**
     * Invokes the lambda function if the there is <code>key</code> defined in <code>cfg</code>, and it contains
     * all stinrgs
     * @param cfg container map
     * @param key key to use
     * @param lambda consumer of the string map
     */
    @SuppressWarnings("unchecked")
    public static void forDefinedStringMap(Map<String, Object> cfg, String key, Consumer<Map<String, String>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            Map<?,?> unchecked = (Map<?,?>)t;
            for (Map.Entry<?,?> e: unchecked.entrySet()) {
                if (!(e.getValue() instanceof String)) {
                    return;
                }
            }
            lambda.accept((Map<String, String>) t);
        }
    }

    /**
     * Parses the define map in this object, if it is defined.
     * @param cfg container map.
     * @param key key to store
     * @param consumer parser lambda
     * @param <T> type of the return value
     * @return parsed instance, or null if value at the <code>key</code> is not found or is not a map.
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseDefinedObjectMap(Map<String,Object> cfg, String key, MapParser<T> consumer) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            return consumer.accept((Map<String, Object>) t);
        }  else {
            return null;
        }
    }

    public static String getDefinedString(Map<String,Object> cfg, String key) {
        Object retVal = cfg.get(key);
        if (retVal == null) {
            return null;
        } else if (retVal instanceof String) {
            return (String)retVal;
        } else {
            return null;
        }
    }

    public static Boolean getDefinedBoolean(Map<String,Object> cfg, String key) {
        Object retVal = cfg.get(key);
        if (retVal == null) {
            return null;
        } else if (retVal instanceof Boolean) {
            return (Boolean) retVal;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void forDefinedObjectMap(Map<String, Object> cfg, String key, Consumer<Map<String, Object>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            lambda.accept((Map<String, Object>) t);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> void filterOutNull(Map<String, Object> cfg, String key, Consumer<T> lambda) {
        T t = (T) cfg.get(key);
        if (t != null) {
            lambda.accept(t);
        }
    }

    public static SidecarPreProcessorOutputImpl buildSidecarPreProcessorOutputFromYAML(Map<String, Object> sidecarOutputYaml) {

        if (sidecarOutputYaml != null) {
            SidecarPreProcessorOutputImpl retVal = new SidecarPreProcessorOutputImpl();

            forDefinedObjectMap(sidecarOutputYaml, "modify", (modifyYaml) -> {
                RequestModificationCommandImpl mod = new RequestModificationCommandImpl();

                // Common part: header and payload
                buildHeaderModifications(modifyYaml, mod);
                buildPayloadCarrier(modifyYaml, mod);

                // Specific part: change route and the possibility to terminate.
                forDefinedObjectMap(modifyYaml, "changeRoute", (m) -> mod.setChangeRoute(buildOutputRoutingFromYaml(m)));
                forDefinedInteger(modifyYaml, "completeWithCode", mod::setCompleteWithCode);

                if (!mod.containsNullsOnly()) {
                    retVal.setModify(mod);
                }
            });

            forDefinedString(sidecarOutputYaml, "unchangedUntil", (str) -> {
                Matcher m = jsonPattern.matcher(str);
                if (m.matches()) {
                    retVal.setUnchangedUntil(parseJSONDate(str));
                }
            });

            forDefinedObjectMap(sidecarOutputYaml, "relayParams", retVal::setRelayParams);

            forDefinedObjectMap(sidecarOutputYaml, "terminate", (terminateYaml) -> {
                buildTerminateCommand(retVal, terminateYaml);
            });


            return retVal;
        } else {
            return null;
        }
    }

    public static SidecarPostProcessorOutputImpl buildSidecarPostProcessorOutputFromYAML(Map<String, Object> sidecarOutputYaml) {

        if (sidecarOutputYaml != null) {
            SidecarPostProcessorOutputImpl retVal = new SidecarPostProcessorOutputImpl();

            forDefinedObjectMap(sidecarOutputYaml, "modify", (modifyYaml) -> {
                ResponseModificationCommandImpl mod = new ResponseModificationCommandImpl();

                // Common part: header and payload
                buildHeaderModifications(modifyYaml, mod);
                buildPayloadCarrier(modifyYaml, mod);

                // Specific part: code
                forDefinedInteger(modifyYaml, "code", mod::setCode);

                if (!mod.containsOnlyNulls()) {
                    retVal.setModify(mod);
                }
            });

            forDefinedString(sidecarOutputYaml, "unchangedUntil", (str) -> {
                Matcher m = jsonPattern.matcher(str);
                if (m.matches()) {
                    retVal.setUnchangedUntil(parseJSONDate(str));
                }
            });

            forDefinedObjectMap(sidecarOutputYaml, "terminate", (terminateYaml) -> {
                buildTerminateCommand(retVal, terminateYaml);
            });


            return retVal;
        } else {
            return null;
        }
    }

    private static void buildHeaderModifications(Map<String, Object> modifyYaml, CallModificationCommandImpl mod) {
        forDefinedStringMap(modifyYaml, "addHeaders", mod::setAddHeaders);
        forDefinedStringList(modifyYaml, "dropHeaders", mod::setDropHeaders);
    }

    private static void buildTerminateCommand(AbstractSidecarOutputImpl retVal, Map<String, Object> terminateYaml) {
        SidecarOutputTerminationCommandImpl cmd = new SidecarOutputTerminationCommandImpl();

        forDefinedInteger(terminateYaml, "code", cmd::setCode);
        forDefinedString(terminateYaml, "message", cmd::setMessage);
        forDefinedStringMap(terminateYaml, "headers", cmd::setHeaders);

        buildPayloadCarrier(terminateYaml, cmd);

        if (!cmd.containsOnlyNulls()) {
            retVal.setTerminate(cmd);
        }
    }

    private static void buildPayloadCarrier(Map<String, Object> modifyYaml, PayloadCarrierImpl mod) {
        forDefinedString(modifyYaml, "payload", mod::setPayload);
        forDefinedBoolean(modifyYaml, "base64Encoded", mod::setBase64Encoded);
        forDefinedObjectMap(modifyYaml, "json", mod::setJSONPayload);
    }

    private static RequestRoutingChangeBeanImpl buildOutputRoutingFromYaml(Map<String, Object> yaml) {
        if (yaml == null) {
            return null;
        }

        RequestRoutingChangeBeanImpl r = new RequestRoutingChangeBeanImpl();
        forDefinedString(yaml, "host", r::setHost);
        forDefinedString(yaml, "file", r::setFile);
        forDefinedString(yaml, "httpVerb", r::setHttpVerb);
        forDefinedString(yaml, "uri", r::setUri);
        forDefinedInteger(yaml, "port", r::setPort);

        if (r.containsOnlyNulls()) {
            return null;
        } else {
            return r;
        }
    }

    public static SidecarInput buildSidecarInputFromYAML(Map<String, Object> lambdaIn) {
        SidecarInput sidecarInput = new SidecarInput();

        forDefinedString(lambdaIn, "masheryMessageId", sidecarInput::setMasheryMessageId);

        forDefinedString(lambdaIn, "point", (s) -> sidecarInput.setPoint(SidecarInputPoint.valueOf(s)));

        forDefinedString(lambdaIn, "synchronicity", (s) -> sidecarInput.setSynchronicity(SidecarSynchronicity.valueOf(s)));

        forDefinedObjectMap(lambdaIn, "request", (rMap) -> {
            final SidecarInputHTTPMessage msg = new SidecarInputHTTPMessage();
            forDefinedStringMap(rMap, "headers", msg::setHeaders);
            forDefinedString(rMap, "payload", msg::setPayload);

            if (msg.getPayload() != null) {
                msg.setPayloadLength(msg.getPayload().length());
            } else {
                msg.setPayloadLength(0);
            }

            sidecarInput.setRequest(msg);
        });

        forDefinedObjectMap(lambdaIn, "response", (rMap) -> {
            final SidecarInputHTTPResponseMessage msg = new SidecarInputHTTPResponseMessage();

            forDefinedInteger(rMap, "code", msg::setResponseCode);
            forDefinedStringMap(rMap, "headers", msg::setHeaders);
            forDefinedString(rMap, "payload", msg::setPayload);

            if (msg.getPayload() != null) {
                msg.setPayloadLength(msg.getPayload().length());
            } else {
                msg.setPayloadLength(0);
            }

            sidecarInput.setResponse(msg);
        });


        forDefinedStringMap(lambdaIn, "eavs", sidecarInput::setEavs);
        forDefinedStringMap(lambdaIn, "packageKeyEAVs", sidecarInput::setPackageKeyEAVs);

        forDefinedString(lambdaIn, "packageKey", sidecarInput::setPackageKey);

        forDefinedObjectMap(lambdaIn, "operation", (rMap) -> readOperation(sidecarInput, rMap));
        forDefinedObjectMap(lambdaIn, "token", (rMap) -> readToken(sidecarInput, rMap));

        forDefinedObjectMap(lambdaIn, "routing", (rMap) -> readRouting(sidecarInput, rMap));

        forDefinedString(lambdaIn, "serviceId", sidecarInput::setServiceId);
        forDefinedString(lambdaIn, "endpointId", sidecarInput::setEndpointId);
        forDefinedString(lambdaIn, "remoteAddress", sidecarInput::setRemoteAddress);


        forDefinedObjectMap(lambdaIn, "params", sidecarInput::addAllParams);

        return sidecarInput;
    }

    /**
     * Iterates over the array containing maps.
     * @param obj Object to traverse
     * @param c consumer to be called for each found.
     */
    @SuppressWarnings("unchecked")
    public static void iterateListOfObjectMaps(Object obj, Consumer<Map<String,Object>> c) {
        if (obj instanceof List) {
            List<Object> l = (List<Object>)obj;
            l.forEach((listObj) -> {
                if (listObj instanceof Map) {
                    Map<String,Object> v = (Map<String,Object>)listObj;
                    c.accept(v);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static void forEachObjectMapIn(Map<String,Object> yaml, BiConsumer<String, Map<String,Object>> c) {
        yaml.forEach((key, value) -> {
            Map<String,Object> yamlVal = (Map<String,Object>) value;
            c.accept(key, yamlVal);
        });
    }

    @SuppressWarnings("unchecked")
    private static void forEachNamedArrayIn(Map<String,Object> yaml, BiConsumer<String, List<Object>> c) {
        yaml.forEach((key, value) -> {
            if (value instanceof List) {
                c.accept(key, (List<Object>)value);
            }
        });
    }

    private static void readOperation(SidecarInput input, Map<String, Object> lambdaIn) {
        SidecarInputOperation op = new SidecarInputOperation();
        forDefinedString(lambdaIn, "httpVerb", op::setHttpVerb);
        forDefinedString(lambdaIn, "path", op::setPath);
        forDefinedString(lambdaIn, "uri", op::setUri);
        forDefinedStringMap(lambdaIn, "query", op::setQuery);

        input.setOperation(op);
    }

    private static void readToken(SidecarInput sidecarInput, Map<String, Object> lambdaIn) {
        SidecarInputToken token = new SidecarInputToken();

        forDefinedString(lambdaIn, "bearerToken", token::setBearerToken);
        forDefinedString(lambdaIn, "grantType", token::setGrantType);
        forDefinedString(lambdaIn, "scope", token::setScope);
        forDefinedString(lambdaIn, "userContext", token::setUserContext);
        forDefinedString(lambdaIn, "expires", (v) -> token.setExpires(parseJSONDate(v)));


        sidecarInput.setToken(token);
    }

    private static void readRouting(SidecarInput sidecarInput, Map<String, Object> lambdaIn) {
        SidecarInputRouting r = new SidecarInputRouting();
        forDefinedString(lambdaIn, "httpVerb", r::setHttpVerb);
        forDefinedString(lambdaIn, "uri", r::setUri);

        sidecarInput.setRouting(r);
    }

    /**
     * Loads all Yaml documents into memory.
     * @param clazz reference class
     * @param resource resource of that class to load
     * @return Iterator of yaml documents, or null if resource was not possible.
     */
    static Iterator<Object> loadAllYamlDocuments(Class clazz, String resource) {
        try (InputStream is = clazz.getResourceAsStream(resource)) {
            if (is != null) {
                ArrayList<Object> loadedDocs = new ArrayList<>();
                Iterator<Object> rawIter =  new Yaml().loadAll(new InputStreamReader(is)).iterator();
                rawIter.forEachRemaining(loadedDocs::add);

                return loadedDocs.iterator();
            } else {
                return null;
            }
        } catch (IOException e) {
            // Ignore it.
            return null;
        }
    }

    /**
     * Retrieves next YAML document from the structure loaded with {@link #loadAllYamlDocuments(Class, String)}.
     * @param obj iterator object
     * @return next instance in the iterator, if found. Otherwise null will be returned.
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> nextYamlDocument(Iterator<Object> obj) {
        if (obj == null) {
            return null;
        } else if (obj.hasNext()) {
            return (Map<String, Object>) obj.next();
        } else {
            return null;
        }
    }
}
