package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryALCPSide;

import java.io.IOException;

/**
 * Application-Level Call Protection-enabled stack. This stack will perform automatic wrapping and unwrapping
 * of the responses with the selected ALCP algorithm, leaving the stack the means of just carrying the
 * message to the sidecar's inputs.
 */
public abstract class ALCPEnabledStack extends CommonStack {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg,
                                                                                    SidecarInvocationData cmd,
                                                                                    Class<U> expectedRetClass) throws IOException {

        // Establishing the return class the sidecar has to return.
        Class<?> sidecarRespClass = expectedRetClass;
        final MasheryALCPSide<Object, Object> alcpSide = (MasheryALCPSide<Object, Object>) cmd.getApplicationLevelCallProtection();

        if (alcpSide != null) {
            if (alcpSide.requiresResponseDecryption()) {
                sidecarRespClass = alcpSide.getSidecarResponseClass();
            }
        }

        Object sidecarResponse;

        if (alcpSide != null && alcpSide.requiresRequestEncryption()) {
            final EncryptedMessage<Object> encrMsg = alcpSide.encrypt(cmd.getInput(), getProcessorServices());
            sidecarResponse = doInvoke(cfg, encrMsg, sidecarRespClass);
        } else {
            sidecarResponse = doInvoke(cfg, cmd.getInput(), sidecarRespClass);
        }

        if (alcpSide != null && alcpSide.requiresResponseDecryption()) {
            return alcpSide.decrypt(sidecarResponse, getProcessorServices(), expectedRetClass);
        } else {
            return (U) sidecarResponse;
        }
    }



    protected abstract <TProtectedIn, TProtectedOutput> TProtectedOutput doInvoke(SidecarStackConfiguration cfg
            , EncryptedMessage<TProtectedIn> m
            , Class<TProtectedOutput> protectedResponseCls) throws IOException;

    protected abstract <TType> TType doInvoke(SidecarStackConfiguration cfg
            , SidecarInput si
            , Class<TType> respCls) throws IOException;



}
