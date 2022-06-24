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

    private final Logger logger = LogManager.getLogger(JavaAssistUtilsTest.class.getName());


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
        Assertions.assertEquals("java.util.ArrayList", JavaAssistUtils.javaClassNameToObjectName(new ArrayList<Integer>().getClass().getName()));

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
                JavaAssistUtils.javaClassNameToObjectName(new SomeComparable[]{inner}.getClass().getName()));
        Assertions.assertEquals("java.util.Map$Entry", JavaAssistUtils.javaClassNameToObjectName(Map.Entry.class.getName()));
        Assertions.assertEquals("java.util.Map$Entry[]", JavaAssistUtils.javaClassNameToObjectName(Map.Entry[].class.getName()));
    }


    @Test
    public void toJvmSignature() {
        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("int"), "I");
        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("int[]"), "[I");
        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("int[][][]"), "[[[I");

        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("void"), "V");

        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String"), "Ljava/lang/String;");
        Assertions.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String[][]"), "[[Ljava/lang/String;");

        try {
            Assertions.assertEquals(JavaAssistUtils.toJvmSignature(""), "");
            Assertions.fail("empty string");
        } catch (Exception ignored) {
        }

        try {
            Assertions.assertEquals(JavaAssistUtils.toJvmSignature(null), null);
            Assertions.fail("null");
        } catch (Exception ignored) {
        }
    }

    @Test
    public void javaTypeToJvmSignature() {
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{}), "()");

        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}), "(I)");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}), "(ID)");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"byte", "float", "short"}), "(BFS)");


        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}), "(Ljava/lang/String;)");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}), "(Ljava/lang/String;J)");

        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}), "(JLjava/lang/Object;Z)");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}), "(CJLjava/lang/Object;Z)");
    }

    @Test
    public void javaTypeToJvmSignatureReturnType() {
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{}, "void"), "()V");

        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}, "int"), "(I)I");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}, "double"), "(ID)D");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"byte", "float", "short"}, "float"), "(BFS)F");


        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}, "java.lang.String"), "(Ljava/lang/String;)Ljava/lang/String;");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}, "long"), "(Ljava/lang/String;J)J");

        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}, "boolean"), "(JLjava/lang/Object;Z)Z");
        Assertions.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}, "java.lang.Object"), "(CJLjava/lang/Object;Z)Ljava/lang/Object;");
    }


    @Test
    public void testParseParameterDescriptor() throws Exception {
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("()V"), new String[]{});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(I)I"), new String[]{"int"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(ID)I"), new String[]{"int", "double"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(BFS)I"), new String[]{"byte", "float", "short"});


        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;)I"), new String[]{"java.lang.String"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;J)I"), new String[]{"java.lang.String", "long"});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(JLjava/lang/Object;Z)I"), new String[]{"long", "java.lang.Object", "boolean"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(CJLjava/lang/Object;Z)I"), new String[]{"char", "long", "java.lang.Object", "boolean"});

    }

    @Test
    public void testParseParameterDescriptor_array() throws Exception {

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([I)I"), new String[]{"int[]"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([IJ)I"), new String[]{"int[]", "long"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([J[I)I"), new String[]{"long[]", "int[]"});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([Ljava/lang/String;)"), new String[]{"java.lang.String[]"});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;[[J)"), new String[]{"java.lang.String", "long[][]"});
        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/Object;[[Ljava/lang/String;)"), new String[]{"java.lang.Object", "java.lang.String[][]"});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[Ljava/lang/String;)"), new String[]{"java.lang.String[][][]"});

        Assertions.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[I)"), new String[]{"int[][][]"});
    }


    @Test
    public void testGetParameterDescription2() throws Exception {
        @SuppressWarnings("deprecation")
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        Assertions.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

    @Test
    public void testJavaClassNameToJvmResourceName1() throws Exception {
        Assertions.assertEquals("java/lang/String.class", JavaAssistUtils.javaClassNameToJvmResourceName("java.lang.String"));
    }

    @Test
    public void testJavaClassNameToJvmResourceName2() throws Exception {
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

}
