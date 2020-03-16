package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.afkl.AFKLMasheryEndpointConfigurationDialect;
import com.airfranceklm.amt.sidecar.elements.StandardElementsFactory;
import com.airfranceklm.amt.sidecar.identity.CounterpartKeySet;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithmFactory;
import com.airfranceklm.amt.sidecar.model.alcp.alg.caav1.CallerAuthenticityAlgorithmV1;
import com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1.HighSecurityProtectionAlgorithmV1;
import com.airfranceklm.amt.sidecar.stack.EchoStack;
import com.airfranceklm.amt.sidecar.stack.LogStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.SidecarProcessorFeature.CircuitBreaker;
import static com.airfranceklm.amt.sidecar.SidecarProcessorFeature.ProcessorServices;
import static com.airfranceklm.amt.sidecar.SidecarProcessorFeature.*;
import static com.airfranceklm.amt.sidecar.stack.SidecarStacks.stacksFor;

/**
 * Helper class for setting up {@link SidecarProcessor} to the needs of a particular deployment site. The
 * class could be used by supplying the developer-required defaults, similar to this:
 * <pre>
 *     SidecarProcessor delegate = new SidecarProcessDefaults()
 *          .havingDefaultCircuitBreaker()
 *          .havingDefaultProcessorServices()
 *          .havingLocalConfigurationLoader(cfgRoot)
 *          // Other methods as required
 *          .build();
 * </pre>
 * <p/>
 * For some cases, the deployer may need to manage a layered setup. In such cases, the use of
 * {@link #SidecarProcessorDefaults(SidecarProcessorDefaults)} is advised, similar to this example:
 * <pre>
 *      SidecarProcessorDefaults standardDefs = new SidecarProcessDefaults()
 *           .havingDefaultCircuitBreaker()
 *           .havingDefaultProcessorServices()
 *           .havingLocalConfigurationLoader(cfgRoot);
 *
 *      SidecarProcessor delegate = new SidecarProcessDefaults(standardDefs)
 *           .havingALCPIdentity(new File(cfgRoot, ALCP_IDENTITY_FILE_NAME))
 *           .havingDefaultALCPSupport()
 *           .build();
 * </pre>
 * <p/>
 * If this class becomes too limited for your case, you should be using getters and setters on the {@link SidecarProcessor}
 * adapter directly.
 */
@Slf4j
public class SidecarProcessorDefaults {

    private Map<SidecarProcessorFeature, Consumer<SidecarProcessor>> target;

    public SidecarProcessorDefaults() {
        target = new LinkedHashMap<>();
    }

    /**
     * Initializes
     *
     * @param existingDefaults existing defaults that should be "inherited" by this configuration
     */
    public SidecarProcessorDefaults(SidecarProcessorDefaults existingDefaults) {
        this();
        this.target.putAll(existingDefaults.target);
    }

    public static Function<SidecarStacks, SidecarProcessorDefaults> getDefaultsFor(LaunchLevel level) {
        switch (level) {
            case Essential:
                return SidecarProcessorDefaults::essential;
            case Advanced:
                return SidecarProcessorDefaults::advanced;
            case BasicALCP:
                return SidecarProcessorDefaults::basicALCP;
            case AdvancedALCP:
                return SidecarProcessorDefaults::advancedALCP;
            default:
                throw new IllegalArgumentException("Unsupported launch level");
        }
    }

    public static LaunchLevel determineLaunchLevel() {
        File configRoot = new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT);
        File idFile = new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT, SidecarProcessorConstants.ALCP_IDENTITY_FILE_NAME);
        File idPassFile = new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT, SidecarProcessorConstants.ALCP_PASSWORD_FILE_NAME);
        File sidecarsFile = new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT, SidecarProcessorConstants.ALCP_SIDECARS_KEYSET_FILE);

        if (sidecarsFile.exists() && idFile.exists() && idPassFile.exists() && configRoot.exists()) {
            return LaunchLevel.AdvancedALCP;
        } else if (idFile.exists() && idPassFile.exists() && configRoot.exists()) {
            return LaunchLevel.BasicALCP;
        } else if (configRoot.exists()) {
            return LaunchLevel.Advanced;
        } else {
            return LaunchLevel.Essential;
        }
    }

    /**
     * Adds a custom, develop-supplied configuration initializer.
     *
     * @param c consumer that will configure an aspect of a sidecar process. Must not be null
     */
    public SidecarProcessorDefaults having(SidecarProcessorFeature f, Consumer<SidecarProcessor> c) {
        target.put(f, Objects.requireNonNull(c));
        return this;
    }

    /**
     * Builds a new instance of the {@link SidecarProcessor} by applying all configured defaults.
     *
     * @return created instance of {@link SidecarProcessor}
     */
    public SidecarProcessor build() {
        final SidecarProcessor sidecarProcessor = new SidecarProcessor();
        apply(sidecarProcessor);

        return sidecarProcessor;
    }

    /**
     * Applies the accumulated defaults to the specified {@link SidecarProcessor} instance
     *
     * @param sp instance of the processor to apply the defaults to.
     */
    public void apply(SidecarProcessor sp) {
        SidecarProcessor usp = Objects.requireNonNull(sp);

        for (Consumer<SidecarProcessor> c : target.values()) {
            c.accept(usp);
        }
    }

    public SidecarProcessorDefaults havingDefaultIdempotentComponents() {
        target.put(IdempotentCallSupport, (c) -> {
            IdempotentUpdateDebouncer idempotentUpdateDebounce = new IdempotentUpdateDebouncer(300, TimeUnit.SECONDS, 10);
            Map<String, SidecarOutputCache> idempotentShallowCache = Collections.synchronizedMap(new IdempotentShallowCache(5 * 1024));

            c.useIdempotentDependencies(idempotentUpdateDebounce, idempotentShallowCache);
        });

        return this;
    }

    public SidecarProcessorDefaults havingDefaultProcessorServices() {
        target.put(ProcessorServices, (c) -> c.setProcessorServices(new JsonProcessorServices()));

        return this;
    }

    public SidecarProcessorDefaults havingDefaultErrorCodes() {
        target.put(ErrorCodes, (c) -> {
            c.setSidecarGeneratedErrorCode(550);
            c.setSidecarDefaultErrorCode(551);
        });
        return this;
    }

    /**
     * Initializes the default background threads executor.
     */
    public SidecarProcessorDefaults havingDefaultAsyncExecutors(int size) {
        target.put(AsyncExecutors, (c) -> {
            ThreadPoolExecutor backgroundInvokers = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            backgroundInvokers.setMaximumPoolSize(size);

            c.setAsyncExecutors(backgroundInvokers);
        });
        return this;
    }

    public SidecarProcessorDefaults havingDefaultConfigurationStore() {
        target.put(ConfigurationStore, (c) -> c.setConfigStore(new ProductionConfigurationStore()));
        return this;
    }

    public SidecarProcessorDefaults havingStacks(SidecarStacks stacks) {
        target.put(Stacks, (c) -> c.setSidecarStacks(stacks));
        return this;
    }

    public SidecarProcessorDefaults havingLocalConfigurationLoader(File f) {
        target.put(LocalConfigurationProvider, (c) -> {
            LocalDirectoryConfigurationProvider loader = new LocalDirectoryConfigurationProvider(f);
            c.setLocalConfigurationProvider(loader);
        });
        return this;
    }

    public SidecarProcessorDefaults havingALCPIdentity(File f, File pwdFile) {
        File uf = Objects.requireNonNull(f);
        File uPwd = Objects.requireNonNull(pwdFile);

        target.put(ALCPIdentityKeySet, (c) -> {
            if (uf.exists() && uf.canRead()) {
                try {
                    ProcessorKeySet ident = ALCPLoaders.loadProcessorKeySet(uf);
                    if (ident != null) {
                        if (uPwd.exists() && uPwd.canRead()) {
                            // TODO: we might also need to check for the file permissions
                            // on this file, to ensure that only a user is able to read these.
                            String pwd = new String(Files.readAllBytes(pwdFile.toPath())).trim();
                            ident.rehydrateKeys(pwd);
                        } else {
                            ident.rehydrateKeys();
                        }

                        c.setAlcpIdentities(ident);
                    } else {
                        log.error("ALCP identity keyset failed to load");
                    }
                } catch (IOException ex) {
                    log.error(String.format("Failed to load ALCP identity: %s. ALCP sidecar invocations will FAIL.", ex.getMessage()), ex);
                }
            }
        });

        return this;
    }

    public SidecarProcessorDefaults havingSidecarKeySet(File f) {
        File uf = Objects.requireNonNull(f);

        target.put(ALCPSidecarKeySet, (c) -> {
            if (f.exists() && f.canRead()) {
                CounterpartKeySet cks = ALCPLoaders.loadCounterpartsFromYaml(uf);
                if (cks != null) {
                    cks.rehydrateKeys();

                    c.setAlcpCounterpartKeys(cks);
                } else {
                    log.error("ALCP counterpart keyset was not loaded.");
                }
            }
        });

        return this;
    }

    /**
     * Initializes the support for standard elements
     */
    public SidecarProcessorDefaults havingStandardInputsElements() {
        target.put(ElementsFactory, (c) -> c.setSupportedElements(new StandardElementsFactory()));

        return this;
    }

    public SidecarProcessorDefaults havingDefaultCircuitBreaker() {
        target.put(CircuitBreaker, (c) -> c.setCircuitBreaker(new SimpleCircuitBreaker()));

        return this;
    }

    public SidecarProcessorDefaults havingDefaultMasheryConfigurationDialect() {
        target.put(MasheryConfigurationDialect, (c) -> c.setConfigurationDialect(new AFKLMasheryEndpointConfigurationDialect()));

        return this;
    }

    /**
     * Sidecar parameters that are necessary for the unit tests
     *
     * @return instance of the defaults.
     */
    public static SidecarProcessorDefaults unitTest() {
        return new SidecarProcessorDefaults()
                .havingDefaultErrorCodes()
                .having(CircuitBreaker, (c) -> c.setCircuitBreaker(new NoopCircuitBreaker()))
                .havingDefaultMasheryConfigurationDialect()
                .havingDefaultIdempotentComponents()
                .havingDefaultProcessorServices()
                .having(ConfigurationStore, (proc) -> proc.setConfigStore(new StatelessSidecarConfigurationStore()))
                .havingStandardInputsElements();
    }

    /**
     * Create sidecar processor defaults that would be suitable for running a proof-of-concept exercises.
     *
     * @return pre-configured instance.
     */
    public static SidecarProcessorDefaults poc() {
        return new SidecarProcessorDefaults()
                .havingDefaultMasheryConfigurationDialect()
                .havingDefaultErrorCodes()
                .having(ConfigurationStore, (proc) -> proc.setConfigStore(new StatelessSidecarConfigurationStore()))
                .havingDefaultCircuitBreaker()
                .having(Stacks, (proc) -> proc.setSidecarStacks(stacksFor(EchoStack.class, LogStack.class)))
                .havingStandardInputsElements()
                .havingDefaultProcessorServices();
    }

    public static SidecarProcessorDefaults essential(SidecarStacks stacks) {
        return new SidecarProcessorDefaults()
                .havingDefaultErrorCodes()
                .havingDefaultMasheryConfigurationDialect()
                .havingDefaultConfigurationStore()
                .havingDefaultCircuitBreaker()
                .havingStacks(stacks)
                .havingStandardInputsElements()
                .havingDefaultProcessorServices();
    }

    public static SidecarProcessorDefaults minimal(SidecarStacks stacks) {
        return new SidecarProcessorDefaults(essential(stacks))
                .havingLocalConfigurationLoader(new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT));
    }

    public static SidecarProcessorDefaults typical(SidecarStacks stacks) {
        return new SidecarProcessorDefaults(minimal(stacks))
                .havingDefaultIdempotentComponents();
    }

    public static SidecarProcessorDefaults advanced(SidecarStacks stacks) {
        return new SidecarProcessorDefaults(typical(stacks))
                .havingDefaultAsyncExecutors(SidecarProcessorConstants.DEFAULT_ASYNC_EXECUTORS_POOL_SIZE);
    }

    public static SidecarProcessorDefaults basicALCP(SidecarStacks stacks) {
        return new SidecarProcessorDefaults(advanced(stacks))
                .having(ALCPAlgorithmsFactory, (proc) -> {
                    ALCPAlgorithmFactory factory = new ALCPAlgorithmFactory();
                    factory.add(CallerAuthenticityAlgorithmV1.ALGORITHM_REF_NAME, CallerAuthenticityAlgorithmV1::fromSpec);

                    proc.setAlcpFactory(factory);
                })
                .havingALCPIdentity(new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT, SidecarProcessorConstants.ALCP_IDENTITY_FILE_NAME), new File(SidecarProcessorConstants.LOCAL_CONFIG_ROOT, SidecarProcessorConstants.ALCP_PASSWORD_FILE_NAME));
    }

    public static SidecarProcessorDefaults advancedALCP(SidecarStacks stacks) {
        return new SidecarProcessorDefaults(basicALCP(stacks))
                .having(ALCPAlgorithmsFactory, (proc) -> {
                    ALCPAlgorithmFactory factory = new ALCPAlgorithmFactory();
                    factory.add(CallerAuthenticityAlgorithmV1.ALGORITHM_REF_NAME, CallerAuthenticityAlgorithmV1::fromSpec);
                    factory.add(HighSecurityProtectionAlgorithmV1.ALGORITHM_REF_NAME, HighSecurityProtectionAlgorithmV1::fromSpec);

                    proc.setAlcpFactory(factory);
                });
    }

    public enum LaunchLevel {
        AdvancedALCP, BasicALCP, Advanced, Essential
    }
}
