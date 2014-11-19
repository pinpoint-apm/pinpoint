package com.nhn.pinpoint.bootstrap.plugin;

import java.util.List;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.exception.PinpointException;

public class BasicClassEditor implements DedicatedClassEditor {
    private final String targetClassName;
    private final List<MetadataInjector> metadataInjectors;
    private final List<InterceptorInjector> interceptorInjectors;
    

    public BasicClassEditor(String targetClassName, List<MetadataInjector> metadataInjectors, List<InterceptorInjector> interceptorInjectors) {
        this.targetClassName = targetClassName;
        this.metadataInjectors = metadataInjectors;
        this.interceptorInjectors = interceptorInjectors;
    }

    @Override
    public byte[] edit(ClassLoader classLoader, InstrumentClass target) {
        try {
            for (MetadataInjector injector : metadataInjectors) {
                injector.inject(classLoader, target); 
            }
            
            for (InterceptorInjector injector : interceptorInjectors) {
                injector.inject(classLoader, target);
            }
            
            return target.toBytecode();
        } catch (Throwable t) {
            throw new PinpointException("Fail to edit class: " + targetClassName, t);
        }
    }

    @Override
    public String getTargetClassName() {
        return targetClassName;
    }
    
    
}
