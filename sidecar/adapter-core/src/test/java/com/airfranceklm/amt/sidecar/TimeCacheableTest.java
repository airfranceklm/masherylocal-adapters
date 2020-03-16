package com.airfranceklm.amt.sidecar;

import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.*;

public class TimeCacheableTest extends EasyMockSupport {

    @Test
    @SuppressWarnings("unchecked")
    public void testOverride() {
        TimeCacheable<ProcessorEvent, String> tc = partialMockBuilder(TimeCacheable.class)
                .addMockedMethod("now")
                .createMock();

        tc.overrideFrom("aaa", "bbb");
        assertEquals("bbb", tc.get(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCachingCalledAfterTimeout() {

        Map<String,String> firstMap = new HashMap<>();
        firstMap.put("a", "b");

        Map<String,String> secondMap = new HashMap<>(firstMap);
        secondMap.put("c", "d");



        TimeCacheable<ProcessorEvent, String> tc = partialMockBuilder(TimeCacheable.class)
                .addMockedMethod("now")
                .createMock();

        Function<ProcessorEvent, Map<String,String>> cfgLocator = createMock(Function.class);
        expect(cfgLocator.apply(anyObject())).andReturn(firstMap).once();
        expect(cfgLocator.apply(anyObject())).andReturn(secondMap).once();

        Function<ProcessorEvent, String> converter = createMock(Function.class);
        expect(converter.apply(anyObject())).andReturn("First").once();
        expect(converter.apply(anyObject())).andReturn("Second").once();

        final long first =  123565344;
        final long unchanged =  123565354;
        final long changeMoment = 123999999;

        expect(tc.now()).andReturn(first).once();
        expect(tc.now()).andReturn(unchanged).once();
        expect(tc.now()).andReturn(changeMoment).times(3);

        replayAll();

        tc.initConfigSourceMemory();
        tc.timeBetweenConfigurationChangeChecks(TimeUnit.MINUTES, 1);
        tc.locateConfigurationUsing(cfgLocator);
        tc.extractCachedValueUsing(converter);

        // The first two come close.
        assertEquals("First", tc.get(null));
        assertEquals("First", tc.get(null));

        // The latter comes after time, and will trigger extraction, after which it will be cached
        assertEquals("Second", tc.get(null));
        assertEquals("Second", tc.get(null));
        assertEquals("Second", tc.get(null));

        verifyAll();
    }
}
