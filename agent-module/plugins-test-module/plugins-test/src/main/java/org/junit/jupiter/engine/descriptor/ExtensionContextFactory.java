package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.EngineExecutionListener;

import java.util.function.Function;

public final class ExtensionContextFactory {

    public static ExtensionContext jupiterEngineContext(EngineExecutionListener engineExecutionListener,
                                                        JupiterEngineDescriptor testDescriptor,
                                                        JupiterConfiguration configuration,
                                                        Function<ExtensionContext, ExecutableInvoker> executableInvokerFactory) {
        return new JupiterEngineExtensionContext(engineExecutionListener, testDescriptor, configuration, executableInvokerFactory);
    }

}
