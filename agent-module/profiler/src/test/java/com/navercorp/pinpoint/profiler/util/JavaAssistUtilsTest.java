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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class JavaAssistUtilsTest {

    private static final String TEST_CLASS_NAME = "com.navercorp.pinpoint.profiler.util.JavaAssistUtilsTest";

    private final Logger logger = LogManager.getLogger(this.getClass());


    @Test
    public void javaArraySize() {
        Assertions.assertEquals(0, JavaAssistUtils.getJavaObjectArraySize(""));
        Assertions.assertEquals(1, JavaAssistUtils.getJavaObjectArraySize("[]"));
        Assertions.assertEquals(3, JavaAssistUtils.getJavaObjectArraySize("[][][]"));

        Assertions.assertEquals(0, JavaAssistUtils.getJavaObjectArraySize("int"));
        Assertions.assertEquals(1, JavaAssistUtils.getJavaObjectArraySize("int[]"));
        Assertions.assertEquals(3, JavaAssistUtils.getJavaObjectArraySize("int[][][]"));

        Assertions.assertEquals(0, JavaAssistUtils.getJavaObjectArraySize("java.lang.String"));
        Assertions.assertEquals(2, JavaAssistUtils.getJavaObjectArraySize("java.lang.String[][]"));
    }

    @Test
    public void javaArraySize_invalid() {
        Assertions.assertEquals(2, JavaAssistUtils.getJavaObjectArraySize("[]test[][]"));
    }

    @Test
    public void javaClassNameToObjectName() {
        // primitives
        Assertions.assertEquals("boolean", JavaAssistUtils.javaClassNameToObjectName(boolean.class.getName()));
        Assertions.assertEquals("byte", JavaAssistUtils.javaClassNameToObjectName(byte.class.getName()));
        Assertions.assertEquals("char", JavaAssistUtils.javaClassNameToObjectName(char.class.getName()));
        Assertions.assertEquals("double", JavaAssistUtils.javaClassNameToObjectName(double.class.getName()));
        Assertions.assertEquals("float", JavaAssistUtils.javaClassNameToObjectName(float.class.getName()));
        Assertions.assertEquals("int", JavaAssistUtils.javaClassNameToObjectName(int.class.getName()));
        Assertions.assertEquals("short", JavaAssistUtils.javaClassNameToObjectName(short.class.getName()));

        // wrappers
        Assertions.assertEquals("java.lang.Integer", JavaAssistUtils.javaClassNameToObjectName(Integer.class.getName()));
        Assertions.assertEquals("java.lang.String", JavaAssistUtils.javaClassNameToObjectName(String.class.getName()));

        // classes
        Assertions.assertEquals("java.util.List", JavaAssistUtils.javaClassNameToObjectName(List.class.getName()));
        Assertions.assertEquals("java.util.ArrayList", JavaAssistUtils.javaClassNameToObjectName(ArrayList.class.getName()));

        // arrays
        Assertions.assertEquals("boolean[]", JavaAssistUtils.javaClassNameToObjectName(boolean[].class.getName()));
        Assertions.assertEquals("byte[]", JavaAssistUtils.javaClassNameToObjectName(byte[].class.getName()));
        Assertions.assertEquals("java.lang.String[]", JavaAssistUtils.javaClassNameToObjectName(String[].class.getName()));

        // inner/nested classes
        Assertions.assertEquals(
                this.getClass().getName() + "$1",
                JavaAssistUtils.javaClassNameToObjectName(new Comparable<Long>() {
                    @Override
                    public int compareTo(Long o) {
                        return 0;
                    }
                }.getClass().getName()));
        class SomeComparable implements Comparable<Long> {
            @Override
            public int compareTo(Long o) {
                return 0;
            }
        }
        SomeComparable inner = new SomeComparable();
        Assertions.assertEquals(
                this.getClass().getName() + "$1SomeComparable",
                JavaAssistUtils.javaClassNameToObjectName(inner.getClass().getName())); // assume nothing else is defined in this class
        Assertions.assertEquals(
                this.getClass().getName() + "$1SomeComparable[]",
                JavaAssistUtils.javaClassNameToObjectName(SomeComparable[].class.getName()));
        Assertions.assertEquals("java.util.Map$Entry", JavaAssistUtils.javaClassNameToObjectName(Map.Entry.class.getName()));
        Assertions.assertEquals("java.util.Map$Entry[]", JavaAssistUtils.javaClassNameToObjectName(Map.Entry[].class.getName()));
    }


    @Test
    public void toJvmSignature() {
        Assertions.assertEquals("I", JavaAssistUtils.toJvmSignature("int"));
        Assertions.assertEquals("[I", JavaAssistUtils.toJvmSignature("int[]"));
        Assertions.assertEquals("[[[I", JavaAssistUtils.toJvmSignature("int[][][]"));

        Assertions.assertEquals("V", JavaAssistUtils.toJvmSignature("void"));

        Assertions.assertEquals("Ljava/lang/String;", JavaAssistUtils.toJvmSignature("java.lang.String"));
        Assertions.assertEquals("[[Ljava/lang/String;", JavaAssistUtils.toJvmSignature("java.lang.String[][]"));

        Assertions.assertThrows(Exception.class, () -> {
            JavaAssistUtils.toJvmSignature("");
        });

        Assertions.assertThrows(Exception.class, () -> {
            JavaAssistUtils.toJvmSignature(null);
        });
    }

    @Test
    public void javaTypeToJvmSignature() {
        Assertions.assertEquals("()", JavaAssistUtils.javaTypeToJvmSignature(new String[]{}));

        Assertions.assertEquals("(I)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}));
        Assertions.assertEquals("(ID)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}));
        Assertions.assertEquals("(BFS)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"byte", "float", "short"}));


        Assertions.assertEquals("(Ljava/lang/String;)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}));
        Assertions.assertEquals("(Ljava/lang/String;J)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}));

        Assertions.assertEquals("(JLjava/lang/Object;Z)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}));
        Assertions.assertEquals("(CJLjava/lang/Object;Z)", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}));
    }

    @Test
    public void javaTypeToJvmSignatureReturnType() {
        Assertions.assertEquals("()V", JavaAssistUtils.javaTypeToJvmSignature(new String[]{}, "void"));

        Assertions.assertEquals("(I)I", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}, "int"));
        Assertions.assertEquals("(ID)D", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}, "double"));
        Assertions.assertEquals("(BFS)F", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"byte", "float", "short"}, "float"));


        Assertions.assertEquals("(Ljava/lang/String;)Ljava/lang/String;", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}, "java.lang.String"));
        Assertions.assertEquals("(Ljava/lang/String;J)J", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}, "long"));

        Assertions.assertEquals("(JLjava/lang/Object;Z)Z", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}, "boolean"));
        Assertions.assertEquals("(CJLjava/lang/Object;Z)Ljava/lang/Object;", JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}, "java.lang.Object"));
    }


    @Test
    public void testParseParameterDescriptor() {
        Assertions.assertArrayEquals(new String[]{}, JavaAssistUtils.parseParameterSignature("()V"));

        Assertions.assertArrayEquals(new String[]{"int"}, JavaAssistUtils.parseParameterSignature("(I)I"));
        Assertions.assertArrayEquals(new String[]{"int", "double"}, JavaAssistUtils.parseParameterSignature("(ID)I"));
        Assertions.assertArrayEquals(new String[]{"byte", "float", "short"}, JavaAssistUtils.parseParameterSignature("(BFS)I"));


        Assertions.assertArrayEquals(new String[]{"java.lang.String"}, JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;)I"));
        Assertions.assertArrayEquals(new String[]{"java.lang.String", "long"}, JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;J)I"));

        Assertions.assertArrayEquals(new String[]{"long", "java.lang.Object", "boolean"}, JavaAssistUtils.parseParameterSignature("(JLjava/lang/Object;Z)I"));
        Assertions.assertArrayEquals(new String[]{"char", "long", "java.lang.Object", "boolean"}, JavaAssistUtils.parseParameterSignature("(CJLjava/lang/Object;Z)I"));

    }

    @Test
    public void testParseParameterDescriptor_array() {

        Assertions.assertArrayEquals(new String[]{"int[]"}, JavaAssistUtils.parseParameterSignature("([I)I"));
        Assertions.assertArrayEquals(new String[]{"int[]", "long"}, JavaAssistUtils.parseParameterSignature("([IJ)I"));
        Assertions.assertArrayEquals(new String[]{"long[]", "int[]"}, JavaAssistUtils.parseParameterSignature("([J[I)I"));

        Assertions.assertArrayEquals(new String[]{"java.lang.String[]"}, JavaAssistUtils.parseParameterSignature("([Ljava/lang/String;)"));

        Assertions.assertArrayEquals(new String[]{"java.lang.String", "long[][]"}, JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;[[J)"));
        Assertions.assertArrayEquals(new String[]{"java.lang.Object", "java.lang.String[][]"}, JavaAssistUtils.parseParameterSignature("(Ljava/lang/Object;[[Ljava/lang/String;)"));

        Assertions.assertArrayEquals(new String[]{"java.lang.String[][][]"}, JavaAssistUtils.parseParameterSignature("([[[Ljava/lang/String;)"));

        Assertions.assertArrayEquals(new String[]{"int[][][]"}, JavaAssistUtils.parseParameterSignature("([[[I)"));
    }


    @Test
    public void testGetParameterDescription2() {
        @SuppressWarnings("deprecation")
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        Assertions.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

    @Test
    public void testJavaClassNameToJvmResourceName1() {
        Assertions.assertEquals("java/lang/String.class", JavaAssistUtils.javaClassNameToJvmResourceName("java.lang.String"));
    }

    @Test
    public void testJavaClassNameToJvmResourceName2() {
        Assertions.assertEquals("java/lang/String.class", JavaAssistUtils.javaClassNameToJvmResourceName("java/lang/String"));
    }

    @Test
    public void testToPinpointParameterType() {
        int[][] stringArray = new int[0][0];
        String parameterType = JavaAssistUtils.toPinpointParameterType(stringArray.getClass());
        Assertions.assertEquals("int[][]", parameterType);
    }

    @Test
    public void javaClassNameToVariableName1() {
        String variableName = JavaAssistUtils.javaClassNameToVariableName("Test$CgLib");
        Assertions.assertEquals("Test_CgLib", variableName);
    }

    @Test
    public void javaClassNameToVariableName2() {
        String variableName = JavaAssistUtils.javaClassNameToVariableName("Test$$CgLib");
        Assertions.assertEquals("Test__CgLib", variableName);
    }

    @Test
    public void javaClassNameToVariableName3() {
        String variableName = JavaAssistUtils.javaClassNameToVariableName("Test$");
        Assertions.assertEquals("Test_", variableName);
    }

    @Test
    public void javaClassNameToVariableName_same_ref() {
        String className = "Test";
        String variableName = JavaAssistUtils.javaClassNameToVariableName(className);
        Assertions.assertSame(className, variableName);
    }

    @Test
    public void getParameterDescription() {
        String[] parameters = {"a", "b"};
        String variableName = JavaAssistUtils.getParameterDescription(parameters);
        Assertions.assertEquals("(a, b)", variableName);
    }

    @Test
    public void getParameterDescription_single() {
        String[] parameters = {"a"};
        String variableName = JavaAssistUtils.getParameterDescription(parameters);
        Assertions.assertEquals("(a)", variableName);
    }

    @Test
    public void getSignature() {
        Assertions.assertEquals("Z", JavaAssistUtils.getSignature("boolean", 0));
        Assertions.assertEquals("Z", JavaAssistUtils.getSignature(" boolean", 1));
    }

}
