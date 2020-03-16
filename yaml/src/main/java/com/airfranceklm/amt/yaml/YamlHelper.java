package com.airfranceklm.amt.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Collection of the helper methods for parsing
 */
public class YamlHelper {

    @SuppressWarnings("unchecked")
    public static void forEachInArray(Object pList, Consumer<Object> c) {
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
    public static void forObjectMap(Object obj, Consumer<Map<String, Object>> c) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map) {
            c.accept((Map<String,Object>)obj);
        }
    }

    public static void forDefinedLong(Map<String, Object> cfg, String key, Consumer<Long> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Long) {
            lambda.accept((Long) t);
        }
    }

    public static void forDefinedInteger(Map<String, ?> cfg, String key, Consumer<Integer> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Number) {
            lambda.accept(((Number) t).intValue());
        }
    }

    public static boolean forDefinedString(Map<String, ?> cfg, String key, Consumer<String> lambda) {
        Object t = cfg.get(key);
        if (t instanceof String) {
            lambda.accept((String) t);
            return true;
        } else {
            return false;
        }
    }

    public static boolean forDefinedDate(Map<String, ?> cfg, String key, Consumer<Date> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Date) {
            lambda.accept((Date) t);
            return true;
        } else {
            return false;
        }
    }

    public static void forDefinedBoolean(Map<String, ?> cfg, String key, Consumer<Boolean> lambda) {
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
    public static void forEachInDefinedStringList(Map<String, Object> cfg, String key, Consumer<String> lambda) {
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

    /**
     * Ensure that that the list contained is of the correct content type.
     * @param yaml yaml object
     * @param key key in the yaml object to process
     * @param reqType type the list should contain
     * @param consumer consumer to be called, if list is defined and types are all checked.
     * @param <T> type the list should contain
     * @return result of the check.
     */
    @SuppressWarnings("unchecked")
    public static <T> ElementMappingResult ensureListContentTypeFor(Map<String,?> yaml, String key, Class<T> reqType, Consumer<List<T>> consumer) {
        if (yaml == null) {
            return ElementMappingResult.WasNull;
        }
        if (yaml.containsKey(key)) {
            Object v = yaml.get(key);
            if (v != null) {
                return ensureListContentsFor(v, reqType, consumer);
            } else {
                return ElementMappingResult.WasNull;
            }
        } else {
            return ElementMappingResult.NotDefined;
        }
    }

    protected static <T> ElementMappingResult ensureListContentsFor(Object v, Class<T> reqType, Consumer<List<T>> consumer) {
        if (v instanceof List) {
            List l = (List)v;
            for (Object o: l) {
                if (!reqType.isInstance(o)) {
                    return ElementMappingResult.OffendingContent;
                }
            }
            // Checks were okeys;
            consumer.accept((List<T>)l);
            return ElementMappingResult.Processed;
        } else {
            return ElementMappingResult.WrongType;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ElementMappingResult ensureMapContentTypeFor(Map<String,?> yaml, String key, Class<T> reqType, Consumer<Map<String,T>> consumer) {
        if (yaml == null) {
            return ElementMappingResult.WasNull;
        }
        if (yaml.containsKey(key)) {
            Object v = yaml.get(key);
            if (v != null) {
                if (v instanceof Map) {
                    Map<?,?> l = (Map<?,?>)v;
                    for (Map.Entry<?,?> entry: l.entrySet()) {
                        if (!reqType.isInstance(entry.getValue())) {
                            return ElementMappingResult.OffendingContent;
                        }
                    }
                    // Checks were okeys;
                    consumer.accept((Map<String,T>)l);
                    return ElementMappingResult.Processed;
                } else {
                    return ElementMappingResult.WrongType;
                }
            } else {
                return ElementMappingResult.WasNull;
            }
        } else {
            return ElementMappingResult.NotDefined;
        }
    }

    @SuppressWarnings("unchecked")
    public static void forDefinedStringList(Map<String, ?> cfg, String key, Consumer<List<String>> lambda) {
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

    @SuppressWarnings("unchecked")
    public static void forDefinedObjectList(Map<String, ?> cfg, String key, Consumer<List<Object>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof List) {
            lambda.accept((List<Object>) t);
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
    public static void forDefinedStringMap(Map<String, ?> cfg, String key, Consumer<Map<String, String>> lambda) {
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
    public static void forDefinedObjectMap(Map<String, ?> cfg, String key, Consumer<Map<String, Object>> lambda) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            lambda.accept((Map<String, Object>) t);
        }
    }


    @SuppressWarnings("unchecked")
    public static Map<String,?> getObjectMap(Map<String,?> cfg, String key) {
        Object t = cfg.get(key);
        if (t instanceof Map) {
            return (Map<String,?>)t;
        } else {
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> void filterOutNull(Map<String, Object> cfg, String key, Consumer<T> lambda) {
        T t = (T) cfg.get(key);
        if (t != null) {
            lambda.accept(t);
        }
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
    public static void forEachNamedArrayIn(Map<String, Object> yaml, BiConsumer<String, List<Object>> c) {
        yaml.forEach((key, value) -> {
            if (value instanceof List) {
                c.accept(key, (List<Object>)value);
            }
        });
    }

    public static Iterator<Object> loadAllYamlDocuments(File f) {
        try (InputStream is = new FileInputStream(f)) {
            return loadAllYamlDocuments(is);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Loads all Yaml documents into memory.
     * @param ownerClass reference class
     * @param resource resource of that class to load
     * @return Iterator of yaml documents, or null if resource was not possible.
     */
    public static Iterator<Object> loadAllYamlDocuments(Class<?> ownerClass, String resource) throws IOException {
        try (InputStream is = ownerClass.getResourceAsStream(resource)) {
            if (is != null) {
                return loadAllYamlDocuments(is);
            } else {
                return null;
            }
        }
    }

    public static Iterator<Object> loadAllYamlDocuments(InputStream is) {
        ArrayList<Object> loadedDocs = new ArrayList<>();
        Iterator<Object> rawIter =  new Yaml().loadAll(new InputStreamReader(is)).iterator();
        rawIter.forEachRemaining(loadedDocs::add);

        return loadedDocs.iterator();
    }

    /**
     * Loads all Yaml documents into memory.
     * @param clazz reference class
     * @param resource resource of that class to load
     * @return Iterator of yaml documents, or null if resource was not possible.
     */
    @SuppressWarnings("unchecked")
    public static <V> V loadSingleYamlDocument(Class<?> clazz, String resource) throws IOException {
        try (InputStream is = clazz.getResourceAsStream(resource)) {
            if (is != null) {
                return loadSingleYamlDocument(is);
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> V loadSingleYamlDocument(InputStream is) {
        return (V)new Yaml().load(new InputStreamReader(is));
    }

    /**
     * Retrieves next YAML document from the structure loaded with {@link #loadAllYamlDocuments(Class, String)}.
     * @param obj iterator object
     * @return next instance in the iterator, if found. Otherwise null will be returned.
     */
    public static Object nextYamlDocument(Iterator<Object> obj) {
        if (obj == null) {
            return null;
        } else if (obj.hasNext()) {
            return obj.next();
        } else {
            return null;
        }
    }

    /**
     * Retrieves next YAML document from the structure loaded with {@link #loadAllYamlDocuments(Class, String)}.
     * @param obj iterator object
     * @return next instance in the iterator, if found. Otherwise null will be returned.
     */
    @SuppressWarnings("unchecked")
    public static Map<String,?> nextYamlMap(Iterator<Object> obj) {
        if (obj == null) {
            return null;
        } else if (obj.hasNext()) {
            Object retVal = obj.next();
            if (retVal instanceof Map) {
                return (Map<String,?>)retVal;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Yaml yamlForNiceOutput() {
        DumperOptions theOp = new DumperOptions();
        theOp.setPrettyFlow(true);
        theOp.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(theOp);
    }

    public static void saveAsMultidocYaml(List<Map<?,?>> objects, File into) throws IOException {
        try (FileWriter fw = new FileWriter(into)) {
            DumperOptions theOp = new DumperOptions();
            theOp.setPrettyFlow(true);
            theOp.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            fw.write(yamlForNiceOutput().dumpAll(objects.iterator()));
        }
    }

    /**
     * Saves the YAML object into a file
     * @param yamlCfg YAML object
     * @param to destination file
     * @throws IOException if the I/O operation will fail.
     */
    public static void saveAsYaml(Map<?,?> yamlCfg, File to) throws IOException {
        try(FileWriter fos = new FileWriter(to)) {
            fos.write(yamlStringOf(yamlCfg));
        }
    }

    public static void saveAsYaml(Object obj, File to) throws IOException {
        try (FileWriter fw = new FileWriter(to)) {
            fw.write(yamlStringOf(obj));
        }
    }

    /**
     * Returns the YAML string.
     * @param m map to convert
     * @return String representing the YAML map.
     */
    public static String yamlStringOf(Object m) {
        return yamlForNiceOutput().dumpAsMap(m);
    }
}
