package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformRequestListener;
import com.navercorp.pinpoint.bootstrap.instrument.RequestHandle;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ClassFileTransformerDispatcher extends ClassFileTransformer {
    @Override
    byte[] transform(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException;

}
