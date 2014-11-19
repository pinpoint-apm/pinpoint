package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.nhn.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;
import com.nhn.pinpoint.exception.PinpointException;

public class DefaultMetadataInjector implements MetadataInjector {
    
    private final String metadataAccessorTypeName;
    private final MetadataInitializationStrategy strategy;
    
    public DefaultMetadataInjector(String metadataAccessorTypeName, MetadataInitializationStrategy strategy) {
        this.metadataAccessorTypeName = metadataAccessorTypeName;
        this.strategy = strategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        Class<?> type;
        try {
            type = classLoader.loadClass(metadataAccessorTypeName);
        } catch (ClassNotFoundException e) {
            throw new PinpointException("Fail to load metadata accessor: " + metadataAccessorTypeName, e);
        }
        
        if (!TraceValue.class.isAssignableFrom(type)) {
            throw new PinpointException("Given type " + metadataAccessorTypeName + " is not a subtype of TraceValue");
        }
        
        Class<? extends TraceValue> metadataAccessorType = (Class<? extends TraceValue>)type; 
        
        if (strategy == null) {
            target.addTraceValue(metadataAccessorType);
        } else {
            if (strategy instanceof ByConstructor) {
                String javaExpression = "new " + ((ByConstructor)strategy).getClassName() + "();";
                target.addTraceValue(metadataAccessorType, javaExpression);
            } else {
                throw new PinpointException("Unsupported strategy: " + strategy);
            }
        }
    }
}
