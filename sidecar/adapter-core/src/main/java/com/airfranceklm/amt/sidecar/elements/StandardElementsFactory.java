package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.elements.impl.*;
import com.airfranceklm.amt.sidecar.filters.NumericAlgorithmsCreators;
import com.airfranceklm.amt.sidecar.filters.StringAlgorithmsCreators;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.ElementFilterDemand;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.*;

/**
 * Factory centralizing creation of the individual elements along with the filtering.
 */
public class StandardElementsFactory implements ElementsFactory {

    private static Map<String, Function<String, DataElement<ProcessorEvent, ?>>> commonElements = new HashMap<>();
    private static Map<String, Function<String, DataElement<PreProcessEvent, ?>>> preProcessorElements = new HashMap<>();
    private static Map<String, Function<String, DataElement<PostProcessEvent, ?>>> postProcessElements = new HashMap<>();

    private static Map<String, Class<?>> elementTypes = new HashMap<>();
    private static Map<Class<?>, Map<String, ElementFilterCreator<?>>> matchAlgorithms = new HashMap<>();

    private static Map<String, DataElementNormalizer<?>> normalizers = new HashMap<>();

    static {
        Builder b = new Builder();

        CommonElementsFactory.fill(b);
        TokenElementsFactory.fill(b);
        UserContextFieldFactory.fill(b);
        EAVElementsFactory.fill(b);
        OperationElementsFactory.fill(b);
        HeaderElementsFactory.fill(b);
        RoutingFactory.fill(b);
        PayloadElementsFactory.fill(b);
        RelayElementFactory.fill(b);

        NumericAlgorithmsCreators.fill(b);
        StringAlgorithmsCreators.fill(b);

        NormalizersFactory.fill(b);
    }

    static class Builder implements ElementsFactoryBuilder {
        @Override
        public void addCommonElement(ElementSpec ne, Function<String, DataElement<ProcessorEvent, ?>> creator) {
            commonElements.put(ne.getElementName(), creator);
            elementTypes.put(ne.getElementName(), ne.getElementClass());
        }

        @Override
        public void addPreProcessorElement(ElementSpec ne, Function<String, DataElement<PreProcessEvent, ?>> creator) {
            preProcessorElements.put(ne.getElementName(), creator);
            elementTypes.put(ne.getElementName(), ne.getElementClass());
        }

        @Override
        public void addPostProcessorElement(ElementSpec ne, Function<String, DataElement<PostProcessEvent, ?>> creator) {
            postProcessElements.put(ne.getElementName(), creator);
            elementTypes.put(ne.getElementName(), ne.getElementClass());
        }

        @Override
        public <T> void addFilter(@NonNull String alg, @NonNull Class<T> type, @NonNull ElementFilterCreator<T> creator) {
            Map<String, ElementFilterCreator<?>> store;

            if (!matchAlgorithms.containsKey(type)) {
                store = new HashMap<>();
                matchAlgorithms.put(type, store);
            } else {
                store = matchAlgorithms.get(type);
            }

            store.put(alg, creator);
        }

        @Override
        public <T> void addNormalizer(String name, DataElementNormalizer<T> norm) {
            normalizers.put(name, norm);
        }

        @Override
        public <T> void addNormalizer(String name, AtomicDataElementNormalizer<T> norm) {
            normalizers.put(name, norm.normalize());
        }
    }

    @SuppressWarnings("unchecked")
    public static DataElement<ProcessorEvent, ?> createCommon(@NonNull ElementDemand cfg) throws DataElementException {
        Function<String, DataElement<ProcessorEvent, ?>> fCommon = commonElements.get(cfg.getName());
        if (fCommon == null) {
            throw new UnknownDataElementException(cfg.getName());
        } else {
            final DataElement<ProcessorEvent, Object> retVal = (DataElement<ProcessorEvent, Object>) fCommon.apply(cfg.getParameter());

            if (cfg.hasFilters()) {
                if (retVal instanceof FilterableDataElement) {
                    applyDemandedFilters(cfg, (FilterableDataElement<ProcessorEvent, Object>) retVal);
                } else {
                    throw new DataElementException(String.format("Common element %s doesn't support filtering", cfg.getName()));
                }
            }

            if (cfg.hasNormalizers()) {
                if (retVal instanceof NormalizableDataElement) {
                    applyDemandedNormalizers(cfg, (NormalizableDataElement<Object>) retVal);
                } else {
                    throw new DataElementException(String.format("Common element %s doesn't support normalization", cfg.getName()));
                }
            }


            return retVal;
        }
    }

    private static DataElementRelevance inferNoFiltersMatchedRelevance(ElementDemand cfg) {
        DataElementRelevance retVal = Ignored;

        int matchScopesCount = 0;
        int requireCount = 0;

        if (cfg.getFilters() != null) {
            for (ElementFilterDemand f : cfg.getFilters()) {
                switch (f.getIntent()) {
                    case MatchScopes:
                        matchScopesCount++;
                        break;
                    case MatchRequired:
                        requireCount++;
                        break;
                    case MatchDescopes:
                    case MatchProhibits:
                    default:
                        // These targets don't influence the selection
                        break;
                }
            }
        }

        // If the configuration requires
        if (requireCount > 0) {
            return ClientError;
        } else if (matchScopesCount > 0) {
            return Noop;
        }

        return Invoke;
    }

    public static <EType extends ProcessorEvent, T> void addFilterTo(DataElement<EType, T> element,
                                                                     Function<T, Boolean> matcher,
                                                                     DataElementFilterIntent intent, String label) throws DataElementException {

        if (element instanceof FilterableDataElement) {
            Function<T, DataElementRelevance> filter = DataElementFilters.translateIntent(matcher, intent);
            ((FilterableDataElement<EType, T>) element).addFilter(filter, label);
        } else {
            throw new DataElementException(String.format("Element %s doesn't support filtering", element.getElementSpec().getElementName()));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ElementFilterCreator<T> getFilterAlgorithm(Class<T> clazz, String alg) {
        Map<String, ElementFilterCreator<?>> m = matchAlgorithms.get(clazz);
        if (m != null) {
            return (ElementFilterCreator<T>) m.get(alg);
        } else {
            return null;
        }
    }

    protected static void applyDemandedNormalizers(ElementDemand ed, NormalizableDataElement<Object> retVal) throws UnknownNormalizerAlgorithmException {
        if (ed.hasNormalizers()) {
            for (String name : ed.getNormalizers()) {
                DataElementNormalizer<?> f = normalizers.get(name);
                if (f != null) {
                    retVal.addNormalizer((DataElementNormalizer<Object>) f);
                } else {
                    throw new UnknownNormalizerAlgorithmException(name);
                }
            }
        }
    }

    protected static void applyDemandedFilters(ElementDemand cfg, FilterableDataElement<? extends ProcessorEvent, Object> retVal) throws UnknownFilterAlgorithmException, IllegalFilterExpressionException {
        if (cfg.hasFilters()) {
            Class<?> type = elementTypes.get(cfg.getName());
            Map<String, ElementFilterCreator<?>> typeAlgorithms = matchAlgorithms.get(type);

            if (typeAlgorithms != null) {
                for (ElementFilterDemand efd : cfg.getFilters()) {
                    if (!typeAlgorithms.containsKey(efd.getAlgorithm())) {
                        throw new UnknownFilterAlgorithmException(cfg.getName(),
                                "Unknown match algorithm",
                                efd.getAlgorithm());
                    }
                }

                String elemName = cfg.getParameter() == null ? cfg.getName() : String.format("%s(%s)", cfg.getName(), cfg.getParameter());

                // At this point, all algorithms are valid.
                for (ElementFilterDemand efd : cfg.getFilters()) {
                    ElementFilterCreator<?> baseCreator = typeAlgorithms.get(efd.getAlgorithm());
                    Predicate<?> base = baseCreator.create(elemName, efd.getExpression());
                    if (efd.isNegate()) {
                        base = base.negate();
                    }

                    Function<?, DataElementRelevance> filter = DataElementFilters.translateIntent(base::test, efd.getIntent());
                    retVal.addFilter((Function<Object, DataElementRelevance>) filter, efd.getLabel());
                }

                DataElementRelevance noMatch = cfg.getNoFiltersMatched();
                if (noMatch == null) {
                    noMatch = inferNoFiltersMatchedRelevance(cfg);
                }

                retVal.setOnNoFilterMatched(noMatch);

            } else {
                throw new UnknownFilterAlgorithmException(cfg.getName(),
                        String.format("No match algorithms known for type %s", type.getName()), null);
            }
        }
    }


    public static DataElement<? super PreProcessEvent, ?> createForPreProcessor(ElementDemand cfg) throws DataElementException {
        DataElement<? super PreProcessEvent, Object> retVal = null;

        if (preProcessorElements.containsKey(cfg.getName())) {
            Function<String, DataElement<PreProcessEvent, ?>> fPre = preProcessorElements.get(cfg.getName());
            retVal = (DataElement<? super PreProcessEvent, Object>) fPre.apply(cfg.getParameter());

            if (cfg.hasFilters()) {
                if (retVal instanceof FilterableDataElement) {
                    final FilterableDataElement<? extends PreProcessEvent, Object> filterableEvt = (FilterableDataElement<? extends PreProcessEvent, Object>) retVal;
                    applyDemandedFilters(cfg, filterableEvt);
                } else {
                    throw new DataElementException(String.format("Pre-processor element %s doesn't support filtering", cfg.getName()));
                }
            }

            if (cfg.hasNormalizers()) {
                if (retVal instanceof NormalizableDataElement) {
                    applyDemandedNormalizers(cfg, (NormalizableDataElement<Object>) retVal);
                } else {
                    throw new DataElementException(String.format("Pre-processor element %s doesn't support normalization", cfg.getName()));
                }
            }

            return retVal;
        } else {
            return createCommon(cfg);
        }
    }

    public static DataElement<? super PostProcessEvent, ?> createForPostProcessor(ElementDemand cfg) throws DataElementException {
        if (postProcessElements.containsKey(cfg.getName())) {
            Function<String, DataElement<PostProcessEvent, ?>> fPre = postProcessElements.get(cfg.getName());
            final DataElement<PostProcessEvent, Object> retVal = (DataElement<PostProcessEvent, Object>) fPre.apply(cfg.getParameter());

            if (cfg.hasFilters()) {
                if (retVal instanceof FilterableDataElement) {
                    final FilterableDataElement<? extends ProcessorEvent, Object> filterablePostEvt = (FilterableDataElement<? extends ProcessorEvent, Object>) retVal;
                    applyDemandedFilters(cfg, filterablePostEvt);
                } else {
                    throw new DataElementException(String.format("Post-processor element %s doesn't support filtering", cfg.getName()));
                }
            }

            if (cfg.hasNormalizers()) {
                if (retVal instanceof NormalizableDataElement) {
                    applyDemandedNormalizers(cfg, (NormalizableDataElement<Object>) retVal);
                } else {
                    throw new DataElementException(String.format("Post-processor element %s doesn't support normalization", cfg.getName()));
                }
            }

            return retVal;
        } else {
            return createCommon(cfg);
        }
    }

    // ------------------------------------------------------------------------------
    // Dynamic methods.

    @Override
    public DataElement<? super PreProcessEvent, ?> produceForPreProcessor(ElementDemand cfg) throws DataElementException {
        return createForPreProcessor(cfg);
    }

    @Override
    public DataElement<? super PostProcessEvent, ?> produceForPostProcessor(ElementDemand cfg) throws DataElementException {
        return createForPostProcessor(cfg);
    }
}
