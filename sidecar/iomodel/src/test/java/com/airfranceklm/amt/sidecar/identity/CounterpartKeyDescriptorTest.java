package com.airfranceklm.amt.sidecar.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class CounterpartKeyDescriptorTest {

    private static ObjectMapper mapper;

    @BeforeClass
    public static void init() {
        mapper = new ObjectMapper();
    }

    @Test
    public void smokeTest() throws IOException {
        CounterpartKeyDescriptor<RSAPublicKeyDescriptor> ckd = new CounterpartKeyDescriptor<>();
        ckd.setKeyIdentifier(KeyIdentifier.typedKey("aws.identity"));

        mapper.writeValue(System.out, ckd);
    }
}
