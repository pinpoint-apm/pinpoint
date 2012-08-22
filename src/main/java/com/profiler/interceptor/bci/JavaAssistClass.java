package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;
import javassist.CannotCompileException;
import javassist.CtClass;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaAssistClass implements InstrumentClass {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private JavaAssistByteCodeInstrumentor instrumentor;
    private CtClass ctClass;

    public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
        this.instrumentor = instrumentor;
        this.ctClass = ctClass;
    }

    @Override
    public void addInterceptor(String methodName, String[] args, Interceptor interceptor) {


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
