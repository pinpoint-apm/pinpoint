package com.nhn.pinpoint.bootstrap.plugin;

import java.security.ProtectionDomain;
import java.util.List;

import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.exception.PinpointException;

public class BasicClassEditor implements DedicatedClassEditor {
    private final ByteCodeInstrumentor instrumentor;
    
    private final String targetClassName;
    
    private final List<MetadataInjector> metadataInjectors;
    private final List<InterceptorInjector> interceptorInjectors;
    

    public BasicClassEditor(ByteCodeInstrumentor instrumentor, String targetClassName, List<MetadataInjector> metadataInjectors, List<InterceptorInjector> interceptorInjectors) {
        this.instrumentor = instrumentor;
        this.targetClassName = targetClassName;
        this.metadataInjectors = metadataInjectors;
        this.interceptorInjectors = interceptorInjectors;
    }

    @Override
    public byte[] edit(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        try {
            InstrumentClass target = instrumentor.getClass(classLoader, className, classFileBuffer);
            
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
