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
	
	public byte[] addBeforeAfterLogics(String javassistClassName) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtMethod[] methods = cc.getDeclaredMethods();

			for (CtMethod method : methods) {
				if (!method.isEmpty()) {
					String methodName = method.getName();
					CtClass[] params = method.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;

						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}
					method.insertBefore("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is started.\");}");
					method.insertAfter("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is finished.\");}");
				} else {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning(method.getLongName() + " is empty !!!!!");
                    }

                }
			}

			CtConstructor[] constructors = cc.getConstructors();

			for (CtConstructor constructor : constructors) {

				if (!constructor.isEmpty()) {
					CtClass[] params = constructor.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;
						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}

					constructor.insertBefore("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is started.\");}");
					constructor.insertAfter("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is finished.\");}");
				} else {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.warning(constructor.getLongName() + " is empty !!!!!");
                    }

                }
			}
			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
			return null;
		}
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
