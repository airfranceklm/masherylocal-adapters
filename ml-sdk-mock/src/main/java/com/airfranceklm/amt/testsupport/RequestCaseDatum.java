package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@EqualsAndHashCode
public class RequestCaseDatum {
    @JsonProperty("as in") @Getter @Setter
    protected String asIn;

    boolean needsCopyFromAnotherCase() {
        return asIn != null;
    }

    String getCaseToCopyFrom() {
        return asIn;
    }

    protected RequestCaseDatum() {
    }
}
