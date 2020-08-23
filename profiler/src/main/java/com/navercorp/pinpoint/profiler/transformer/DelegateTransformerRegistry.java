package com.navercorp.pinpoint.profiler.transformer;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;

import java.lang.instrument.ClassFileTransformer;

public class DelegateTransformerRegistry implements TransformerRegistry {
    private final TransformerRegistry transformerRegistry;

    private final TransformerRegistry debugTransformerRegistry;

    public DelegateTransformerRegistry(TransformerRegistry transformerRegistry, TransformerRegistry debugTransformerRegistry) {
        this.transformerRegistry = Assert.requireNonNull(transformerRegistry, "transformerRegistry");
        this.debugTransformerRegistry = Assert.requireNonNull(debugTransformerRegistry, "debugTransformerRegistry");
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        final ClassFileTransformer transformer = this.transformerRegistry.findTransformer(classLoader, classInternalName, classFileBuffer);
        if (transformer != null) {
            return transformer;
        }
        // For debug
        // TODO What if a modifier is duplicated?
        final ClassFileTransformer debugTransformer = this.debugTransformerRegistry.findTransformer(classLoader, classInternalName, classFileBuffer);
        if (debugTransformer != null) {
            return debugTransformer;
        }
        return null;
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer, InternalClassMetadata classMetadata) {
        final ClassFileTransformer transformer = this.transformerRegistry.findTransformer(classLoader, classInternalName, classFileBuffer, classMetadata);
        if (transformer != null) {
            return transformer;
        }

        final ClassFileTransformer debugTransformer = this.debugTransformerRegistry.findTransformer(classLoader, classInternalName, classFileBuffer, classMetadata);
        if (debugTransformer != null) {
            return debugTransformer;
        }
        return null;
    }
}
