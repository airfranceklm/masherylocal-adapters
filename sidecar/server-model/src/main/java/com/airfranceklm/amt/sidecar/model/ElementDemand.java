package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.elements.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;
import static com.airfranceklm.amt.sidecar.model.ElementVisibility.Input;

/**
 * Demand of the element to be included into the sidecar inputs, together
 * with the applicable filtering conditions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementDemand {

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String parameter;

    @Getter @Setter @Singular
    private List<ElementFilterDemand> filters;

    @Getter @Setter @Builder.Default
    private ElementVisibility visibility = Input;

    @Getter @Setter @Builder.Default
    private DataElementRelevance noFiltersMatched = null;

    @Getter @Setter @Singular
    private List<String> normalizers;

    public ElementDemand(@NonNull ElementSpec es) {
        this(es.getElementName());
    }

    public ElementDemand(@NonNull ElementWithParameterSpec es) {
        this(es.getElementName(), es.getElementParameter());
    }

    public ElementDemand(String name) {
        this(name, null);
    }

    public ElementDemand(String name, String parameter) {
        this.name = name;
        this.parameter = parameter;
    }

    public void addFilter(String basicType, DataElementFilterIntent intent) {
        addFilter(basicType, null, intent);
    }

    public void addFilter(ElementFilterDemand efd) {
        allocateOrGetFilters().add(efd);
    }

    public void addFilter(String basicType, String expr, DataElementFilterIntent intent) {
        allocateOrGetFilters().add(new ElementFilterDemand(basicType,  expr, intent));
    }

    public ElementDemand filtered(Consumer<ElementFilterDemand.ElementFilterDemandBuilder> f) {
        ElementFilterDemand.ElementFilterDemandBuilder b = ElementFilterDemand.demandElementFilter();
        Objects.requireNonNull(f).accept(b);

        allocateOrGetFilters().add(b.build());
        return this;
    }

    public <T extends Enum<T>> ElementDemand normalized(@NonNull Enum<T> norm) {
        return normalized(norm.name());
    }

    public ElementDemand normalized(@NonNull String normalizationAlg) {
        allocOrGet(this::getNormalizers, this::setNormalizers, ArrayList::new).add(normalizationAlg);
        return this;
    }

    public boolean hasFilters() {
        return filters != null && filters.size() > 0;
    }

    protected List<ElementFilterDemand> allocateOrGetFilters() {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        return filters;
    }

    /**
     * Checks if this element demands specified settings
     * @param name name of the element
     * @param param parameter
     * @return true if this element demands it, false otherwise.
     */
    public boolean demands(String name, String param) {
        return Objects.equals(this.name, name) && Objects.equals(this.parameter, param);
    }

    public static ElementDemand elem(@NonNull String elem) {
        return elem(elem, null);
    }

    public static ElementDemand elem(@NonNull ElementSpec ne) {
        return elem(ne.getElementName(), null);
    }

    public static ElementDemand elem(@NonNull ElementSpec ne, String param) {
        return elem(Objects.requireNonNull(ne).getElementName(), param);
    }

    public static ElementDemand elem(@NonNull String name, String param) {
        return new ElementDemand(Objects.requireNonNull(name), param);
    }

    public boolean isFor(ElementSpec elemSpec) {
        return demands(elemSpec.getElementName(), null);
    }

    public boolean isFor(ElementSpec elemSpect, String param) {
        return demands(elemSpect.getElementName(), param);
    }

    public boolean eitherOf(ElementSpec... elems) {
        for (ElementSpec ne: elems) {
            if (Objects.equals(getName(), ne.getElementName())) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFilter(String alg, DataElementFilterIntent filterIntent) {
        if (filters != null) {
            return filters.stream()
                    .filter((f) -> alg.equals(f.getAlgorithm()) && filterIntent.equals(f.getIntent()))
                    .findFirst()
                    .orElse(null) != null;
        } else {
            return false;
        }
    }

    public boolean hasNormalizers() {
        return this.normalizers != null && this.normalizers.size() > 0;
    }
}
