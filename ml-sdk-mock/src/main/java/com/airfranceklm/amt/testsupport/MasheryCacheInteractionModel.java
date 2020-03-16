package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.CacheImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
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
import static org.junit.Assert.fail;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "masheryCache")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MasheryCacheInteractionModel extends RequestCaseDatum {

    @Singular
    private Map<String, Serializable> entries;
    @Singular
    private List<MasheryCacheInboundObjectModel> inboundObjects;
    @Singular
    private List<String> expectedGets;

    @Builder.Default
    private boolean lenient = false;

    public void deepCopyFrom(@NonNull MasheryCacheInteractionModel another) {
        Mocks.cloneNullableMap(another::getEntries, this::setEntries, HashMap::new);
        Mocks.cloneNullableCollection(another::getInboundObjects, this::setInboundObjects, ArrayList::new);

        Mocks.cloneNullableCollection(another::getExpectedGets, this::setExpectedGets, ArrayList::new);

        this.lenient = another.lenient;
    }

    public Cache mock(EasyMockSupport owner) {
        Cache retVal = owner.createMock(Cache.class);
        CacheImpl delegate = new CacheImpl(entries);

        if (inboundObjects != null) {
            inboundObjects.forEach((v) -> {
                try {
                    retVal.put(v.getKey(), v.getStoredObject(), v.getCacheDuration());
                    if (v.getException() != null) {
                        expectLastCall().andThrow(new CacheException(v.getException())).anyTimes();
                    } else {
                        expectLastCall().andDelegateTo(delegate);
                    }
                } catch (CacheException ex) {
                    fail("Exception during recording of the mock");
                }
            });
        }
        try {
            if (expectedGets != null) {
                for (String g : expectedGets) {
                    expect(retVal.get(anyObject(), eq(g))).andDelegateTo(delegate).anyTimes();
                }
            }

            if (lenient) {
                expect(retVal.get(anyObject(), anyString())).andDelegateTo(delegate).anyTimes();

                retVal.put(anyString(), anyObject(), anyInt());
                expectLastCall().andDelegateTo(delegate).anyTimes();
            }
        } catch (CacheException ex) {
            fail("Exception during recording of a mock");
        }

        return retVal;
    }

    public void acceptVisitor(@NonNull TestModelVisitor v) {
        v.visit(this);
    }


    public void inheritFrom(MasheryCacheInteractionModel other) {
        copyIfNullMap(this::getEntries, other::getEntries, this::setEntries, HashMap::new);

        copyIfNullCollection(this::getInboundObjects, other::getInboundObjects, this::setInboundObjects, ArrayList::new);
        copyIfNullCollection(this::getExpectedGets, other::getExpectedGets, this::setExpectedGets, ArrayList::new);

    }
}
