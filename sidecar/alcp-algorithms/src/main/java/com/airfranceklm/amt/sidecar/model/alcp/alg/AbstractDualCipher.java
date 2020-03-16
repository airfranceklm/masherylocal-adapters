package com.airfranceklm.amt.sidecar.model.alcp.alg;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;

/**
 * Base class for a dual cipher that uses RSA and AES encryption.
 *
 * @param <TIn> Type of the Mashery -&gt; Sidecar request
 * @param <TOut> Type of Mashery &lt;- Sidecar response
 * @param <TAlg> Type describing the algorithm interpreting the two above.
 */
public class AbstractDualCipher<TIn, TOut, TAlg extends ALCPAlgorithm<TIn, TOut> & DualCipherAlgorithm> {
    private TAlg algorithm;

    public AbstractDualCipher(TAlg alg) {
        this.algorithm = alg;
    }

    public String getRSACipherName() {
        return this.algorithm.getAssymetricCipherName();
    }

    public String getAESCipherName() {
        return this.algorithm.getSymmetricCipherName();
    }

    public TAlg getAlgorithm() {
        return algorithm;
    }
}
