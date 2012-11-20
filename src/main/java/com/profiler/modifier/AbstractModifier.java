package com.profiler.modifier;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.InstrumentException;
import javassist.ClassPool;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;

public abstract class AbstractModifier implements Modifier {

    private final Logger logger = Logger.getLogger(AbstractModifier.class.getName());

    protected final ClassPool classPool;
    protected ByteCodeInstrumentor byteCodeInstrumentor;

    public AbstractModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.classPool = byteCodeInstrumentor.getClassPool();
    }

    public void printClassConvertComplete(String javassistClassName) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(javassistClassName + " class is converted.");
        }
    }

}
