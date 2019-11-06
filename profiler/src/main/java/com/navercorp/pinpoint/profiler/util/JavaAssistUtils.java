/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navercorp.pinpoint.common.util.StringUtils;


/**
 * @author emeroad
 */
public final class JavaAssistUtils {
    private static final String EMPTY_ARRAY = "()";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String ARRAY = "[]";


    private static final Pattern PARAMETER_SIGNATURE_PATTERN = Pattern.compile("\\[*L[^;]+;|\\[*[ZBCSIFDJ]|[ZBCSIFDJ]");

    private static final Map<String, String> PRIMITIVE_JAVA_TO_JVM = createPrimitiveJavaToJvmMap();

    private static Map<String, String> createPrimitiveJavaToJvmMap() {
        final Map<String, String> primitiveJavaToJvm = new HashMap<String, String>();
        primitiveJavaToJvm.put("byte", "B");
        primitiveJavaToJvm.put("char", "C");
        primitiveJavaToJvm.put("double", "D");
        primitiveJavaToJvm.put("float", "F");
        primitiveJavaToJvm.put("int", "I");
        primitiveJavaToJvm.put("long", "J");
        primitiveJavaToJvm.put("short", "S");
        primitiveJavaToJvm.put("void", "V");
        primitiveJavaToJvm.put("boolean", "Z");
        return primitiveJavaToJvm;
    }

    private JavaAssistUtils() {
    }


    public static String javaTypeToJvmSignature(String[] javaTypeArray, String returnType) {
        if (returnType == null) {
            throw new NullPointerException("returnType");
        }
        final String parameterSignature = javaTypeToJvmSignature(javaTypeArray);
        final StringBuilder sb = new StringBuilder(parameterSignature.length() + 8);
        sb.append(parameterSignature);
        sb.append(toJvmSignature(returnType));
        return sb.toString();
    }

    public static String javaTypeToJvmSignature(String[] javaTypeArray) {
        if (com.navercorp.pinpoint.common.util.ArrayUtils.isEmpty(javaTypeArray)) {
            return "()";
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
            throw new IllegalArgumentException("invalid javaType. \"\"");
        }

        final int javaObjectArraySize = getJavaObjectArraySize(javaType);
        final int javaArrayLength = javaObjectArraySize * 2;
        String pureJavaType;
        if (javaObjectArraySize != 0) {
            // pure java
            pureJavaType = javaType.substring(0, javaType.length() - javaArrayLength);
        } else {
            pureJavaType = javaType;
        }
        final String signature = PRIMITIVE_JAVA_TO_JVM.get(pureJavaType);
        if (signature != null) {
            // primitive type
            return appendJvmArray(signature, javaObjectArraySize);
        }
        return toJvmObject(javaObjectArraySize, pureJavaType);

    }

    private static String toJvmObject(int javaObjectArraySize, String pureJavaType) {
        //        "java.lang.String[][]"->"[[Ljava.lang.String;"
        final StringBuilder buffer = new StringBuilder(pureJavaType.length() + javaObjectArraySize + 2);
        for (int i = 0; i < javaObjectArraySize; i++) {
            buffer.append('[');
        }
        buffer.append('L');
        buffer.append(javaNameToJvmName(pureJavaType));
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
        return javaName.replace('.', '/').concat(".class");
    }

    /**
     * java/lang/String -> java.lang.String
     *
     * @param jvmNameArray
     * @return
     */
    public static List<String> jvmNameToJavaName(List<String> jvmNameArray) {
        if (jvmNameArray == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<String>(jvmNameArray.size());
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
        final int endIndex = javaType.length() - 1;
        final char checkEndArrayExist = javaType.charAt(endIndex);
        if (checkEndArrayExist != ']') {
            return 0;
        }
        int arraySize = 0;
        for (int i = endIndex; i > 0; i = i - 2) {
            final char arrayEnd = javaType.charAt(i);
            final char arrayStart = javaType.charAt(i - 1);
            if (arrayStart == '[' && arrayEnd == ']') {
                arraySize++;
            } else {
                return arraySize;
            }
        }
        return arraySize;
    }

    public static String[] parseParameterSignature(String signature) {
        if (signature == null) {
            throw new NullPointerException("signature");
        }
        final List<String> parameterSignatureList = splitParameterSignature(signature);
        if (parameterSignatureList.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        final String[] objectType = new String[parameterSignatureList.size()];
        for (int i = 0; i < parameterSignatureList.size(); i++) {
            final String parameterSignature = parameterSignatureList.get(i);
            objectType[i] = byteCodeSignatureToObjectType(parameterSignature, 0);
        }
        return objectType;
    }

    public static String javaClassNameToObjectName(String javaClassName) {
        final char scheme = javaClassName.charAt(0);
        switch (scheme) {
            case '[':
                return toArrayType(javaClassName);
            default:
                return javaClassName;
        }
    }

    // to variable name.
    // '.' '$' '[' ']' => '_'
    public static String javaClassNameToVariableName(String javaClassName) {
        if (javaClassName == null) {
            throw new NullPointerException("java class name");
        }

        return javaClassName.replace('.', '_').replace('$', '_').replace('[', '_').replace(']', '_');
    }

    private static String byteCodeSignatureToObjectType(String signature, int startIndex) {
        final char scheme = signature.charAt(startIndex);
        switch (scheme) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            case 'L':
                return toObjectType(signature, startIndex + 1);
            case '[': {
                return toArrayType(signature);
            }
        }
        throw new IllegalArgumentException("invalid signature :" + signature);
    }

    private static String toArrayType(String description) {
        final int arraySize = getArraySize(description);
        final String objectType = byteCodeSignatureToObjectType(description, arraySize);
        return arrayType(objectType, arraySize);
    }

    private static String arrayType(String objectType, int arraySize) {
        final int arrayStringLength = ARRAY.length() * arraySize;
        StringBuilder sb = new StringBuilder(objectType.length() + arrayStringLength);
        sb.append(objectType);
        for (int i = 0; i < arraySize; i++) {
            sb.append(ARRAY);
        }
        return sb.toString();
    }

    private static int getArraySize(String description) {
        if (StringUtils.isEmpty(description)) {
            return 0;
        }
        int arraySize = 0;
        for (int i = 0; i < description.length(); i++) {
            final char c = description.charAt(i);
            if (c == '[') {
                arraySize++;
            } else {
                break;
            }
        }
        return arraySize;
    }

    private static String toObjectType(String signature, int startIndex) {
        // Ljava/lang/String;
        final String assistClass = signature.substring(startIndex, signature.length() - 1);
        final String objectName = jvmNameToJavaName(assistClass);
        if (objectName.isEmpty()) {
            throw new IllegalArgumentException("invalid signature. objectName not found :" + signature);
        }
        return objectName;
    }


    private static List<String> splitParameterSignature(String signature) {
        final String parameterSignature = getParameterSignature(signature);
        if (parameterSignature.isEmpty()) {
            return Collections.emptyList();
        }
        final Matcher matcher = PARAMETER_SIGNATURE_PATTERN.matcher(parameterSignature);
        final List<String> parameterTypeList = new ArrayList<String>();
        while (matcher.find()) {
            parameterTypeList.add(matcher.group());
        }
        return parameterTypeList;
    }


    private static String getParameterSignature(String signature) {
        int start = signature.indexOf('(');
        if (start == -1) {
            throw new IllegalArgumentException("'(' not found. signature:" + signature);
        }
        final int end = signature.indexOf(')', start + 1);
        if (end == -1) {
            throw new IllegalArgumentException("')' not found. signature:" + signature);
        }
        start = start + 1;
        if (start == end) {
            return "";
        }
        return signature.substring(start, end);
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

    public static String[] toPinpointParameterType(Class<?>[] paramClasses) {
        if (paramClasses == null) {
            return null;
        }

        String[] paramsString = new String[paramClasses.length];
        for (int i = 0; i < paramClasses.length; i++) {
            paramsString[i] = toPinpointParameterType(paramClasses[i]);
        }

        return paramsString;
    }

    public static String toPinpointParameterType(Class<?> type) {
        if (type.isArray()) {
            return toPinpointParameterType(type.getComponentType()) + "[]";
        } else {
            return type.getName();
        }
    }


    @Deprecated
    public static String getParameterDescription(Class[] params) {
        if (params == null) {
            return EMPTY_ARRAY;
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
            return EMPTY_ARRAY;
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


}
