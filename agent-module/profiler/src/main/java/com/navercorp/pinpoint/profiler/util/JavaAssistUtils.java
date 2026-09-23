/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.objectweb.asm.Type;


/**
 * @author emeroad
 */
public final class JavaAssistUtils {
    private static final String EMPTY_PARAMETER = "()";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String ARRAY = "[]";

    private static final String CLASS_POST_FIX = ".class";

    private JavaAssistUtils() {
    }


    public static String javaTypeToJvmSignature(String[] javaTypeArray, String returnType) {
        if (returnType == null) {
            throw new NullPointerException("returnType");
        }
        final String parameterSignature = javaTypeToJvmSignature(javaTypeArray);
        return parameterSignature.concat(toJvmSignature(returnType));
    }

    public static String javaTypeToJvmSignature(String[] javaTypeArray) {
        if (ArrayUtils.isEmpty(javaTypeArray)) {
            return EMPTY_PARAMETER;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        for (String javaType : javaTypeArray) {
            final String jvmSignature = toJvmSignature(javaType);
            buffer.append(jvmSignature);
        }
        buffer.append(')');
        return buffer.toString();

    }

    public static String toJvmSignature(String javaType) {
        if (javaType == null) {
            throw new NullPointerException("javaType");
        }
        if (javaType.isEmpty()) {
            throw new IllegalArgumentException("javaType is empty");
        }

        final int javaObjectArraySize = getJavaObjectArraySize(javaType);
        final int end = getEndIndex(javaType, javaObjectArraySize);
        final String signature = getSignature(javaType, 0);
        if (signature != null) {
            // primitive type
            return appendJvmArray(signature, javaObjectArraySize);
        }
        return toJvmObject(javaObjectArraySize, javaType, 0, end);
    }

    private static int getEndIndex(String javaType, int javaObjectArraySize) {
        if (javaObjectArraySize == 0) {
            return javaType.length();
        }
        // pure java
        final int javaArrayLength = javaObjectArraySize * 2;
        return javaType.length() - javaArrayLength;
    }

    static String getSignature(String pureJavaType, int index) {
        final char first = pureJavaType.charAt(index);
        switch (first) {
            case 'b':
                if (pureJavaType.startsWith("boolean", index)) {
                    return "Z";
                } else if (pureJavaType.startsWith("byte", index)) {
                    return "B";
                }
                break;
            case 'c':
                if (pureJavaType.startsWith("char", index)) {
                    return "C";
                }
                break;
            case 'd':
                if (pureJavaType.startsWith("double", index)) {
                    return "D";
                }
                break;
            case 'f':
                if (pureJavaType.startsWith("float", index)) {
                    return "F";
                }
                break;
            case 'i':
                if (pureJavaType.startsWith("int", index)) {
                    return "I";
                }
                break;
            case 'l':
                if (pureJavaType.startsWith("long", index)) {
                    return "J";
                }
                break;
            case 's':
                if (pureJavaType.startsWith("short", index)) {
                    return "S";
                }
                break;
            case 'v':
                if (pureJavaType.startsWith("void", index)) {
                    return "V";
                }
                break;
        }
        return null;
    }

    private static String toJvmObject(int javaObjectArraySize, String pureJavaType, int begin, int end) {
        //        "java.lang.String[][]"->"[[Ljava.lang.String;"
        final StringBuilder buffer = new StringBuilder(pureJavaType.length() + javaObjectArraySize + 2);
        for (int i = 0; i < javaObjectArraySize; i++) {
            buffer.append('[');
        }
        buffer.append('L');
        StringMatchUtils.appendAndReplace(pureJavaType, begin, end, '.', '/', buffer);
        buffer.append(';');
        return buffer.toString();
    }

    /**
     * java.lang.String -> java/lang/String
     *
     * @param javaName
     * @return
     */
    public static String javaNameToJvmName(String javaName) {
        if (javaName == null) {
            throw new NullPointerException("javaName");
        }
        return javaName.replace('.', '/');
    }

    /**
     * java/lang/String -> java.lang.String
     *
     * @param jvmName
     * @return
     */
    public static String jvmNameToJavaName(String jvmName) {
        if (jvmName == null) {
            throw new NullPointerException("jvmName");
        }
        return jvmName.replace('/', '.');
    }


    /**
     * java.lang.String -> java/lang/String.class
     *
     * @param javaName
     * @return
     */
    public static String javaClassNameToJvmResourceName(String javaName) {
        if (javaName == null) {
            throw new NullPointerException("javaName");
        }
        final int index = javaName.indexOf('.');
        if (index == -1) {
            return javaName + CLASS_POST_FIX;
        }

        StringBuilder builder = new StringBuilder(javaName.length() + CLASS_POST_FIX.length());
        builder.append(javaName, 0, index);

        StringMatchUtils.appendAndReplace(javaName, index, '.', '/', builder);

        builder.append(CLASS_POST_FIX);
        return builder.toString();
    }

    /**
     * java/lang/String -> java.lang.String
     *
     * @param jvmNameArray
     * @return
     */
    public static List<String> jvmNameToJavaName(List<String> jvmNameArray) {
        if (CollectionUtils.isEmpty(jvmNameArray)) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>(jvmNameArray.size());
        for (String jvmName : jvmNameArray) {
            list.add(jvmNameToJavaName(jvmName));
        }
        return list;
    }

    private static String appendJvmArray(String signature, int javaObjectArraySize) {
        if (javaObjectArraySize == 0) {
            return signature;
        }
        StringBuilder sb = new StringBuilder(signature.length() + javaObjectArraySize);
        for (int i = 0; i < javaObjectArraySize; i++) {
            sb.append('[');
        }
        sb.append(signature);
        return sb.toString();
    }

    static int getJavaObjectArraySize(String javaType) {
        if (javaType == null) {
            throw new NullPointerException("javaType");
        }
        if (javaType.isEmpty()) {
            return 0;
        }
        return StringMatchUtils.endsWithCountMatches(javaType, ARRAY);
    }

    public static String[] parseParameterSignature(String signature) {
        if (signature == null) {
            throw new NullPointerException("signature");
        }
        final Type[] argumentTypes = Type.getArgumentTypes(signature);
        if (argumentTypes.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final String[] objectType = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            objectType[i] = argumentTypes[i].getClassName();
        }
        return objectType;
    }

    public static String javaClassNameToObjectName(String javaClassName) {
        final char scheme = javaClassName.charAt(0);
        if (scheme == '[') {
            // "[Ljava.lang.String;" -> "java.lang.String[]"
            return Type.getType(javaClassName).getClassName();
        }
        return javaClassName;
    }

    // to variable name.
    // '.' '$' '[' ']' => '_'
    private static final char[] CLASS_REPLACE_CHAR =  ".$[]".toCharArray();

    public static String javaClassNameToVariableName(String javaClassName) {
        if (javaClassName == null) {
            throw new NullPointerException("javaClassName");
        }

        final int index = StringMatchUtils.indexOf(javaClassName, CLASS_REPLACE_CHAR);
        if (index == -1) {
            return javaClassName;
        }

        StringBuilder builder = new StringBuilder(javaClassName.length());
        builder.append(javaClassName, 0, index);
        for (int i = index; i < javaClassName.length(); i++) {
            final char c = javaClassName.charAt(i);
            if (StringMatchUtils.contains(c, CLASS_REPLACE_CHAR)) {
                builder.append('_');
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String[] getParameterType(Class<?>[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getName();
        }
        return paramsString;
    }

    public static String[] toPinpointParameterType(Class<?>[] paramClasses) {
        if (paramClasses == null) {
            return null;
        }

        String[] paramsString = new String[paramClasses.length];
        for (int i = 0; i < paramClasses.length; i++) {
            paramsString[i] = ReflectionUtils.getParameterTypeName(paramClasses[i]);
        }

        return paramsString;
    }

    public static String toPinpointParameterType(Class<?> type) {
        return ReflectionUtils.getParameterTypeName(type);
    }

    @Deprecated
    public static String getParameterDescription(Class<?>[] params) {
        if (params == null) {
            return EMPTY_PARAMETER;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        sb.append(params[0].getName());
        for (int i = 1; i < params.length; i++) {
            sb.append(", ");
            sb.append(params[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }


    public static String getParameterDescription(String[] params) {
        if (ArrayUtils.isEmpty(params)) {
            return EMPTY_PARAMETER;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        sb.append(params[0]);
        for (int i = 1; i < params.length; i++) {
            sb.append(", ");
            sb.append(params[i]);
        }
        sb.append(')');
        return sb.toString();
    }


}
