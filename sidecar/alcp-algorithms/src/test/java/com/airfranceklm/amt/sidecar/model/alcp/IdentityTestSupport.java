package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class IdentityTestSupport extends EasyMockSupport {
    public static final String MASH_PRIVATE_KEY = "/.key_material/mashery.pkcs8";
    public static final String MASH_PUBLIC_KEY = "/.key_material/mashery.pub";
    public static final String MASH_PRIVATE_PASS = "/.key_material/.mashery.pwd";
    public static final String MASH_AES_SALT = "/.key_material/mashery.aes";

    public static final String SIDECAR_PUBLIC_KEY = "/.key_material/sidecar.pub";
    public static final String SIDECAR_PRIVATE_KEY = "/.key_material/sidecar.pkcs8";
    public static final String SIDECAR_PRIVATE_PASS = "/.key_material/.sidecar.pwd";

    public static final String SIDECAR_AES_SALT = "/.key_material/sidecar.aes";

    protected final String unitTestAreaId = "unit-test-area";
    protected final String unitTestKeyId = "unit-test-key";

    private static final Path targetOutput;

    static {
        URL url = IdentityTestSupport.class.getResource(SIDECAR_AES_SALT);
        assertNotNull(url);
        targetOutput = new File(url.getFile()).toPath().getParent().getParent().getParent();
    }

    protected File exchangeable(String name) {
        return targetOutput.resolve(name).toFile();
    }

    @Test
    public void testLocatingOutput() {

    }

    /**
     * Convenient method to setup Mashery to Sidecar authentication channel.
     * @return
     */
    protected SidecarAuthenticationChannel mockRequestChannel() {
        try {
            return new SidecarAuthenticationChannel(mockClusterIdentity(), mockSidecarIdentityAsCounterpart());
        } catch (Throwable ex) {
            fail(String.format("Could not setup mocks: %s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Convenient method to setup Mashery to any sidecar authentication channel. This channel does not
     * define {@link SidecarAuthenticationChannel#getSidecarIdentity()}, which will remain null.
     * @return instance suitable for the unit tests.
     */
    protected SidecarAuthenticationChannel mockIdentityOnlyRequestChannel() {
        try {
            return new SidecarAuthenticationChannel(mockClusterIdentity(), null);
        } catch (Throwable ex) {
            fail(String.format("Could not setup mocks: %s", ex.getMessage()));
            return null;
        }
    }

    protected ClusterIdentity mockClusterIdentity() throws IOException, InvalidKeySpecException, InvalidKeyException {
        InputStream pubIs = getClass().getResourceAsStream(MASH_PUBLIC_KEY);
        InputStream pkIs = getClass().getResourceAsStream(MASH_PRIVATE_KEY);
        InputStream aesIs = getClass().getResourceAsStream(MASH_AES_SALT);

        assertNotNull(getHint("Mashery public key required"), pubIs);
        assertNotNull(getHint("Mashery private key required"), pkIs);
        assertNotNull(getHint("Mashery aes salt required"), aesIs);

        String aesPwd = textOfResource(MASH_AES_SALT).trim();
        String pkcs8Pwd = textOfResource(MASH_PRIVATE_PASS).trim();

        ClusterIdentity ci = createMock(ClusterIdentity.class);
        expect(ci.getAreaId()).andReturn(unitTestAreaId).anyTimes();
        expect(ci.getKeyId()).andReturn(unitTestKeyId).anyTimes();
        expect(ci.getPasswordSalt()).andReturn(aesPwd).anyTimes();
        expect(ci.getPrivateKey()).andReturn(RSAKeyOps.privateKeyFromPKCS8(pkIs, pkcs8Pwd)).anyTimes();
        expect(ci.getPublicKey()).andReturn(RSAKeyOps.publicKeyFromPCKS8(pubIs)).anyTimes();

        return ci;
    }

    protected CounterpartIdentity mockClusterIdentityAsCounterpart() {
        try {
            InputStream pubIs = getClass().getResourceAsStream(MASH_PUBLIC_KEY);

            assertNotNull(getHint("Mashery public key required"), pubIs);

            CounterpartIdentity si = createMock(CounterpartIdentity.class);
            expect(si.getPasswordSalt()).andReturn(textOfResource(MASH_AES_SALT)).anyTimes();
            expect(si.getPublicKey()).andReturn(RSAKeyOps.publicKeyFromPCKS8(pubIs)).anyTimes();

            return si;
        } catch (Throwable ex) {
            fail(String.format("Exception in initialization: %s", ex.getMessage()));
            return null; // Unreachable code
        }
    }

    protected String textOfResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            assertNotNull(is);

            ByteArrayOutputStream retVal = new ByteArrayOutputStream(is.available());
            IOUtils.copy(is, retVal);

            return retVal.toString();
        }
    }

    protected SidecarIdentity mockSidecarIdentity() {
        try {
            InputStream sidecarPk = getClass().getResourceAsStream(SIDECAR_PRIVATE_KEY);

            assertNotNull(getHint("Sidecar private key"), sidecarPk);

            String pkPwd = textOfResource(SIDECAR_PRIVATE_PASS);
            String aesSalt = textOfResource(SIDECAR_AES_SALT);

            SidecarIdentity si = createMock(SidecarIdentity.class);
            expect(si.getName()).andReturn("Unit Test Sidecar").anyTimes();
            expect(si.getPasswordSalt()).andReturn(aesSalt).anyTimes();
            expect(si.getPrivateKey()).andReturn(RSAKeyOps.privateKeyFromPKCS8(sidecarPk, pkPwd)).anyTimes();

            return si;
        } catch (Throwable ex) {
            fail(String.format("Exception %s should have been thrown: %s", ex.getClass().getName(), ex.getMessage()));
            return null; // Unreachable statement
        }
    }

    protected CounterpartIdentity mockSidecarIdentityAsCounterpart() throws IOException, InvalidKeySpecException {
        InputStream pubIs = getClass().getResourceAsStream(SIDECAR_PUBLIC_KEY);

        assertNotNull(getHint("Sidecar public key required"), pubIs);

        CounterpartIdentity si = createMock(CounterpartIdentity.class);
        expect(si.getPasswordSalt()).andReturn(textOfResource(SIDECAR_AES_SALT)).anyTimes();
        expect(si.getPublicKey()).andReturn(RSAKeyOps.publicKeyFromPCKS8(pubIs)).anyTimes();

        return si;
    }

    protected KnownMasheryIdentities mockStrictMasheryIdentities(CounterpartIdentity ci) {
        KnownMasheryIdentities retVal = createMock(KnownMasheryIdentities.class);
        try {
            expect(retVal.getMasheryIdentity(unitTestAreaId, unitTestKeyId))
                    .andReturn(ci)
                    .anyTimes();

            expect(retVal.getMasheryIdentity(null, unitTestKeyId))
                    .andReturn(ci)
                    .anyTimes();

            expect(retVal.getMasheryIdentity(anyString(), anyString()))
                    .andThrow(new UnknownMasheryRequester("UTR"))
                    .anyTimes();
            return retVal;
        } catch (UnknownMasheryRequester unknownMasheryRequester) {
            fail("Exceptions should not have been thrown");
            return null;
        }
    }


    private String getHint(String lead) {
        return String.format("%s. Did you run initKeyMaterial.sh in ./src/test/resources?", lead);
    }
}
