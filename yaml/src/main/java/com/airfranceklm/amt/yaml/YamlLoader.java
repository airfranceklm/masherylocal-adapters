package com.airfranceklm.amt.yaml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.airfranceklm.amt.yaml.YamlHelper.*;

/**
 * Loads an object from Map based on the supplied annotations.
 */
public class YamlLoader {
    private static final String DEFAULT_CONTEXT = "";

    private static DateFormat[] dateFormats = new DateFormat[3];
    static {
        final SimpleDateFormat gmtTimeZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        gmtTimeZone.setTimeZone(TimeZone.getTimeZone("Zulu"));

        dateFormats[0] = gmtTimeZone;
    }

    public static <T> T loadFromYAML(Map<String,?> source, Class<T> returnValue) throws IllegalAccessException, InstantiationException {
        if (source == null) {
            return null;
        }

        // Let's load something.
        T retVal = returnValue.newInstance();
        for (Method m: returnValue.getMethods()) {
            if (m.getParameterCount() != 1) {
                continue;
            }

            // Binding annotation: binds a single key in the configuration to the
            // particular setter.
            YamlBinding bnd = m.getAnnotation(YamlBinding.class);
            if (bnd != null) {
                if (DEFAULT_CONTEXT.equals(bnd.context())) {
                    processAnnotation(source, retVal, m, bnd);
                } else {
                    forDefinedObjectMap(source, bnd.context(), (val) -> processAnnotation(val, retVal, m, bnd));
                }
            }

            // Yaml receiver. Will receive the whole document.
            YamlReceiver rcv = m.getAnnotation(YamlReceiver.class);
            if (rcv != null) {
                if (m.getParameterTypes()[0] == Map.class) {
                    final String typeName = ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[1].getTypeName();
                    Class v = null;

                    try {
                        v = Class.forName(typeName);
                    } catch (ClassNotFoundException e) {
                        // Swallow it?
                        continue;
                    }

                    if (v == Object.class) {
                        invokeSetter(m, retVal, source);
                    } else {
                        loadObjectsOf(source, v, retVal, m);
                    }
                }
            }
        }

        return retVal;
    }

    private static <T> void processAnnotation(Map<String, ?> source, T retVal, Method m, YamlBinding bnd) {
        Class pType = m.getParameterTypes()[0];
        if (pType.equals(Integer.class) || pType.equals(Integer.TYPE)) {
            forDefinedInteger(source, bnd.value(), (val) -> invokeSetter(m, retVal, val));
        } else if (pType.equals(Boolean.class) || pType.equals(Boolean.TYPE)) {
            forDefinedBoolean(source, bnd.value(), (val) -> invokeSetter(m, retVal, val));
        } else if (pType.equals(String.class)) {
            forDefinedString(source, bnd.value(), (val) -> invokeSetter(m, retVal, val));
        } else if (pType.equals(Map.class) || Map.class.isAssignableFrom(pType)) {
            Class collType = bnd.collectionType();
            if (isObject(collType)) {
                // Try interring the type from the collection.
                String typeName = ((ParameterizedType)m.getGenericParameterTypes()[0]).getActualTypeArguments()[1].getTypeName();
                Class defCls = safeClassForName(typeName);
                if (defCls != null) {
                    collType = defCls;
                }
            }

            if (isObject(collType)) {
                forDefinedObjectMap(source, bnd.value(), (val) -> invokeSetter(m, retVal, val));
            } else if (isYamlReadable(collType)) {
                Object rawObj = source.get(bnd.value());
                if (rawObj instanceof Map) {
                    invokeSetter(m, retVal, loadMap((Map<?,?>)rawObj, collType));
                }
            } else {
                ensureMapContentTypeFor(source, bnd.value(), collType, (safeMap) -> invokeSetter(m, retVal, safeMap));
            }


        } else if (pType.equals(List.class) || List.class.isAssignableFrom(pType)) {
            Class collType = bnd.collectionType();
            if (isObject(collType)) {
                // Try interring the type from the collection.
                String typeName = ((ParameterizedType)m.getGenericParameterTypes()[0]).getActualTypeArguments()[0].getTypeName();
                Class defCls = safeClassForName(typeName);
                if (defCls != null) {
                    collType = defCls;
                }
            }

            if (isObject(collType)) {
                forDefinedObjectList(source, bnd.value(), (val) -> invokeSetter(m, retVal, val));
            } else if (isYamlReadable(collType)) {
                Object lo = source.get(bnd.value());
                if (lo instanceof List) {
                    invokeSetter(m, retVal, loadList((List)lo, collType));
                }
            } else {
                ensureListContentTypeFor(source, bnd.value(), collType, (l) -> invokeSetter(m, retVal, l));
            }

//
        } else if (pType.equals(Date.class)) {
            // If YAML doesn't define this explicitly as date,
            // we will attempt parsing to ay known format.
            if (!forDefinedDate(source, bnd.value(), (dt) -> invokeSetter(m, retVal, dt))) {
                forDefinedString(source, bnd.value(), (str) -> {
                    Date dt = parseDate(str);
                    if (dt != null) {
                        invokeSetter(m, retVal, dt);
                    }
                });
            }
        } else {
           Map<String,?> yaml = getObjectMap(source, bnd.value());
           Object nestedBean = null;
           try {
               nestedBean = loadFromYAML(yaml, pType);
           } catch (IllegalAccessException | InstantiationException e) {
               // Swallow
           }

            if (nestedBean != null) {
               invokeSetter(m, retVal, nestedBean);
           }
        }
    }

    private static Map<Object,Object> loadMap(Map<?, ?> cMap, Class collType) {
        Map<Object,Object> converted = new LinkedHashMap();

        for (Map.Entry<?,?> entry: cMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                try {
                    Object loaded = loadFromYAML((Map<String,?>)entry.getValue(), collType);
                    if (loaded != null) {
                        converted.put(entry.getKey(), loaded);
                    }
                } catch (IllegalAccessException | InstantiationException e) {
                    // Swallow it; todo: capture the errors.
                }
            }
        }

        return converted;
    }

    @SuppressWarnings("unchecked")
    private static List loadList(List l, Class collType) {
        List mapped = new ArrayList(l.size());

        for (Object o : l) {
            if (o instanceof Map) {
                try {
                    Object yamlLoaded = loadFromYAML((Map<String,?>)o, collType);
                    if (yamlLoaded != null) {
                        mapped.add(yamlLoaded);
                    }
                } catch (IllegalAccessException | InstantiationException e) {
                    // Swallow; TODO -> report this.
                }
            }
        }
        return mapped;
    }

    private static boolean isObject(Class collType) {
        return Object.class.equals(collType);
    }

    private static boolean isString(Class collType) {
        return String.class.equals(collType);
    }

    private static boolean isYamlReadable(Class collType) {
        return collType.getAnnotation(YamlReadable.class) != null;
    }

    private static <T> void loadObjectsOf(Map<String, ?> val, Class returnValue, T retVal, Method m) {
        Map recastMap = new LinkedHashMap();
        val.forEach((key, value) -> {
            if (value instanceof Map) {
                try {
                    recastMap.put(key, loadFromYAML((Map)value, returnValue));
                } catch (IllegalAccessException | InstantiationException e) {
                    // Swallow.
                }
            }
        });
        invokeSetter(m, retVal, recastMap);
    }

    private static Date parseDate(String str) {
        for (DateFormat df : dateFormats) {
            try {
                return df.parse(str);
            } catch (ParseException e) {
                // Ignore.
            }
        }
        return null;
    }

    private static void invokeSetter(Method m, Object target, Object value) {
        try {
            m.invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Swallowed
        }
    }

    private static Class safeClassForName(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
