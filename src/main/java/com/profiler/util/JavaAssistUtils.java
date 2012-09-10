package com.profiler.util;

import javassist.*;
import javassist.bytecode.Descriptor;

public class JavaAssistUtils {
    private final static String NULL = "()";
    /**
     * test(int, java.lang.String) 일경우
     * (int, java.lang.String)로 생성된다.
     * @param params
     * @return
     */
    public static String getParameterDescription(CtClass[] params) {
        if(params == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("(");
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(")");
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

    public static String getParameterDescription(Class[] params) {
        if(params == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("(");
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getParameterDescription(String[] params) {
        if(params == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("(");
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(")");
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

    public CtMethod findAllMethod(CtClass ctClass, String methodName, String[] args) throws NotFoundException {
        CtClass[] params = getCtParameter(args, ctClass.getClassPool());
        String paramDescriptor = Descriptor.ofParameters(params);
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if(method.getName().equals(methodName) && method.getMethodInfo2().getDescriptor().startsWith(paramDescriptor)) {
                return method;
            }
        }
        throw new NotFoundException(methodName+ "(..) is not found in " + ctClass.getName());
    }

    public static boolean isStaticBehavior(CtBehavior behavior) {
		int modifiers = behavior.getModifiers();
		return java.lang.reflect.Modifier.isStatic(modifiers);
	}
}
