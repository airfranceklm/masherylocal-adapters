package com.airfranceklm.amt.sidecar.model.alcp;

public class ALCPAlgorithmSide<TProtectedInput, TProtectedOutput> {
    protected ALCPAlgorithm<TProtectedInput, TProtectedOutput> alg;

    public ALCPAlgorithmSide(ALCPAlgorithm<TProtectedInput, TProtectedOutput> alg) {
        this.alg = alg;
    }

    public boolean requiresRequestEncryption() {
        return alg.getActivation().requiresRequestEncryption();
    }

    public boolean requiresResponseDecryption() {
        return alg.getActivation().requiresResponseDecryption();
    }
}
