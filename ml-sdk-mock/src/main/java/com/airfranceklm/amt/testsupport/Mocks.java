package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.ContentProducerArgumentsMatcher;
import com.airfranceklm.amt.testsupport.mocks.ContentProducerBinaryArgumentsMatcher;
import com.airfranceklm.amt.testsupport.mocks.StringCaseInsensitiveMatcher;
import com.mashery.http.io.ContentProducer;
import com.mashery.trafficmanager.model.core.ExtendedAttributes;
import lombok.NonNull;
import org.easymock.EasyMockSupport;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class Mocks {

    public static  TreeMap<String, String> asTreeMap(Map<String,String> m) {
        if (m == null) {
            return null;
        } else if (m instanceof TreeMap) {
            return (TreeMap<String,String>)m;
        } else {
            TreeMap<String, String> retVal = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            retVal.putAll(m);
            return retVal;
        }
    }

    /**
     * Joins nullable maps
     * @param maps Maps to join
     * @param <K> key
     * @param <V> value
     * @return map containing the values from all. If all maps are null, then null will be returnd.
     */
    @SafeVarargs
    static <K, V> Map<K,V> joinNullableMaps(Map<K, V>... maps) {
        Map<K, V> retVal = null;
        for (Map<K,V> m: maps) {
            if (m != null) {
                if (retVal == null) {
                    retVal = new HashMap<>();
                }
                retVal.putAll(m);
            }
        }

        return retVal;
    }

    static String computeRequestURI(@NonNull String base, String resource, Map<String,String> pathParam, Map<String, String> queryString){
        String uBase = base;
        if (pathParam != null) {
            for (Map.Entry<String,String> pp : pathParam.entrySet()) {
                uBase = uBase.replaceAll(String.format("{%s}", pp.getKey()), pp.getValue());
            }
        }

        StringBuilder sb = new StringBuilder(uBase);
        if (resource != null) {
            if (!uBase.endsWith("/") && !resource.startsWith("/")) {
                sb.append("/");
            }
            sb.append(resource);
        }

        if (queryString != null && queryString.size() > 0) {

            List<String> params = new ArrayList<>(queryString.size());
            params.addAll(queryString.keySet());
            Collections.sort(params);

            sb.append("?");
            int cnt = 0;
            for (String p: params) {
                if (cnt > 0) {
                    sb.append("&");
                }
                sb.append(urlEnc(p)).append("=").append(urlEnc(queryString.get(p)));
                cnt++;
            }
        }

        return sb.toString();
    }

    private static String urlEnc(@NonNull String base)  {
        try {
            return URLEncoder.encode(base, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            fail(ex.getMessage());

            // Code is unreachable
            throw new IllegalStateException("Unreachable code");
        }
    }

    static ExtendedAttributes extendedAttributesFrom(EasyMockSupport owner, Map<String, String> params) {
        ExtendedAttributes retVal = owner.createMock(ExtendedAttributes.class);
        if (params != null) {
            params.forEach((key, value) -> {
                expect(retVal.getValue(key)).andReturn(value).anyTimes();
            });
        }

        // Attempt to read a non-existing EAV will return null.
        expect(retVal.getValue(anyString())).andReturn(null).anyTimes();

        return retVal;
    }

    public static <T> void forEachNotNull(List<T> coll, Consumer<T> c) {
        if (coll != null) {
            for (T t: coll) {
                if (t != null) {
                    c.accept(t);
                }
            }
        }
    }

    public static <K, V> void forEachNotNull(Map<K, V> coll, BiConsumer<K, V> c) {
        if (coll != null) {
            for (Map.Entry<K, V> t: coll.entrySet()) {
                if (t != null) {
                    c.accept(t.getKey(), t.getValue());
                }
            }
        }
    }

    public static <U, T extends Collection<U>> void copyIfNullCollection(Supplier<T> getter, Supplier<T> source, Consumer<T> setter, Supplier<T> createor) {
        if (getter.get() == null) {
            T t = source.get();
            if (t != null); {
                T copied = createor.get();
                assertNotNull(copied);

                copied.addAll(t);
                setter.accept(copied);
            }
        }
    }

    public static <U, V, T extends Map<U, V>> void copyIfNullMap(Supplier<T> getter, Supplier<T> source, Consumer<T> setter, Supplier<T> createor) {
        if (getter.get() == null) {
            T t = source.get();
            if (t != null) {
                T copied = createor.get();
                assertNotNull(copied);

                copied.putAll(t);
                setter.accept(copied);
            }
        }
    }

    public static <U, T extends Collection<U>> void cloneNullableCollection(Supplier<T> supplier, Consumer<T> setter, Supplier<T> creator) {
        T t = supplier.get();
        if (t == null) {
            setter.accept(null);
        } else {
            T copied = creator.get();
            assertNotNull(copied);

            copied.addAll(t);
            setter.accept(copied);
        }
    }

    public static <U, V, T extends Map<U, V>> void cloneNullableMap(Supplier<T> supplier, Consumer<T> setter, Supplier<T> creator) {
        T t = supplier.get();
        if (t == null) {
            setter.accept(null);
        } else {
            T copied = creator.get();
            assertNotNull(copied);

            copied.putAll(t);
            setter.accept(copied);
        }
    }

    public static <T> void copyValue(@NonNull Supplier<T> thatGetter, @NonNull Consumer<T> thisConsumer) {
        T t = thatGetter.get();
        thisConsumer.accept(t);
    }

    public static <T> void copyIfNull(@NonNull Supplier<T> thisGetter, @NonNull Supplier<T> thatGetter, @NonNull Consumer<T> thisConsumer) {
        T t = thisGetter.get();
        if (t == null) {
            final T other = thatGetter.get();
            if (other != null) {
                thisConsumer.accept(other);
            }
        }
    }

    public static <T> T allocOrGet(@NonNull Supplier<T> getter, @NonNull Consumer<T> setter, @NonNull Supplier<T> creator) {
        T t = getter.get();
        if (t == null) {
            t = creator.get();
            setter.accept(t);
        }

        return t;
    }

    /**
     * Checks that the content producer will yield the expected string, exactly as specified.
     *
     * @param str expected string
     * @return mock stub.
     */
    public static ContentProducer contentProducerYielding(String str) {
        reportMatcher(new ContentProducerArgumentsMatcher(str));
        return null;
    }

    public static ContentProducer contentProducerYielding(byte[] data) {
        reportMatcher(new ContentProducerBinaryArgumentsMatcher(data));
        return null;
    }

    public static String eqCaseInsensitive(String exp) {
        Objects.requireNonNull(exp);
        reportMatcher(new StringCaseInsensitiveMatcher(exp));
        return null;
    }

    @SafeVarargs
    public static <T> T nonNullOf(T... values) {
        for (T t: values) {
            if (t != null) {
                return t;
            }
        }
        fail("A non-null object or a default should have been provided");
        throw new IllegalStateException("Unreachable code");
    }

    @SafeVarargs
    public  static <T>void assertAllEquals(T exp, T... comp) {
        assertNotNull(comp);

        for (T t: comp) {
            assertEquals(exp, t);
        }
    }
}
