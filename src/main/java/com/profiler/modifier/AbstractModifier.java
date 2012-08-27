package com.profiler.modifier;


import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void checkLibrary(ClassLoader classLoader, String javassistClassName) {
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
    }

}
