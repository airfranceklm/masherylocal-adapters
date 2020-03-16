package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.DebugContextImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashery.trafficmanager.debug.DebugContext;
import lombok.*;
import org.easymock.EasyMockSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNullCollection;
import static com.airfranceklm.amt.testsupport.Mocks.copyIfNullMap;
import static org.easymock.EasyMock.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "debugContext")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MasheryDebugContextInteractionModel extends RequestCaseDatum {

    @Singular
    private Map<String, Serializable> entries;

    @Singular
    private List<String> expectGets;

    @Singular
    private List<String> expectRemoves;

    @Singular
    private Map<String, Serializable> expectPuts;

    private boolean strict;

    @Builder.Default
    private boolean acceptAnyValues = false;

    public DebugContext mock(EasyMockSupport owner) {
        DebugContextImpl delegate = new DebugContextImpl(entries);
        if (isLenient()) {
            return delegate;
        } else {
            DebugContext strictMock = owner.createMock(DebugContext.class);

            if (expectGets != null) {
                for (String g : expectGets) {
                    expect(strictMock.getEntry(g)).andDelegateTo(delegate).anyTimes();
                }
            }

            if (expectPuts != null) {
                expectPuts.forEach((k, v) -> {
                    strictMock.logEntry(k, v);
                    expectLastCall();
                });
            }

            if (expectRemoves != null) {
                expectRemoves.forEach((g) -> expect(strictMock.removeEntry(g)).andDelegateTo(delegate));
            }

            if (acceptAnyValues) {
                expect(strictMock.getEntry(anyString())).andDelegateTo(delegate).anyTimes();

                strictMock.logEntry(anyString(), anyObject());
                expectLastCall().andDelegateTo(delegate).anyTimes();

                expect(strictMock.removeEntry(anyString())).andDelegateTo(delegate).anyTimes();

                strictMock.clearEntries();
                expectLastCall().andDelegateTo(delegate).anyTimes();
            }
            return strictMock;
        }
    }

    public boolean isLenient() {
        return !strict && (expectGets == null || expectGets.size() == 0)
                && (expectPuts == null || expectPuts.size() == 0)
                && (expectRemoves == null || expectRemoves.size() == 0);
    }

    public MasheryDebugContextInteractionModel deepCopy(MasheryDebugContextInteractionModel another) {
        Mocks.cloneNullableMap(another::getEntries, this::setEntries, HashMap::new);

        Mocks.cloneNullableCollection(another::getExpectGets, this::setExpectGets, ArrayList::new);
        Mocks.cloneNullableCollection(another::getExpectRemoves, this::setExpectRemoves, ArrayList::new);

        Mocks.cloneNullableMap(another::getExpectPuts, this::setExpectPuts, HashMap::new);

        return this;
    }

    public void acceptVisitor(@NonNull TestModelVisitor v) {
        v.visit(this);
    }

    public void inheritFrom(MasheryDebugContextInteractionModel other) {
        copyIfNullMap(this::getEntries, other::getEntries, this::setEntries, HashMap::new);
        copyIfNullMap(this::getExpectPuts, other::getExpectPuts, this::setExpectPuts, HashMap::new);

        copyIfNullCollection(this::getExpectGets, other::getExpectGets, this::setExpectGets, ArrayList::new);
        copyIfNullCollection(this::getExpectRemoves, other::getExpectRemoves, this::setExpectRemoves, ArrayList::new);

    }

}
