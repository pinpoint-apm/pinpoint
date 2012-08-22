package com.profiler.interceptor.bci;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.InterceptorRegistry;
import com.profiler.interceptor.StaticAroundInterceptor;

public class JavaAssistClass implements InstrumentClass {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private JavaAssistByteCodeInstrumentor instrumentor;
	private CtClass ctClass;

	private final AtomicInteger interceptorNo = new AtomicInteger(1);

	public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
		this.instrumentor = instrumentor;
		this.ctClass = ctClass;
	}

	@Override
	public void addInterceptor(String methodName, String[] args, Interceptor interceptor) {
		int num = interceptorNo.getAndIncrement();
		InterceptorRegistry.addInterceptor(num, interceptor);

		try {
			CtClass[] params = new CtClass[args.length];

			for (int i = 0; i < args.length; i++) {
				params[i] = instrumentor.getClassPool().getCtClass(args[i]);
			}
			CtMethod method = ctClass.getDeclaredMethod(methodName, params);

//			if (interceptor instanceof StaticAroundInterceptor) {
				method.insertBefore("System.out.println(\"request=\" + this.getClass().getClassLoader()); ((com.profiler.interceptor.StaticAroundInterceptor) com.profiler.interceptor.InterceptorRegistry.getInterceptor(" + num + ")).before(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args);");
//			}

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (CannotCompileException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] toBytecode() {
		try {
			return ctClass.toBytecode();
		} catch (IOException e) {
			logger.log(Level.INFO, "IoException class:" + ctClass.getName() + " " + e.getMessage(), e);
		} catch (CannotCompileException e) {
			logger.log(Level.INFO, "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
		}
		return null;
	}
}
