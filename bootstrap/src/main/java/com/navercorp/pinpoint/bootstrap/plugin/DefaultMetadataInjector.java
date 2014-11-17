package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.nhn.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;

public class DefaultMetadataInjector implements MetadataInjector {
    
    private final Class<? extends TraceValue> metadataType;
    private final MetadataInitializationStrategy strategy;
    
    public DefaultMetadataInjector(Class<? extends TraceValue> metadataType, MetadataInitializationStrategy strategy) {
        this.metadataType = metadataType;
        this.strategy = strategy;
    }

    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        if (strategy == null) {
            target.addTraceValue(metadataType);
        } else {
            if (strategy instanceof ByConstructor) {
                String javaExpression = "new " + ((ByConstructor)strategy).getClassName() + "();";
                target.addTraceValue(metadataType, javaExpression);
            } else {
                throw new IllegalArgumentException("Unsupported strategy: " + strategy);
            }
        }
    }
}
