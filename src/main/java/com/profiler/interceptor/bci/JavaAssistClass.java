package com.profiler.interceptor.bci;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.*;
import javassist.*;

public class JavaAssistClass implements InstrumentClass {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private JavaAssistByteCodeInstrumentor instrumentor;
    private CtClass ctClass;

    public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
        this.instrumentor = instrumentor;
        this.ctClass = ctClass;
    }

    @Override
    public boolean addInterceptor(String methodName, String[] args, Interceptor interceptor) {
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
            return true;
        } catch (NotFoundException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } catch (CannotCompileException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return false;
    }

    private void addAroundInterceptor(String methodName, int id, CtBehavior method) throws NotFoundException, CannotCompileException {
        addStaticBeforeInterceptor(methodName, id, method);
        addStaticAfterInterceptor(methodName, id, method);
    }

    private void addStaticAfterInterceptor(String methodName, int id, CtBehavior behavior) throws  NotFoundException, CannotCompileException {
        StringBuilder after = new StringBuilder(1024);
        after.append("{");
        addGetStaticAfterInterceptor(after, id);
        after.append("  interceptor.after(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args, ($w)$_);");
        after.append("}");
        String buildAfter = after.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor after behavior:" + behavior.getLongName() + " code:" + buildAfter);
        }
        behavior.insertAfter(buildAfter);

        StringBuilder catchCode = new StringBuilder(1024);
        catchCode.append("{");
        addGetStaticAfterInterceptor(catchCode, id);
        catchCode.append("  interceptor.after(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args, $e);");
        catchCode.append("  throw $e;");
        catchCode.append("}");
        String buildCatch = catchCode.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildCatch);
        }
        CtClass th = instrumentor.getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, th);

    }

    private void addGetStaticAfterInterceptor(StringBuilder after, int id) {
        after.append("  com.profiler.interceptor.StaticAfterInterceptor interceptor = "
                + "(com.profiler.interceptor.StaticAfterInterceptor) com.profiler.interceptor.InterceptorRegistry.getInterceptor(");
        after.append(id);
        after.append(");");
    }

    private void addStaticBeforeInterceptor(String methodName, int id, CtBehavior behavior) throws CannotCompileException {
        StringBuilder code = new StringBuilder(1024);
        code.append("{");
        addGetBeforeInterceptor(id, code);
        code.append("  interceptor.before(this, \"" + ctClass.getName() + "\", \"" + methodName + "\", $args);");
        code.append("}");
        String buildBefore = code.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticBeforeInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildBefore);
        }

        if(behavior instanceof CtConstructor) {
            ((CtConstructor) behavior).insertBeforeBody(buildBefore);
        } else {
            behavior.insertBefore(buildBefore);
        }
    }

    private void addGetBeforeInterceptor(int id, StringBuilder code) {
        code.append("  com.profiler.interceptor.StaticBeforeInterceptor interceptor = "
                + "(com.profiler.interceptor.StaticBeforeInterceptor)com.profiler.interceptor.InterceptorRegistry.getInterceptor(");
        code.append(id);
        code.append(");");
    }

    public boolean addDebugLogBeforeAfterMethod() {
        String className = this.ctClass.getName();
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
        try {
            CtClass cc = this.instrumentor.getClassPool().get(className);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod method : methods) {
                if (method.isEmpty()) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(method.getLongName() + " is empty.");
                    }
                    continue;
                }
                String methodName = method.getName();

                // TODO method의 prameter type을 interceptor에 별도 추가해야 될것으로 보임.
                String params = getParamsToString(method.getParameterTypes());
                addAroundInterceptor(methodName, id, method);
            }
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 제대로 동작안함 다시 봐야 될것 같음.
     * 생성자일경우의 bytecode 수정시 에러가 남.
     * @return
     */
    @Deprecated
    public boolean addDebugLogBeforeAfterConstructor() {
        String className = this.ctClass.getName();
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
        try {
            CtClass cc = this.instrumentor.getClassPool().get(className);
            CtConstructor[] constructors = cc.getConstructors();

            for (CtConstructor constructor : constructors) {

                if (constructor.isEmpty()) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(constructor.getLongName() + " is empty.");
                    }
                    continue;
                }
                String constructorName = constructor.getName();
                String params = getParamsToString(constructor.getParameterTypes());

//                constructor.insertAfter("{System.out.println(\"*****" + constructorName + " Constructor:Param=(" + params + ") is finished. \" + $args);}");
//                constructor.addCatch("{System.out.println(\"*****" + constructorName + " Constructor:Param=(" + params + ") is finished.\"); throw $e; }"
//                        , instrumentor.getClassPool().get("java.lang.Throwable"));
                addAroundInterceptor(constructorName, id, constructor);
            }
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return false;
    }

    private String getParamsToString(CtClass[] params) throws NotFoundException {
        StringBuilder sb = new StringBuilder(512);
        if (params.length != 0) {
            int paramsLength = params.length;
            for (int loop = paramsLength - 1; loop > 0; loop--) {
                sb.append(params[loop].getName()).append(",");
            }
        }
        String paramsStr = sb.toString();
        if (logger.isLoggable(Level.FINE)) {
             logger.fine("params type:" + paramsStr);
        }
        return paramsStr;
    }


    private CtMethod getMethod(String methodName, String[] args) throws NotFoundException {
        CtClass[] params = getCtParameter(args);
        return ctClass.getDeclaredMethod(methodName, params);
    }

    private CtClass[] getCtParameter(String[] args) throws NotFoundException {
        if (args == null) {
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
            logger.log(Level.INFO, "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
        }
        return null;
    }

}
