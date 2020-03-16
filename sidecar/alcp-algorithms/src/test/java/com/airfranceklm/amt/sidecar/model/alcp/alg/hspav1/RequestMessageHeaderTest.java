package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequestMessageHeaderTest {

    @Test
    public void testParsing() throws IllegalMessageHeaderException {
        String base64 = "ncJWkfWzqRu97G4+DcgmTFAVQM4gZEh63Gd+GGzQ3z5PqiiRGbiFPWekqUB32rtQhOGCwT9ZhUKN4csPP7SBLdv67+G/hqBGPXUcKDn/RNTqq3BNESpvWzW6PSSdEmiyV0ebLMgN5OEdXXmLRkcFE5LGJR+WPioTrwvqEvKALEw1MngrlLlbaHtPTybjXi5lymS3dLyVGxRbTk9QVG6hdQCvwwfpIDFCRNrpR6bYMiNH5TKVZQopnhE7PieQcgFadunvpyoWTzx6JKYyn4rd4S4tvpHyfAFCwjSELNf85U9vF2QRg6Uk0p2UEJjVsW+e5u+hCHCaOminj3ZP4hBFVQ==";
        String str = "unit-test-ar3a:unit-t3st-key:1578920960:ncJWkfWzqRu97G4+DcgmTFAVQM4gZEh63Gd+GGzQ3z5PqiiRGbiFPWekqUB32rtQhOGCwT9ZhUKN4csPP7SBLdv67+G/hqBGPXUcKDn/RNTqq3BNESpvWzW6PSSdEmiyV0ebLMgN5OEdXXmLRkcFE5LGJR+WPioTrwvqEvKALEw1MngrlLlbaHtPTybjXi5lymS3dLyVGxRbTk9QVG6hdQCvwwfpIDFCRNrpR6bYMiNH5TKVZQopnhE7PieQcgFadunvpyoWTzx6JKYyn4rd4S4tvpHyfAFCwjSELNf85U9vF2QRg6Uk0p2UEJjVsW+e5u+hCHCaOminj3ZP4hBFVQ==";

        RequestMessageHeader rmh = RequestMessageHeader.parse(str);
        assertEquals("unit-test-ar3a", rmh.getAreaId());
        assertEquals("unit-t3st-key", rmh.getKeyId());
        assertEquals(1578920960L, rmh.getEpochSeconds());
        assertEquals(base64, rmh.getAuthSignature());
    }
}
