package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.AbstractSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PreFlightSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PreProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.elements.ElementsFactory;
import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.RSAPublicKeyDescriptor;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;
import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithmSpec;
import com.airfranceklm.amt.sidecar.identity.CounterpartKeyDescriptor;
import com.airfranceklm.amt.sidecar.model.alcp.SidecarAuthenticationChannel;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for translating configurations into runtime.
 */
@Slf4j
public class ConfigurationStoreHelper {

    private static final List<String> emptyStringList = new ArrayList<>();

    static PreProcessorSidecarRuntime createPreProcessorRuntime(PreProcessEvent ppe, SidecarProcessor processor, ElementsFactory factory) {

        HashSet<String> remainingKeys = new HashSet<>(ppe.getEndpoint().getProcessor().getPreProcessorParameters().keySet());
        PreProcessorSidecarConfiguration preCfg = null;
        PreFlightSidecarConfiguration preFlightCfg = null;


        MasheryConfigurationReader<PreProcessorSidecarConfiguration> preReader = processor.getConfigurationDialect().getPreProcessorReader();

        if (preReader != null) {
            preCfg = preReader.read(ppe);
            remainingKeys.removeAll(preReader.relevantKeys());
        }

        MasheryConfigurationReader<PreFlightSidecarConfiguration> preflight = processor.getConfigurationDialect().getPreflightReader();
        if (preflight != null) {
            preFlightCfg = preflight.read(ppe);
            remainingKeys.removeAll(preflight.relevantKeys());
        }

        if (remainingKeys.size() > 0) {
            switch (processor.getConfigurationDialect().qualifyRemainingKeys(remainingKeys)) {
                case UnrecognizedKeysDisallowed:
                    log.error(String.format("PRE-processor configuration for endpoint %s contains %d unrecognized keys. Unrecognized keys are DISALLOWED by the dialect."
                            , ppe.getEndpoint().getExternalID()
                            , remainingKeys.size()));
                    return null;
                case UnrecognizedKeysAllowed:
                    log.error(String.format("PRE-processor configuration for endpoint %s contains %d unrecognized keys. Unrecognized keys are ALLOWED by the dialect. Please clean-up your configuration."
                            , ppe.getEndpoint().getExternalID()
                            , remainingKeys.size()));
                    break;
            }
        }

        // Building the common runtime.
        return buildPreProcessorSidecarRuntime(processor, factory, preFlightCfg, preCfg, null);
    }

    public static PreProcessorSidecarRuntime buildPreProcessorSidecarRuntime(SidecarProcessor processor
            , ElementsFactory factory
            , PreFlightSidecarConfiguration preFlightCfg
            , PreProcessorSidecarConfiguration preCfg
            , JsonSidecarPreProcessorOutput staticModification) {

        PreProcessorSidecarRuntime runtime = new PreProcessorSidecarRuntime();

        if (preFlightCfg != null && preFlightCfg.preflightDemanded()) {
            final PreFlightSidecarInputBuilder preflightBuilder = new PreFlightSidecarInputBuilder(factory, preFlightCfg);

            applyALCP(preflightBuilder, processor);
            applyStack(preflightBuilder, processor);
            preflightBuilder.configurationCompleted();

            runtime.setPreflightBuilder(preflightBuilder);
        }

        if (preCfg != null) {
            final PreProcessSidecarInputBuilder preBuilder = new PreProcessSidecarInputBuilder(factory, preCfg);
            applyALCP(preBuilder, processor);
            applyStack(preBuilder, processor);
            preBuilder.configurationCompleted();

            runtime.setPreProcessBuilder(preBuilder);
        }

        runtime.setStaticModification(staticModification);

        return runtime;
    }

    private static void applyALCP(AbstractSidecarInputBuilder<?, ?> retVal, SidecarProcessor proc) {
        if (proc != null) {
            SidecarConfiguration cfg = retVal.getConfiguration();

            final ALCPConfiguration alcpCfg = cfg.getAlcpConfiguration();
            if (alcpCfg != null) {

                CounterpartKeyDescriptor<RSAPublicKeyDescriptor> sidecarKd = null;

                // Read the counterpart identity.
                if (alcpCfg.getSidecarIdentityRef() != null) {
                    if (proc.getAlcpCounterpartKeys() == null) {
                        cfg.incrementError();
                        cfg.addMessage("Reference to ALCP key by name while no keys are loaded");
                    } else {
                        sidecarKd = proc.getAlcpCounterpartKeys().getKeyById(alcpCfg.getSidecarIdentityRef());
                        if (sidecarKd != null) {
                            retVal.setSidecarIdentity(sidecarKd.asCounterpartIdentity());
                        } else {
                            cfg.incrementError();
                            cfg.addMessage("Reference to unknown sidecar identity key");
                        }
                    }
                } else if (alcpCfg.definesIdentityExplicitly()) {
                    sidecarKd = new CounterpartKeyDescriptor<>();
                    sidecarKd.setPasswordSalt(alcpCfg.getPasswordSalt());
                    sidecarKd.setSymmetricKey(alcpCfg.getSymmetricKey());

                    if (alcpCfg.getPublicKey() != null) {
                        RSAPublicKeyDescriptor pkd = new RSAPublicKeyDescriptor(alcpCfg.getPublicKey());
                        PublicKey pk = pkd.getPublicKey();
                        if (pk == null) {
                            cfg.incrementError();
                            cfg.addMessage("Public key was not created from the material supplied");
                        }
                    }
                }
                // else: the counterpart identity is not defined. For certain algorithms that do not require
                // sidecar to perform symmetric or asymmetric decryption this is a perfectly valid scenario.

                // Read the ALCP algorithm settings
                if (alcpCfg.getAlgorithm() != null) {
                    ALCPAlgorithmSpec algSpec = new ALCPAlgorithmSpec(alcpCfg.getAlgorithm()
                            , alcpCfg.getParams()
                            , alcpCfg.getActivation());

                    if (proc.getAlcpFactory() == null) {
                        cfg.incrementError();
                        cfg.addMessage("Attempt to load ALCP algorithm without an initialized factory");
                    } else {
                        ALCPAlgorithm<?, ?> alg = proc.getAlcpFactory().create(algSpec);

                        if (alg != null) {
                            ClusterIdentity ci = proc.getAlcpIdentities() != null ? proc.getAlcpIdentities().alcpIdentity() : null;

                            final SidecarAuthenticationChannel ch = new SidecarAuthenticationChannel(ci
                                    , sidecarKd != null ? sidecarKd.asCounterpartIdentity() : null);

                            if (!alg.isChannelSufficient(ch)) {
                                cfg.incrementError();
                                cfg.addMessage(String.format("Insufficient settings for ALCP channel for algorithm %s", alg.getName()));
                            } else {
                                retVal.setAlcp(alg.getMasherySide(ch));
                            }
                        } else {
                            cfg.incrementError();
                            cfg.addMessage(String.format("Reference to unsupported algorithm (%s) or unsupported/insufficient algorithm options", algSpec.getAlgorithmName()));
                        }
                    }
                }
            }
        }
    }

    private static void applyStack(AbstractSidecarInputBuilder<?, ?> retVal, SidecarProcessor proc) {
        if (proc != null) {
            SidecarConfiguration cfg = retVal.getConfiguration();

            final SidecarStack stackFor = proc.getStackFor(cfg);
            if (stackFor != null) {
                final SidecarStackConfiguration stackCfg = stackFor.configureFrom(cfg);

                if (stackCfg.isValid()) {
                    retVal.setStack(stackFor);
                    retVal.setStackConfiguration(stackCfg);
                } else {
                    cfg.incrementError();
                    cfg.addMessage("Stack configuration is not valid");
                }
            } else {
                cfg.incrementError();
                cfg.addMessage("Stack cannot be resolved.");
            }
        }
    }

    static PostProcessSidecarInputBuilder getPostProcessSidecarInputBuilder(PostProcessEvent ppe, SidecarProcessor proc, ElementsFactory factory) {
        MasheryConfigurationReader<PostProcessorSidecarConfiguration> reader = proc.getConfigurationDialect().getPostProcessorReader();
        if (reader != null) {
            PostProcessorSidecarConfiguration cfg = reader.read(ppe);
            final Map<String, String> params = ppe.getEndpoint().getProcessor().getPostProcessorParameters();

            HashSet<String> remainingKeys = new HashSet<>(params == null ? emptyStringList : params.keySet());
            remainingKeys.removeAll(reader.relevantKeys());

            if (remainingKeys.size() > 0) {
                switch (proc.getConfigurationDialect().qualifyRemainingKeys(remainingKeys)) {
                    case UnrecognizedKeysDisallowed:
                        log.error(String.format("POST-processor configuration for endpoint %s contains %d unrecognized keys. Unrecognized keys are DISALLOWED by the dialect."
                                , ppe.getEndpoint().getExternalID()
                                , remainingKeys.size()));
                        return null;
                    case UnrecognizedKeysAllowed:
                        log.error(String.format("POST-processor configuration for endpoint %s contains %d unrecognized keys. Unrecognized keys are ALLOWED by the dialect. Please clean-up your configuration."
                                , ppe.getEndpoint().getExternalID()
                                , remainingKeys.size()));
                        break;
                }
            }

            return buildPostProcessorInputBuilder(proc, factory, cfg);
        } else {
            return null;
        }
    }

    static PostProcessSidecarInputBuilder buildPostProcessorInputBuilder(SidecarProcessor proc, ElementsFactory factory, PostProcessorSidecarConfiguration cfg) {
        final PostProcessSidecarInputBuilder retVal = new PostProcessSidecarInputBuilder(factory, cfg);

        applyALCP(retVal, proc);
        applyStack(retVal, proc);
        retVal.configurationCompleted();
        return retVal;
    }
}
