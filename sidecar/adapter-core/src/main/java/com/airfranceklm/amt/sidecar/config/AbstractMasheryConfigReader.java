package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.MasheryConfigurationReader;
import com.airfranceklm.amt.sidecar.model.*;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.Getter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.CommonExpressions.splitStandardValueList;

public abstract class AbstractMasheryConfigReader<SCType extends SidecarConfiguration>
    implements MasheryConfigurationReader<SCType> {

    protected static final Pattern numberPattern = Pattern.compile("\\d+");
    protected static final Pattern floatPattern = Pattern.compile("\\d+\\.\\d+");
    protected static final Pattern booleanPattern = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    private int errorCount;

    private List<String> parseMessages;

    private List<Converter> firstPass;
    private List<SecondPassConverter> secondPass;
    private List<String> relevantKeys;
    private Function<ProcessorEvent, Map<String,String>> configLocator;

    private String prefix;

    public AbstractMasheryConfigReader() {
        this.firstPass = new ArrayList<>();
        this.secondPass = new ArrayList<>();

        configLocator = this::locatePreProcessor;
    }

    public AbstractMasheryConfigReader(String prefix) {
        this();
        this.prefix = prefix;
    }

    @Override
    public SCType read(ProcessorEvent pe) {
        reset();

        SCType cfg = create();
        readPointReference(cfg, pe);
        relevantKeys = readConfiguration(cfg, configLocator.apply(pe));

        return cfg;
    }

    public SCType read(Map<String,String> cfg) {
        SCType retVal = create();
        reset();
        relevantKeys = readConfiguration(retVal, cfg);

        return retVal;
    }

    @Override
    public List<String> relevantKeys() {
        return relevantKeys;
    }

    // ---------------------------------------------------------------
    // Implementation methods.

    protected void reset() {
        this.errorCount = 0;
        this.parseMessages = new ArrayList<>();
    }

    protected void readPostProcessorConfiguration() {
        configLocator = this::locatePostProcessor;
    }

    protected Map<String,String> locatePreProcessor(ProcessorEvent pe) {
        return pe.getEndpoint().getProcessor().getPreProcessorParameters();
    }

    protected Map<String,String> locatePostProcessor(ProcessorEvent pe) {
        return pe.getEndpoint().getProcessor().getPostProcessorParameters();
    }

    protected void readPointReference(SCType cfg, ProcessorEvent pp) {
        cfg.setServiceId(pp.getEndpoint().getAPI().getExternalID());
        cfg.setEndpointId(pp.getEndpoint().getExternalID());
    }

    protected void yieldParseMessage(String msg) {
        parseMessages.add(msg);
    }

    /**
     * Reads the supplied configuration, returns the keys that were actually used.
     *
     * @param config configuration object
     * @return List of keys that were recognized by this dialect.
     */
    protected List<String> readConfiguration(SCType theCfg, Map<String, String> config) {
        if (config == null) {
            return Collections.emptyList();
        }

        List<String> matchedKeys = new ArrayList<>();
        Set<String> parsingKeys = new HashSet<>(config.keySet());

        // First-pass: retrieve the information about the key.
        parsingKeys.forEach((key) -> {
            firstPass.forEach((conv) -> {
                String keyToParse = key;

                // If the key specializes, and a prefix is configured, then we should consider matching
                // only if the key begins with this point.
                if (conv.keySpecializes() && prefix != null) {
                    if (key.startsWith(prefix)) {
                        keyToParse = key.substring(prefix.length());
                    } else {
                        return;
                    }
                }

                final Matcher m = conv.getKeyPattern().matcher(keyToParse);

                if (m.matches()) {
                    matchedKeys.add(key);
                    final String value = config.get(key);

                    final int reportedErrors = conv.getConverter().apply(theCfg, m, value);
                    if (reportedErrors != 0) {
                        errorCount += reportedErrors;
                        parseMessages.add(String.format("Key %s is not valid on first-pass", key));
                    }
                }
            });
        });

        parsingKeys.removeAll(matchedKeys);

        // Second-pass

        parsingKeys.forEach((key) -> {
            secondPass.forEach((conv) -> {
                final String keyToParse = prefix != null && conv.keySpecializes()? key.substring(prefix.length()) : key;

                if (conv.getPredicate().test(theCfg, keyToParse)) {
                    matchedKeys.add(key);
                    final String value = config.get(key);

                    final int reportedErrors = conv.getConverter().apply(theCfg, keyToParse, value);
                    if (reportedErrors != 0) {
                        errorCount += reportedErrors;
                        parseMessages.add(String.format("Key %s is not valid on second-pass", key));
                    }
                }
            });
        });


        // Pass messages
        if (parseMessages.size() > 0) {
            theCfg.setMessages(parseMessages);
        }
        theCfg.incrementError(errorCount);

        return matchedKeys;
    }

    protected void addSimple(String regex, KeySpecialization prefixable, SimplePassFunction<SCType> pFunc) {
        add(regex, prefixable, pFunc::yieldScore);
    }

    /**
     * Adds a simple parser with a pre-configured regular expression
     * @param regex regular expression
     * @param pFunc
     */
    protected void addSimple(Pattern regex, KeySpecialization prefixable, SimplePassFunction<SCType> pFunc) {
        add(regex, prefixable, pFunc::yieldScore);
    }

    protected void add(String regex, KeySpecialization prefixable, FirstPassFunction<SCType> pFunc) {
        add(Pattern.compile(regex), prefixable, pFunc);
    }

    protected void add(Pattern compile, KeySpecialization prefixable, FirstPassFunction<SCType> pFunc) {
        this.firstPass.add(new Converter(compile, prefixable, pFunc));
    }

    protected void add(BiPredicate<SCType, String> compile, KeySpecialization ksp, SecondPassFunction<SCType> pFunc) {
        this.secondPass.add(new SecondPassConverter(compile, ksp, pFunc));
    }

    protected abstract SCType create();

    // ----------------------------------------------------------------------------------------------------
    // Methods that are likely to be re-used by the dialects

    protected void forEachLexeme(String exp, Consumer<String> c) {
        forEachInList(splitStandardValueList(exp), c);
    }

    protected <T> void forEachInList(T[] l, Consumer<T> c) {
        if (l != null) {
            for (T t : l) {
                c.accept(t);
            }
        }
    }

    protected Boolean standardBooleanLexeme(String lexem) {
        Matcher m = booleanPattern.matcher(lexem);
        if (m.matches()) {
            if ("true".equalsIgnoreCase(lexem)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            return null;
        }
    }


    // -------------------------------------------------------------------------------------------
    // Converters


    class Converter {

        @Getter Pattern keyPattern;
        @Getter FirstPassFunction<SCType> converter;
        @Getter KeySpecialization specialization;

        Converter(Pattern pattern, FirstPassFunction<SCType> converter) {
            this(pattern, KeySpecialization.CommonKey, converter);
        }

        Converter(Pattern keyPattern, KeySpecialization specialization, FirstPassFunction<SCType> converter) {
            this.keyPattern = keyPattern;
            this.converter = converter;
            this.specialization = specialization;
        }

        boolean keySpecializes() {
            return KeySpecialization.ScopedKey == specialization;
        }
    }

    class SecondPassConverter {
        @Getter BiPredicate<SCType, String> predicate;
        @Getter SecondPassFunction<SCType> converter;
        @Getter KeySpecialization specialization;

        public SecondPassConverter(BiPredicate<SCType, String> predicate
                                   , KeySpecialization specialization
                , SecondPassFunction<SCType> converter) {
            this.predicate = predicate;
            this.converter = converter;
            this.specialization = specialization;
        }

        boolean keySpecializes() {
            return KeySpecialization.ScopedKey == specialization;
        }
    }

    // ------------------------------------------------------------
    // Static methods.

    protected static Object minimumTypeConversion(String value) {
        if (value == null) {
            return null;
        }
        String uVal = value.trim();

        if ("null".equals(uVal)) {
            return null;
        } else if (numberPattern.matcher(uVal).matches()) {
            // Longs will be used only if the value exceeds the maximum.
            Long lv = new Long(uVal);
            if (lv > Integer.MAX_VALUE) {
                return lv;
            } else {
                return lv.intValue();
            }
        } else if (booleanPattern.matcher(uVal).matches()) {
            return Boolean.parseBoolean(uVal);
        } else if (floatPattern.matcher(uVal).matches()) {
            return Double.parseDouble(uVal);
        } else {
            return uVal;
        }
    }
}
