package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class that will build the sidecar configuration from the specific YAML configuration.
 */
public class YamlConfigurationBuilder {

    public static SidecarConfiguration getSidecarConfiguration(SidecarInputPoint point, Map<String, Object> cfg) {
        SidecarConfiguration retVal = new SidecarConfiguration(point);

        forDefinedString(cfg, "synchronicity", (val) -> {
            retVal.setSynchronicity(SidecarSynchronicity.valueOf(val));
        });

        forDefinedString(cfg, "stack", retVal::setStack);

        forDefinedStringMap(cfg, "stack parameters", retVal::setStackParams);
        forDefinedObjectMap(cfg, "sidecar params", retVal::setSidecarParams);

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

        forDefinedBoolean(cfg, "failsafe", retVal::setFailsafe);
        forEachInDefinedStringList(cfg, "expand", (v) -> {
            retVal.expandTo(InputScopeExpansion.valueOf(v));
        });

        forDefinedObjectMap(cfg, "request headers", (reqCfg) -> {
            forDefinedStringList(reqCfg, "require", v -> {
                retVal.processRequestHeaders(ConfigRequirement.Required, v);
            });
            forDefinedStringList(reqCfg, "include", v -> {
                retVal.processRequestHeaders(ConfigRequirement.Included, v);
            });

            forEachInDefinedStringList(reqCfg, "skip", retVal::skipRequestHeader);

        });

        forDefinedObjectMap(cfg, "response headers", (reqCfg) -> {
            forEachInDefinedStringList(reqCfg, "include", retVal::includeResponseHeader);
            forEachInDefinedStringList(reqCfg, "skip", retVal::skipsResponseHeader);

        });

        forDefinedObjectMap(cfg, "application eavs", (reqCfg) -> {
            retVal.expandTo(InputScopeExpansion.ApplicationEAVs);

            forEachInDefinedStringList(reqCfg, "require", (v) -> {
                retVal.processApplicationEAV(ConfigRequirement.Required, v);
            });
            forEachInDefinedStringList(reqCfg, "include", (v) -> {
                retVal.processApplicationEAV(ConfigRequirement.Included, v);
            });

        });

        forDefinedObjectMap(cfg, "package key eavs", (reqCfg) -> {
            retVal.expandTo(InputScopeExpansion.PackageKeyEAVS);

            forEachInDefinedStringList(reqCfg, "require", (v) -> {
                retVal.processPackageKeyEAV(ConfigRequirement.Required, v);
            });
            forEachInDefinedStringList(reqCfg, "include", (v) -> {
                retVal.processPackageKeyEAV(ConfigRequirement.Included, v);
            });

        });

        forDefinedObjectMap(cfg, "pre-flight", (preFlight) -> {
            retVal.setPreflightEnabled(true);

            forDefinedObjectMap(preFlight, "headers", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (h) -> {
                    retVal.processPreflightHeaders(ConfigRequirement.Required, h);
                });

                forEachInDefinedStringList(hmap, "include", (h) -> {
                    retVal.processPreflightHeaders(ConfigRequirement.Included, h);
                });
            });

            forDefinedObjectMap(preFlight, "application eavs", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (eav) -> {
                    retVal.processPreflightEAVs(ConfigRequirement.Required, eav);
                });

                forEachInDefinedStringList(hmap, "include", (eav) -> {
                    retVal.processPreflightEAVs(ConfigRequirement.Included, eav);
                });
            });

            forDefinedObjectMap(preFlight, "package key eavs", (hmap) -> {
                forEachInDefinedStringList(hmap, "require", (eav) -> {
                    retVal.processPreflightPackageKeyEAVs(ConfigRequirement.Required, eav);
                });

                forEachInDefinedStringList(hmap, "include", (eav) -> {
                    retVal.processPreflightPackageKeyEAVs(ConfigRequirement.Included, eav);
                });
            });

            forDefinedObjectMap(preFlight, "params", retVal::setPreflightParams);

            forEachInDefinedStringList(preFlight, "expand", (s) -> {
                retVal.expandPreflightTo(InputScopeExpansion.valueOf(s));
            });
        });

        forDefinedObjectMap(cfg, "static modification", (rMap) -> {
            retVal.setStaticModification(buildSidecarOutputFromYAML(rMap));
        });

        return retVal;
    }

    public static void forDefinedLong(Map<String, Object> cfg, String key, Consumer<Long> lambda) {
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

    public static void forEachInDefinedStringList(Map<String, Object> cfg, String key, Consumer<String> lambda) {
        Object t = cfg.get(key);
        if (t instanceof List) {
            List<String> l = (List<String>) t;
            l.forEach(lambda);
        }
    }

    public static void forDefinedStringList(Map<String, Object> cfg, String key, Consumer<List<String>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof List) {
            lambda.accept((List<String>) t);
        }
    }

    public static void forDefinedStringMap(Map<String, Object> cfg, String key, Consumer<Map<String, String>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            lambda.accept((Map<String, String>) t);
        }
    }

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

    public static SidecarOutput buildSidecarOutputFromYAML(Map<String, Object> sidecarOutputYaml) {

        if (sidecarOutputYaml != null) {
            SidecarOutputImpl sidecarOutput = new SidecarOutputImpl();
            forDefinedInteger(sidecarOutputYaml, "code", sidecarOutput::setCode);
            forDefinedString(sidecarOutputYaml, "payload", sidecarOutput::setPayload);
            forDefinedStringList(sidecarOutputYaml, "dropHeaders", sidecarOutput::setDropHeaders);
            forDefinedStringMap(sidecarOutputYaml, "addHeaders", sidecarOutput::setAddHeaders);

            forDefinedString(sidecarOutputYaml, "message", sidecarOutput::setMessage);
            forDefinedObjectMap(sidecarOutputYaml, "json", (m) -> {
                sidecarOutput.setJson(AFKLMSidecarProcessor.objectMapper.valueToTree(m));
            });

            return sidecarOutput;
        } else {
            return null;
        }
    }

    public static SidecarInput buildSidecarInputFromYAML(Map<String, Object> lambdaIn) {
        SidecarInput sidecarInput = new SidecarInput();
        forDefinedString(lambdaIn, "point", (s) -> {
            sidecarInput.setPoint(SidecarInputPoint.valueOf(s));
        });

        forDefinedString(lambdaIn, "synchronicity", (s) -> {
            sidecarInput.setSynchronicity(SidecarSynchronicity.valueOf(s));
        });

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

        forDefinedObjectMap(lambdaIn, "operation", (rMap) -> {
            readOperation(sidecarInput, rMap);
        });
        forDefinedObjectMap(lambdaIn, "token", (rMap) -> {
            readToken(sidecarInput, rMap);
        });

        forDefinedObjectMap(lambdaIn, "routing", (rMap) -> {
            readRouting(sidecarInput, rMap);
        });

        forDefinedString(lambdaIn, "serviceId", sidecarInput::setServiceId);
        forDefinedString(lambdaIn, "endpointId", sidecarInput::setEndpointId);
        forDefinedString(lambdaIn, "remoteAddress", sidecarInput::setRemoteAddress);


        forDefinedObjectMap(lambdaIn, "params", sidecarInput::addAllParams);

        return sidecarInput;
    }

    public static void forEachObjectMapIn(Map<String,Object> yaml, BiConsumer<String, Map<String,Object>> c) {
        yaml.forEach((key, value) -> {
            Map<String,Object> yamlVal = (Map<String,Object>) value;
            c.accept(key, yamlVal);
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
        forDefinedString(lambdaIn, "expires", (v) -> {
            try {
                token.setExpires(AFKLMSidecarProcessor.jsonDate.parse(v));
            } catch (ParseException ex) {
                // Do we need to catch it?
            }
        });


        sidecarInput.setToken(token);
    }

    private static void readRouting(SidecarInput sidecarInput, Map<String, Object> lambdaIn) {
        SidecarInputRouting r = new SidecarInputRouting();
        forDefinedString(lambdaIn, "httpVerb", r::setHttpVerb);
        forDefinedString(lambdaIn, "uri", r::setUri);

        sidecarInput.setRouting(r);
    }
}
