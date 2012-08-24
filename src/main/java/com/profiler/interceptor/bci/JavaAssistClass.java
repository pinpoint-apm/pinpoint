package com.profiler.interceptor.bci;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.*;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

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
        int id = InterceptorRegistry.addInterceptor(interceptor);
        try {
            CtMethod method = getMethod(methodName, args);
            if (interceptor instanceof StaticAroundInterceptor) {
                addAroundInterceptor(methodName, id, method);
            } else if (interceptor instanceof StaticBeforeInterceptor) {
                addStaticBeforeInterceptor(methodName, id, method);
            } else if (interceptor instanceof StaticAfterInterceptor) {
                addStaticAfterInterceptor(methodName, id, method);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private void addAroundInterceptor(String methodName, int id, CtMethod method) throws NotFoundException, CannotCompileException {
        addStaticBeforeInterceptor(methodName, id, method);
        addStaticAfterInterceptor(methodName, id, method);
    }



    private void addStaticAfterInterceptor(String methodName, int id, CtMethod method) throws CannotCompileException, NotFoundException {
        StringBuilder after = new StringBuilder(1024);
        after.append("{");
        addGetStaticAfterInterceptor(after, id);
        after.append("  interceptor.after(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args, $_);");
        after.append("}");
        String buildAfter = after.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor after method:" + method.getLongName() + " code:" + buildAfter);
        }
        method.insertAfter(buildAfter);

        StringBuilder catchCode = new StringBuilder(1024);
        catchCode.append("{");
        addGetStaticAfterInterceptor(catchCode, id);
        catchCode.append("  interceptor.after(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args, $e);");
        catchCode.append("  throw $e;");
        catchCode.append("}");
        String buildCatch = catchCode.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor catch method:" + method.getLongName() + " code:" + buildCatch);
        }
        CtClass th = instrumentor.getClassPool().get("java.lang.Throwable");
        method.addCatch(buildCatch, th);

    }

    private void addGetStaticAfterInterceptor(StringBuilder after, int id) {
        after.append("  com.profiler.interceptor.StaticAfterInterceptor interceptor = "
                + "(com.profiler.interceptor.StaticAfterInterceptor) com.profiler.interceptor.InterceptorRegistry.getInterceptor(");
        after.append(id);
        after.append(");");
    }

    private void addStaticBeforeInterceptor(String methodName, int id, CtMethod method) throws CannotCompileException {
        StringBuilder code = new StringBuilder(1024);
        code.append("{");
        addGetBeforeInterceptor(id, code);
        code.append("  interceptor.before(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args);");
        code.append("}");
        String buildBefore = code.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticBeforeInterceptor catch method:" + method.getLongName() + " code:" + buildBefore);
        }
        method.insertBefore(buildBefore);
    }

    private void addGetBeforeInterceptor(int id, StringBuilder code) {
        code.append("  com.profiler.interceptor.StaticBeforeInterceptor interceptor = "
                + "(com.profiler.interceptor.StaticBeforeInterceptor)com.profiler.interceptor.InterceptorRegistry.getInterceptor(");
        code.append(id);
        code.append(");");
    }


    private CtMethod getMethod(String methodName, String[] args) throws NotFoundException {
        CtClass[] params = getCtParameter(args);
        return ctClass.getDeclaredMethod(methodName, params);
    }

    private CtClass[] getCtParameter(String[] args) throws NotFoundException {
        if(args == null) {
            return null;
        }
        CtClass[] params = new CtClass[args.length];
        for (int i = 0; i < args.length; i++) {
            params[i] = instrumentor.getClassPool().getCtClass(args[i]);
        }
        return params;
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

    public Class toClass() {
        try {
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
