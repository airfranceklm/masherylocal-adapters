package com.airfranceklm.amt.sidecar.model.alcp;

/**
 * Options that the high-security v1 protocol accepts.
 */
public enum HighSecurityAlgorithmV1Options {
    RSACipher, AESCipher, SigningCipher, AESVariablePasswordLength, IVInferenceDigestAlgorithm, MaximumClockInaccuracy,
    GCMMode
}
