package com.nhn.pinpoint.profiler.util;

import javassist.*;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class JavaAssistUtils {
    private final static String EMTPY_ARRAY = "()";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final Logger logger = LoggerFactory.getLogger(JavaAssistUtils.class);

    private JavaAssistUtils() {
    }

    /**
     * test(int, java.lang.String) 일경우
     * (int, java.lang.String)로 생성된다.
     *
     * @param params
     * @return
     */
    public static String getParameterDescription(CtClass[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }


    public static String[] getParameterType(Class[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getName();
        }
        return paramsString;
    }

    public static String[] getParameterSimpleType(CtClass[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getSimpleName();
        }
        return paramsString;
    }

    public static String[] getParameterType(CtClass[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getName();
        }
        return paramsString;
    }

    public static String getParameterDescription(Class[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }


    public static String getParameterDescription(String[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static CtClass[] getCtParameter(String[] args, ClassPool pool) throws NotFoundException {
        if (args == null) {
            return null;
        }
        CtClass[] params = new CtClass[args.length];
        for (int i = 0; i < args.length; i++) {
            params[i] = pool.getCtClass(args[i]);
        }
        return params;
    }


    public static int getLineNumber(CtBehavior method) {
        if (method == null) {
            return -1;
        }
        return method.getMethodInfo().getLineNumber(0);
    }


    public CtMethod findAllMethod(CtClass ctClass, String methodName, String[] args) throws NotFoundException {
        CtClass[] params = getCtParameter(args, ctClass.getClassPool());
        String paramDescriptor = Descriptor.ofParameters(params);
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals(methodName) && method.getMethodInfo2().getDescriptor().startsWith(paramDescriptor)) {
                return method;
            }
        }
        throw new NotFoundException(methodName + "(..) is not found in " + ctClass.getName());
    }

    public static boolean isStaticBehavior(CtBehavior behavior) {
        int modifiers = behavior.getModifiers();
        return Modifier.isStatic(modifiers);
    }


    public static String[] getParameterVariableName(CtBehavior method) throws NotFoundException {
        LocalVariableAttribute localVariableAttribute = lookupLocalVariableAttribute(method);
        if (localVariableAttribute == null) {
            return getParameterDefaultVariableName(method);
        }
        return getParameterVariableName(method, localVariableAttribute);
    }

    /**
     * LocalVariable 메모리 공간을 얻어 온다.
     *
     * @param method
     * @return null일 경우 debug모드로 컴파일 되지 않아서 그럼.
     */
    public static LocalVariableAttribute lookupLocalVariableAttribute(CtBehavior method) {
        MethodInfo methodInfo = method.getMethodInfo2();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        AttributeInfo localVariableTable = codeAttribute.getAttribute(LocalVariableAttribute.tag);
        LocalVariableAttribute local = (LocalVariableAttribute) localVariableTable;
        return local;
    }

    public static String[] getParameterVariableName(CtBehavior method, LocalVariableAttribute localVariableAttribute) throws NotFoundException {
        // http://www.jarvana.com/jarvana/view/org/jboss/weld/servlet/weld-servlet/1.0.1-Final/weld-servlet-1.0.1-Final-sources.jar!/org/slf4j/instrumentation/JavassistHelper.java?format=ok
        // http://grepcode.com/file/repo1.maven.org/maven2/jp.objectfanatics/assertion-weaver/0.0.30/jp/objectfanatics/commons/javassist/JavassistUtils.java
        // 이거 참고함.
        if (localVariableAttribute == null) {
            // null이라는건 debug모드로 컴파일 되지 않았다는 의미이다.
            // parameter class명을 default로 넘기는 건 아래 메소드가 함. getParameterDefaultVariableName.
            return null;
        }

        dump(localVariableAttribute);
        CtClass[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] parameterVariableNames = new String[parameterTypes.length];
        boolean thisExist = thisExist(method);

        int paramIndex = 0;
        for (int i = 0; i < localVariableAttribute.tableLength(); i++) {
            // start pc가 0이 아닐경우 parameter를 나타내는 localVariableName이 아님.
            if (localVariableAttribute.startPc(i) != 0) {
                continue;
            }
            int index = localVariableAttribute.index(i);
            if (index == 0 && thisExist) {
                // this 변수임. skip
                continue;
            }
            String variablename = localVariableAttribute.variableName(i);
            parameterVariableNames[paramIndex++] = variablename;
        }
        return parameterVariableNames;
    }

    private static boolean thisExist(CtBehavior method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            return false;
        } else {
            // this 포함이므로 1;
            return true;
        }
    }

    private static void dump(LocalVariableAttribute lva) {
        if (logger.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder(1024);
            for (int i = 0; i < lva.tableLength(); i++) {
                buffer.append("\n");
                buffer.append(i);
                buffer.append("  start_pc:");
                buffer.append(lva.startPc(i));
                buffer.append("  index:");
                buffer.append(lva.index(i));
                buffer.append("  name:");
                buffer.append(lva.variableName(i));
                buffer.append("  nameIndex:");
                buffer.append(lva.nameIndex(i));
            }
            logger.debug(buffer.toString());
        }
    }


    public static String[] getParameterDefaultVariableName(CtBehavior method) throws NotFoundException {
        CtClass[] parameterTypes = method.getParameterTypes();
        String[] variableName = new String[parameterTypes.length];
        for (int i = 0; i < variableName.length; i++) {
            variableName[i] = parameterTypes[i].getSimpleName().toLowerCase();
        }
        return variableName;
    }
}
