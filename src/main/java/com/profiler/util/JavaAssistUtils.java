package com.profiler.util;

import javassist.*;
import javassist.bytecode.*;

public class JavaAssistUtils {
    private final static String EMTPY_ARRAY = "()";
    private static String[] EMPTY_STRING_ARRAY = new String[0];

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

    public static String mergeParameterVariableNameDescription(String[] paramterType, String[] variableName) {
        if (paramterType.length != variableName.length) {
            throw new IllegalArgumentException("args size not equal");
        }
        if (paramterType.length == 0) {
            return EMTPY_ARRAY;
        }

        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = paramterType.length - 1;
        for (int i = 0; i < paramterType.length; i++) {
            sb.append(paramterType[i]);
            sb.append(' ');
            sb.append(variableName[i]);
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
        return java.lang.reflect.Modifier.isStatic(modifiers);
    }


    public static String[] getParameterVariableName(CtBehavior method) throws NotFoundException {
        LocalVariableAttribute localVariableAttribute = lookupLocalVariableAttribute(method);
        return getParameterVariableName(method, localVariableAttribute);
    }

    /**
     * LocalVariable 메모리 공간을 얻어 온다.
     *
     * @param method
     * @return null일 경우 debug모드로 컴파일 되지 않아서 그럼.
     */
    public static LocalVariableAttribute lookupLocalVariableAttribute(CtBehavior method) {
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        AttributeInfo localVariableTable = codeAttribute.getAttribute(LocalVariableAttribute.tag);
        LocalVariableAttribute local = (LocalVariableAttribute) localVariableTable;
        return local;
    }

    public static String[] getParameterVariableName(CtBehavior method, LocalVariableAttribute localVariableAttribute) throws NotFoundException {
        // http://www.jarvana.com/jarvana/view/org/jboss/weld/servlet/weld-servlet/1.0.1-Final/weld-servlet-1.0.1-Final-sources.jar!/org/slf4j/instrumentation/JavassistHelper.java?format=ok
        // 이거 참고함.
        if (localVariableAttribute == null) {
            // null이라는건 debug모드로 컴파일 되지 않았다는 의미이다.
            // parameter class명을 default로 하자.
            return null;
        }
        CtClass[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] parameterVariableNames = new String[parameterTypes.length];
        int firstIndex = 0;
        int modifiers = method.getModifiers();
//      동기화 메소드라도 index를 증가시키면 안되는데. 참고 소스와는 뭔가 다름 나중에 문제가 생길수 있으니 일단 주석으로 적음.
//        if (Modifier.isSynchronized(modifiers)) {
//            firstIndex++;
//        }
        if (Modifier.isStatic(modifiers) == false) {
            firstIndex++;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            int accesIndex = firstIndex + i;
            String variablename = localVariableAttribute.variableName(accesIndex);
            parameterVariableNames[i] = variablename;
        }
        return parameterVariableNames;
    }
}
