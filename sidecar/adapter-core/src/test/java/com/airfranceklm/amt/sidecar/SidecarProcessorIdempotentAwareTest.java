package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Test how idempotent awareness will be working.
 */
public class SidecarProcessorIdempotentAwareTest extends EasyMockSupport {

    private static final String key  = "idempotent::srv_endp_null";

    private static Date beforeRefDate = JsonHelper.parseJSONDate("2019-10-10T13:59:00Z");
    private static Date refDate = JsonHelper.parseJSONDate("2019-10-10T14:00:00Z");
    private static Date replyTime = JsonHelper.parseJSONDate("2019-10-10T14:00:01Z");
    private static Date beforeExp = JsonHelper.parseJSONDate("2019-10-10T14:04:44Z");
    private static Date storageRefreshTrigger = JsonHelper.parseJSONDate("2019-10-10T14:04:57Z");
    private static Date storeExpiry = JsonHelper.parseJSONDate("2019-10-10T14:05:00Z");
    private static Date almostExpired = JsonHelper.parseJSONDate("2019-10-10T14:53:20Z");
    private static Date expiryDate = JsonHelper.parseJSONDate("2019-10-10T14:53:34Z");

    private static Date afterExpiryDate = JsonHelper.parseJSONDate("2019-10-10T14:53:47Z");

    /**
     * A call misses out caches and does not return the <code>unchangedUntil</code> date.
     */
    @Test
    public void testMissingCachesAndReceivingNonIdempotentResponse() throws CacheException, IOException {

        SidecarInvocationData sid = createInvocationBase();

        sid.setCache(createCacheMockHaving(null));

        IdempotentShallowCache shallowMock = createShallowCacheWith(null);
        IdempotentUpdateDebouncer bouncer = createMock(IdempotentUpdateDebouncer.class);

        SidecarProcessor proc = createProcessorMock();

        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        expect(proc.invokePreProcessorWithCircuitBreaker(sid)).andReturn(t);
        expect(proc.now()).andReturn(refDate.getTime()).once();

        proc.useIdempotentDependencies(bouncer, shallowMock);

        replayAll();

        final SidecarPreProcessorOutput retVal = proc.invokeIdempotentAware(sid);
        assertSame(retVal, t);

        verifyAll();
    }

    /**
     * Test demonstrates the situation where the sidecar process will see the idempotent response from the
     * API provider the first time. In this case, both idempotent shallow cache as well as Mashery cache don't have
     * data stored. The return value is saved into in-memory cache.
     */
    @Test
    public void testMissingCachesAndReceivingIdempotentResponse() throws CacheException, IOException {

        SidecarInvocationData sid = createInvocationBase();

        sid.setCache(createCacheMockHaving(null));

        IdempotentShallowCache shallowMock = createShallowCacheWith(null);

        SidecarProcessor proc = createProcessorMock();
        proc.useIdempotentDependencies(null, shallowMock);

        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        expect(proc.now()).andReturn(refDate.getTime()).once();
        expect(proc.now()).andReturn(refDate.getTime() + 150).once();
        expect(proc.invokePreProcessorWithCircuitBreaker(sid)).andReturn(t);

        SidecarOutputCache expSoc = new SidecarOutputCache();
        expSoc.storeUntil(t, new Date(storeExpiry.getTime() + 150));

        sid.getCache().put(key, expSoc, 300);
        expectLastCall().once();

        replayAll();

        final SidecarPreProcessorOutput retVal = proc.invokeIdempotentAware(sid);
        assertSame(retVal, t);

        verifyAll();
    }

    /**
     * In this test, the shallow idempotent cache doesn't have data, but cache has. The object
     * is be loaded from cache and returned, without making a call to the sidecar stack.
     * It is saved in the shallow memory cache, as it's reusable.
     */
    @Test
    public void testLoadingFromCacheAndPuttingIntoShallowMemoryCache() throws CacheException, IOException {

        SidecarInvocationData sid = createInvocationBase();

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(null);

        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);
        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, storeExpiry);

        Cache ch = createCacheMockHaving(soc);

        // Shallow cache should remember the value.
        expect(shallowMock.put(key, soc)).andReturn(null).once();

        sid.setCache(ch);

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(null, shallowMock);
        expect(processorMock.now()).andReturn(beforeExp.getTime()).once();
        replayAll();

        assertSame(t, processorMock.invokeIdempotentAware(sid));
        verifyAll();
    }

    /**
     * In this test, the shallow idempotent cache doesn't have data, but cache has an object that is stale. This
     * should not happen under normal operation, however this scenario must be considered. If the object
     * is stale, then it should not be returned as a result of a call.
     */
    @Test
    public void testLoadingStaleObjectFromCache() throws CacheException, IOException {

        SidecarInvocationData sid = createInvocationBase();

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(null);

        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(beforeRefDate);
        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, expiryDate);

        sid.setCache(createCacheMockHaving(soc));

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(null, shallowMock);
        expect(processorMock.now()).andReturn(refDate.getTime()).once();
        expect(processorMock.now()).andReturn(replyTime.getTime()).once();

        final JsonSidecarPreProcessorOutput expRet = new JsonSidecarPreProcessorOutput();
        expRet.setUnchangedUntil(expiryDate);
        expect(processorMock.invokePreProcessorWithCircuitBreaker(sid)).andReturn(expRet).once();

        // The returned object must be stored in cache.
        SidecarOutputCache expCache = new SidecarOutputCache();
        expCache.storeUntil(expRet, adjustForDiff(storeExpiry, refDate, replyTime));

        sid.getCache().put(key, expCache, 300);
        expectLastCall().once();

        replayAll();

        assertSame(expRet, processorMock.invokeIdempotentAware(sid));
        verifyAll();
    }

    /**
     * In this test, the shallow idempotent cache doesn't have data, but cache has. The object
     * should be loaded from cache and returned, without making a call to the sidecar stack.
     * It is saved NOT in the idempotent cache as it's too close to its expiry time.
     */
    @Test
    public void testLoadingFromCacheAndSkippingNonStorableObject() throws CacheException, IOException {

        SidecarInvocationData sid = createInvocationBase();

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(null);

        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, storeExpiry);
        assertFalse(soc.isStorable(almostExpired.getTime()));

        // Shallow cache should NOT remember the value.

        sid.setCache(createCacheMockHaving(soc));

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(null, shallowMock);

        expect(processorMock.now()).andReturn(almostExpired.getTime()).once();
        replayAll();

        assertSame(t, processorMock.invokeIdempotentAware(sid));
        verifyAll();
    }

    /**
     * In this test, a valid object is returned from the shallow object cache. Since it is a valid object,
     * then it is returned. Repeated insertion into cache doesn't happen as it is not necessary.
     */
    @Test
    public void testGettingValidObjectFromShallowCache() throws IOException {
        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, storeExpiry);

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(soc);

        SidecarInvocationData sid = createInvocationBase();

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(null, shallowMock);
        expect(processorMock.now()).andReturn(refDate.getTime()).once();

        replayAll();

        final SidecarPreProcessorOutput idem = processorMock.invokeIdempotentAware(sid);
        assertSame(t, idem);
    }

    /**
     * In this test, an expired object is retrieved from the shallow memory cache. This will happen is the API client
     * was not calling and the idempotent cache has expired. In this case, the following needs to happen:
     * <ul>
     *     <li>the reference will be removed;</li>
     *     <li>the query to the cache will be made.</li>
     * </ul>
     *
     * In this scenario, the object is stale in memory cache, and no newer object exists in Mashery cache. So there
     * will be a call to the API back-end.
     */
    @Test
    public void testGettingStaleObjectFromShallowCache() throws CacheException, IOException {
        SidecarInvocationData sid = createInvocationBase();

        sid.setCache(createCacheMockHaving(null));

        JsonSidecarPreProcessorOutput stale = new JsonSidecarPreProcessorOutput();
        stale.setUnchangedUntil(new Date(refDate.getTime() - 1000));

        SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(stale, stale.getUnchangedUntil());

        IdempotentShallowCache shallowMock = createShallowCacheWith(soc);
        expect(shallowMock.remove(key)).andReturn(soc).once();

        SidecarProcessor proc = createProcessorMock();

        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        expect(proc.invokePreProcessorWithCircuitBreaker(sid)).andReturn(t).once();

        expect(proc.now()).andReturn(refDate.getTime()).once();
        expect(proc.now()).andReturn(replyTime.getTime()).once();

        proc.useIdempotentDependencies(null, shallowMock);

        SidecarOutputCache expCache = new SidecarOutputCache();
        expCache.storeUntil(t, adjustForDiff(storeExpiry, refDate, replyTime));

        sid.getCache().put(key, expCache, 300);
        expectLastCall().once();

        replayAll();

        final SidecarPreProcessorOutput retVal = proc.invokeIdempotentAware(sid);
        assertSame(retVal, t);

        verifyAll();
    }

    /**
     * This is a variation of {@link #testGettingStaleObjectFromShallowCache()} method, where Mashery cache
     * DOES have a valid object. In this test, the valid object is fetched from the cache and is put
     * back to the shallow cache, and no call to the API backend still is made.
     */
    @Test
    public void testGettingStaleObjectFromShallowCacheAndGettingValidFromCache() throws CacheException, IOException {
        SidecarInvocationData sid = createInvocationBase();

        JsonSidecarPreProcessorOutput stale = new JsonSidecarPreProcessorOutput();
        stale.setUnchangedUntil(new Date(refDate.getTime() - 1000));

        SidecarOutputCache staleSoc = new SidecarOutputCache();
        staleSoc.storeUntil(stale, stale.getUnchangedUntil());

        IdempotentShallowCache shallowMock = createShallowCacheWith(staleSoc);
        expect(shallowMock.remove(key)).andReturn(staleSoc).once();

        SidecarProcessor proc = createProcessorMock();

        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        SidecarOutputCache expCache = new SidecarOutputCache();
        expCache.storeUntil(t, storeExpiry);
        sid.setCache(createCacheMockHaving(expCache));

        // The newly fetched object must be put back into the shallow memory cache for further
        // reuse.
        expect(shallowMock.put(key, expCache)).andReturn(staleSoc).once();

        proc.useIdempotentDependencies(null, shallowMock);
        expect(proc.now()).andReturn(refDate.getTime()).once();

        replayAll();

        final SidecarPreProcessorOutput retVal = proc.invokeIdempotentAware(sid);
        assertSame(retVal, t);

        verifyAll();
    }

    /**
     * A variation of {@link #testGettingValidObjectFromShallowCache()}, where the object is almost at the end of its
     * storage period. To ensure that Mashery cache has the object, it is re-persisted to last next storage interval.
     */
    @Test
    public void testGettingValidObjectFromShallowCacheAndRefreshingMasheryCache() throws IOException, CacheException {
        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, storeExpiry);

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(soc);

        SidecarInvocationData sid = createInvocationBase();

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(bouncer(false), shallowMock);
        expect(processorMock.now()).andReturn(storageRefreshTrigger.getTime()).once();

        // The processor should put the persisted object for another 5 minutes into Mashery in-memory cache.
        SidecarOutputCache expSoc = new SidecarOutputCache(soc);
        expSoc.extendStoreWith(TimeUnit.MINUTES, 5);

        Cache cacheMock = createMock(Cache.class);
        cacheMock.put(key, expSoc, 303);
        // Why 303? The call is made 3 seconds before the storage has expired. Storage is extended with 5 minutes
        // from the storage expiry time. This means that the object should expire in 5 minutes = 300 seconds
        // of the next extension time, plus 3 second that are still left in the current storage interval.
        expectLastCall().once();

        sid.setCache(cacheMock);

        replayAll();

        final SidecarPreProcessorOutput idem = processorMock.invokeIdempotentAware(sid);
        assertSame(t, idem);
    }

    /**
     * A variation of {@link #testGettingValidObjectFromShallowCacheAndRefreshingMasheryCache()}, where the object is
     * almost at the end of its storage period. In case a bouncer will bounce repetitive update, the cache should
     * not be updated.
     */
    @Test
    public void testGettingValidObjectFromShallowCacheAndGettingCacheUpdateBounced() throws IOException, CacheException {
        // Mashery store does have it.
        final JsonSidecarPreProcessorOutput t = new JsonSidecarPreProcessorOutput();
        t.setUnchangedUntil(expiryDate);

        final SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeUntil(t, storeExpiry);

        // Shallow cache doesn't have it.
        IdempotentShallowCache shallowMock = createShallowCacheWith(soc);

        SidecarInvocationData sid = createInvocationBase();

        final SidecarProcessor processorMock = createProcessorMock();
        processorMock.useIdempotentDependencies(bouncer(true), shallowMock);
        expect(processorMock.now()).andReturn(storageRefreshTrigger.getTime()).once();

        // In this test, there is no interaction with cache.
        sid.setCache(createMock(Cache.class));

        replayAll();

        final SidecarPreProcessorOutput idem = processorMock.invokeIdempotentAware(sid);
        assertSame(t, idem);
    }

    private IdempotentUpdateDebouncer bouncer(boolean bouncing) {
        IdempotentUpdateDebouncer retVal = createMock(IdempotentUpdateDebouncer.class);
        expect(retVal.shouldBounce(key)).andReturn(bouncing).once();
        return retVal;
    }

    private Date adjustForDiff(Date base, Date d1, Date d2) {
        assertTrue(d2.after(d1));

        long ts = base.getTime();
        long diff = d2.getTime() - d1.getTime();
        ts += diff;

        return new Date(ts);
    }

    private SidecarProcessor createProcessorMock() {
        return partialMockBuilder(SidecarProcessor.class)
                .addMockedMethod("invokePreProcessorWithCircuitBreaker")
                .addMockedMethod("now")
                .createMock();
    }

    private Cache createCacheMockHaving(SidecarOutputCache soc) throws CacheException {
        Cache ch = createMock(Cache.class);
        expect(ch.get(anyObject(), eq(key))).andReturn(soc).once();
        return ch;
    }

    private IdempotentShallowCache createShallowCacheWith(SidecarOutputCache o) {
        IdempotentShallowCache shallowMock = createMock(IdempotentShallowCache.class);
        expect(shallowMock.get(key)).andReturn(o).once();
        return shallowMock;
    }

    private SidecarInvocationData createInvocationBase() {
        SidecarInvocationData sid = new SidecarInvocationData(null);
        sid.setServiceId("srv");
        sid.setEndpointId("endp");
        sid.setIdempotentAware(true);
        return sid;
    }

}
