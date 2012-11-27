package com.profiler.interceptor.bci;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ApiUtils;
import com.profiler.interceptor.*;
import com.profiler.util.JavaAssistUtils;
import javassist.*;

public class JavaAssistClass implements InstrumentClass {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private JavaAssistByteCodeInstrumentor instrumentor;
    private CtClass ctClass;

    public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
        this.instrumentor = instrumentor;
        this.ctClass = ctClass;
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    @Override
    public boolean insertCodeBeforeConstructor(String[] args, String code) {
        try {
            CtConstructor constructor = getConstructor(args);
            constructor.insertBefore(code);
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeAfterConstructor(String[] args, String code) {
        try {
            CtConstructor constructor = getConstructor(args);
            constructor.insertAfter(code);
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeBeforeMethod(String methodName, String[] args, String code) {
        try {
            CtMethod method = getMethod(methodName, args);
            method.insertBefore(code);
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeAfterMethod(String methodName, String[] args, String code) {
        try {
            CtMethod method = getMethod(methodName, args);
            method.insertAfter(code);
            return true;
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return false;
        }
    }

    public void addTraceVariable(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException {
        addTraceVariable0(variableName, setterName, getterName, variableType, initValue);
    }

    public void addTraceVariable(String variableName, String setterName, String getterName, String variableType) throws InstrumentException {
        addTraceVariable0(variableName, setterName, getterName, variableType, null);
    }

    public void addTraceVariable0(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException {
        try {
            CtClass type = instrumentor.getClassPool().get(variableType);
            CtField traceVariable = new CtField(type, variableName, ctClass);
            if (initValue == null) {
                ctClass.addField(traceVariable);
            } else {
                ctClass.addField(traceVariable, initValue);
            }
            if (setterName != null) {
                CtMethod setterMethod = CtNewMethod.setter(setterName, traceVariable);
                ctClass.addMethod(setterMethod);
            }
            if (getterName != null) {
                CtMethod getterMethod = CtNewMethod.getter(getterName, traceVariable);
                ctClass.addMethod(getterMethod);
            }
        } catch (NotFoundException e) {
            throw new InstrumentException(variableName + " addTraceVariable fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(variableName + " addTraceVariable fail. Cause:" + e.getMessage(), e);
        }
    }

    public int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        return addInterceptor0(null, args, interceptor, -1, Type.auto);
    }


    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        return addInterceptor0(methodName, args, interceptor, -1, Type.auto);
    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException {
        return addInterceptor0(methodName, args, null, interceptorId, Type.auto);
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        return addInterceptor0(methodName, args, interceptor, -1, type);
    }

    private CtBehavior getBehavior(String methodName, String[] args) throws NotFoundException {
        if (methodName == null) {
            return getConstructor(args);
        }
        return getMethod(methodName, args);
    }

    private int addInterceptor0(String methodName, String[] args, Interceptor interceptor, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException {
        CtBehavior behavior = null;
        try {
            behavior = getBehavior(methodName, args);
        } catch (NotFoundException e) {
            // target method나 constructor를 차지 못했을 경우는 NotFoundInstrumentException을 던진다.
            if (interceptor == null) {
                throw new NotFoundInstrumentException(interceptorId + " add fail. Cause:" + e.getMessage(), e);
            } else {
                throw new NotFoundInstrumentException(interceptor.getClass().getSimpleName() + " add fail. Cause:" + e.getMessage(), e);
            }
        }
        try {
            if (interceptor != null) {
                interceptorId = InterceptorRegistry.addInterceptor(interceptor);
                if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
                    setMethodDescriptor(behavior, (ByteCodeMethodDescriptorSupport) interceptor);
                }
                if (interceptor instanceof ApiIdSupport) {
                    setApiId(behavior, (ApiIdSupport) interceptor);
                }
            } else {
                interceptor = InterceptorRegistry.getInterceptor(interceptorId);
            }

            if (type == Type.auto) {
                if (interceptor instanceof StaticAroundInterceptor) {
                    addStaticAroundInterceptor(methodName, interceptorId, behavior);
                } else if (interceptor instanceof StaticBeforeInterceptor) {
                    addStaticBeforeInterceptor(methodName, interceptorId, behavior);
                } else if (interceptor instanceof StaticAfterInterceptor) {
                    addStaticAfterInterceptor(methodName, interceptorId, behavior);
                } else {
                    throw new IllegalArgumentException("unsupported");
                }
            } else if (type == Type.around && interceptor instanceof StaticAroundInterceptor) {
                addStaticAroundInterceptor(methodName, interceptorId, behavior);
            } else if (type == Type.before && interceptor instanceof StaticBeforeInterceptor) {
                addStaticBeforeInterceptor(methodName, interceptorId, behavior);
            } else if (type == Type.after && interceptor instanceof StaticAfterInterceptor) {
                addStaticAfterInterceptor(methodName, interceptorId, behavior);
            } else {
                throw new IllegalArgumentException("unsupported");
            }
            return interceptorId;
        } catch (NotFoundException e) {
            throw new InstrumentException(interceptor.getClass().getSimpleName() + " add fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(interceptor.getClass().getSimpleName() + "add fail. Cause:" + e.getMessage(), e);
        }
    }

    private void setApiId(CtBehavior behavior, ApiIdSupport interceptor) throws NotFoundException {
        CtClass[] parameterTypes = behavior.getParameterTypes();
        String[] parameterType = JavaAssistUtils.getParameterType(parameterTypes);
        int apiId = ApiMappingTable.findApiId(ctClass.getName(), behavior.getName(), parameterType);
        interceptor.setApiId(apiId);
    }

    private void setMethodDescriptor(CtBehavior behavior, ByteCodeMethodDescriptorSupport interceptor) throws NotFoundException {
        DefaultMethodDescriptor methodDescriptor = new DefaultMethodDescriptor();

        String methodName = behavior.getName();
        methodDescriptor.setMethodName(methodName);

        methodDescriptor.setClassName(ctClass.getName());

        CtClass[] parameterTypes = behavior.getParameterTypes();
        String[] parameterType = JavaAssistUtils.getParameterType(parameterTypes);
        methodDescriptor.setParameterTypes(parameterType);

        String[] parameterVariableName = JavaAssistUtils.getParameterVariableName(behavior);
        methodDescriptor.setParameterVariableName(parameterVariableName);

        int lineNumber = JavaAssistUtils.getLineNumber(behavior);
        methodDescriptor.setLineNumber(lineNumber);

        String parameterDescription = ApiUtils.mergeParameterVariableNameDescription(parameterType, parameterVariableName);
        methodDescriptor.setParameterDescriptor(parameterDescription);


        interceptor.setMethodDescriptor(methodDescriptor);
    }

    private void addStaticAroundInterceptor(String methodName, int id, CtBehavior method) throws NotFoundException, CannotCompileException {
        addStaticBeforeInterceptor(methodName, id, method);
        addStaticAfterInterceptor(methodName, id, method);
    }

    private void addStaticAfterInterceptor(String methodName, int id, CtBehavior behavior) throws NotFoundException, CannotCompileException {

        String target = getTarget(behavior);
        String returnType = getReturnType(behavior);
        String parameterTypeString = JavaAssistUtils.getParameterDescription(behavior.getParameterTypes());
        String parameter = getParameter(behavior);

        CodeBuilder after = new CodeBuilder();
        after.begin();
        after.format("  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAfterInterceptor.class.getName(), id);
        after.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s);", target, ctClass.getName(), methodName, parameterTypeString, parameter, returnType);
        after.end();
        String buildAfter = after.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor after behavior:" + behavior.getLongName() + " code:" + buildAfter);
        }
        behavior.insertAfter(buildAfter);


        CodeBuilder catchCode = new CodeBuilder();
        catchCode.begin();
        catchCode.format("  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAfterInterceptor.class.getName(), id);
        catchCode.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, $e);", target, ctClass.getName(), methodName, parameterTypeString, parameter);
        catchCode.append("  throw $e;");
        catchCode.end();
        String buildCatch = catchCode.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticAfterInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildCatch);
        }
        CtClass th = instrumentor.getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, th);

    }

    private String getTarget(CtBehavior behavior) {
        boolean staticMethod = JavaAssistUtils.isStaticBehavior(behavior);
        if (staticMethod) {
            return "null";
        } else {
            return "this";
        }
    }

    public String getReturnType(CtBehavior behavior) throws NotFoundException {
        if (behavior instanceof CtMethod) {
            CtClass returnType = ((CtMethod) behavior).getReturnType();
            if (CtClass.voidType == returnType) {
                return "null";
            }
        }
        return "($w)$_";
    }


    private void addStaticBeforeInterceptor(String methodName, int id, CtBehavior behavior) throws CannotCompileException, NotFoundException {
        String target = getTarget(behavior);
        // 인터셉터 호출시 최대한 연산량을 줄이기 위해서 정보는 가능한 정적 데이터로 생성한다.
        String parameterDescription = JavaAssistUtils.getParameterDescription(behavior.getParameterTypes());
        String parameter = getParameter(behavior);

        CodeBuilder code = new CodeBuilder();
        code.begin();
        code.format("  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticBeforeInterceptor.class.getName(), id);
        code.format("  interceptor.before(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s);", target, ctClass.getName(), methodName, parameterDescription, parameter);
        code.end();
        String buildBefore = code.toString();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("addStaticBeforeInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildBefore);
        }

        if (behavior instanceof CtConstructor) {
            ((CtConstructor) behavior).insertBeforeBody(buildBefore);
        } else {
            behavior.insertBefore(buildBefore);
        }
    }

    private String getParameter(CtBehavior behavior) throws NotFoundException {
        CtClass[] parameterTypes = behavior.getParameterTypes();
        if (parameterTypes.length == 0) {
            return "null";
        }
        return "$args";
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
                addStaticAroundInterceptor(methodName, id, method);
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
     * 제대로 동작안함 다시 봐야 될것 같음. 생성자일경우의 bytecode 수정시 에러가 남.
     *
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

                // constructor.insertAfter("{System.out.println(\"*****" +
                // constructorName + " Constructor:Param=(" + params +
                // ") is finished. \" + $args);}");
                // constructor.addCatch("{System.out.println(\"*****" +
                // constructorName + " Constructor:Param=(" + params +
                // ") is finished.\"); throw $e; }"
                // , instrumentor.getClassPool().get("java.lang.Throwable"));
                addStaticAroundInterceptor(constructorName, id, constructor);
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
        CtClass[] params = JavaAssistUtils.getCtParameter(args, instrumentor.getClassPool());
        // cttime에는 직접 구현클래스를 조작해야 되므로 상속관계의 method를 찾으면 안됨.
        return ctClass.getDeclaredMethod(methodName, params);
    }

    private CtConstructor getConstructor(String[] args) throws NotFoundException {
        CtClass[] params = JavaAssistUtils.getCtParameter(args, instrumentor.getClassPool());
        return ctClass.getDeclaredConstructor(params);
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

    public Class<?> toClass() {
        try {
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            logger.log(Level.INFO, "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
        }
        return null;
    }
}
