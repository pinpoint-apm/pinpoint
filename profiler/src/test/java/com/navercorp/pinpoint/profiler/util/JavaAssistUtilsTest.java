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

import javassist.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class JavaAssistUtilsTest {

    private static final String TEST_CLASS_NAME = "com.navercorp.pinpoint.profiler.util.JavaAssistUtilsTest";

    private final Logger logger = LoggerFactory.getLogger(JavaAssistUtilsTest.class.getName());
    private ClassPool pool;

    @Before
    public void setUp() throws Exception {
        pool = new ClassPool();
        pool.appendSystemPath();
    }

    @Test
    public void testGetParameterDescription() throws Exception {
        CtClass ctClass = pool.get("java.lang.String");
        CtMethod substring = ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType});

        String ctDescription = JavaAssistUtils.getParameterDescription(substring.getParameterTypes());
        logger.debug(ctDescription);

        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{int.class});
        logger.debug(clsDescription);
        Assert.assertEquals(ctDescription, clsDescription);
    }


    @Test
    public void javaArraySize() {
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize(""), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("[]"), 1);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("[][][]"), 3);

        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int"), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int[]"), 1);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int[][][]"), 3);


        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("java.lang.String"), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("java.lang.String[][]"), 2);
    }

    @Test
    public void javaClassNameToObjectName() {
        // primitives
        Assert.assertEquals("boolean", JavaAssistUtils.javaClassNameToObjectName(boolean.class.getName()));
        Assert.assertEquals("byte", JavaAssistUtils.javaClassNameToObjectName(byte.class.getName()));
        Assert.assertEquals("char", JavaAssistUtils.javaClassNameToObjectName(char.class.getName()));
        Assert.assertEquals("double", JavaAssistUtils.javaClassNameToObjectName(double.class.getName()));
        Assert.assertEquals("float", JavaAssistUtils.javaClassNameToObjectName(float.class.getName()));
        Assert.assertEquals("int", JavaAssistUtils.javaClassNameToObjectName(int.class.getName()));
        Assert.assertEquals("short", JavaAssistUtils.javaClassNameToObjectName(short.class.getName()));

        // wrappers
        Assert.assertEquals("java.lang.Integer", JavaAssistUtils.javaClassNameToObjectName(Integer.class.getName()));
        Assert.assertEquals("java.lang.String", JavaAssistUtils.javaClassNameToObjectName(String.class.getName()));

        // classes
        Assert.assertEquals("java.util.List", JavaAssistUtils.javaClassNameToObjectName(List.class.getName()));
        Assert.assertEquals("java.util.ArrayList", JavaAssistUtils.javaClassNameToObjectName(new ArrayList<Integer>().getClass().getName()));

        // arrays
        Assert.assertEquals("boolean[]", JavaAssistUtils.javaClassNameToObjectName(boolean[].class.getName()));
        Assert.assertEquals("byte[]", JavaAssistUtils.javaClassNameToObjectName(byte[].class.getName()));
        Assert.assertEquals("java.lang.String[]", JavaAssistUtils.javaClassNameToObjectName(String[].class.getName()));

        // inner/nested classes
        Assert.assertEquals(
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
        Assert.assertEquals(
                this.getClass().getName() + "$1SomeComparable",
                JavaAssistUtils.javaClassNameToObjectName(inner.getClass().getName())); // assume nothing else is defined in this class
        Assert.assertEquals(
                this.getClass().getName() + "$1SomeComparable[]",
                JavaAssistUtils.javaClassNameToObjectName(new SomeComparable[] {inner}.getClass().getName()));
        Assert.assertEquals("java.util.Map$Entry", JavaAssistUtils.javaClassNameToObjectName(Map.Entry.class.getName()));
        Assert.assertEquals("java.util.Map$Entry[]", JavaAssistUtils.javaClassNameToObjectName(Map.Entry[].class.getName()));
    }


    @Test
    public void toJvmSignature() {
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int"), "I");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int[]"), "[I");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int[][][]"), "[[[I");

        Assert.assertEquals(JavaAssistUtils.toJvmSignature("void"), "V");

        Assert.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String"), "Ljava/lang/String;");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String[][]"), "[[Ljava/lang/String;");

        try {
            Assert.assertEquals(JavaAssistUtils.toJvmSignature(""), "");
            Assert.fail("empty string");
        } catch (Exception ignore) {
        }

        try {
            Assert.assertEquals(JavaAssistUtils.toJvmSignature(null), null);
            Assert.fail("null");
        } catch (Exception ignore) {
        }
    }

    @Test
     public void javaTypeToJvmSignature() {
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{}), "()");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}), "(I)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}), "(ID)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature( new String[]{"byte", "float", "short"}), "(BFS)");


        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}), "(Ljava/lang/String;)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}), "(Ljava/lang/String;J)");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}), "(JLjava/lang/Object;Z)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}), "(CJLjava/lang/Object;Z)");
    }

    @Test
    public void javaTypeToJvmSignatureReturnType() {
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{}, "void"), "()V");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}, "int"), "(I)I");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}, "double"), "(ID)D");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature( new String[]{"byte", "float", "short"}, "float"), "(BFS)F");


        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}, "java.lang.String"), "(Ljava/lang/String;)Ljava/lang/String;");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}, "long"), "(Ljava/lang/String;J)J");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}, "boolean"), "(JLjava/lang/Object;Z)Z");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}, "java.lang.Object"), "(CJLjava/lang/Object;Z)Ljava/lang/Object;");
    }


    @Test
    public void testParseParameterDescriptor() throws Exception {
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("()V"), new String[]{});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(I)I"), new String[]{"int"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(ID)I"), new String[]{"int", "double"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(BFS)I"), new String[]{"byte", "float", "short"});


        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;)I"), new String[]{"java.lang.String"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;J)I"), new String[]{"java.lang.String", "long"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(JLjava/lang/Object;Z)I"), new String[]{"long", "java.lang.Object", "boolean"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(CJLjava/lang/Object;Z)I"), new String[]{"char", "long", "java.lang.Object", "boolean"});

    }

    @Test
    public void testParseParameterDescriptor_array() throws Exception {

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([I)I"), new String[]{"int[]"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([IJ)I"), new String[]{"int[]", "long"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([J[I)I"), new String[]{"long[]", "int[]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([Ljava/lang/String;)"), new String[]{"java.lang.String[]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;[[J)"), new String[]{"java.lang.String", "long[][]"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/Object;[[Ljava/lang/String;)"), new String[]{"java.lang.Object", "java.lang.String[][]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[Ljava/lang/String;)"), new String[]{"java.lang.String[][][]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[I)"), new String[]{"int[][][]"});
    }

    public void getLineNumber_testAPI(String params) {
    }

    @Test
    public void testGetLineNumber() throws Exception {
//        pool.appendClassPath(new ClassClassPath(AbstractHttpClient.class));
        CtClass ctClass = pool.get("com.navercorp.pinpoint.profiler.util.JavaAssistUtilsTest");
        CtClass params = pool.get("java.lang.String");
        // non-javadoc, see interface HttpClient
//        public synchronized final HttpParams getParams() {
//            if (defaultParams == null) {
//                defaultParams = createHttpParams();
//            }
//            return defaultParams;
//        }

        CtMethod setParams = ctClass.getDeclaredMethod("getLineNumber_testAPI", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.debug("line:{}", lineNumber);

        logger.debug(setParams.getName());
        logger.debug(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.debug(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "params");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.debug(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.debug(s);
    }

    public void testVariableNameError1_testAPI(boolean autoCommitFlag) {
        logger.debug("testVariableNameError1_testAPI test api");
    }

    @Test
    public void testVariableNameError1() throws Exception {
        CtClass ctClass = pool.get(TEST_CLASS_NAME);
        CtMethod setParams = ctClass.getDeclaredMethod("testVariableNameError1_testAPI", new CtClass[]{CtClass.booleanType});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.debug("line:{}", lineNumber);

        logger.debug(setParams.getName());
        logger.debug(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.debug(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "autoCommitFlag");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.debug(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.debug(s);
    }

    public void testVariableNameError2_testAPI(String sql) {
        logger.debug("testVariableNameError1_testAPI test api");
    }

    @Test
    public void testVariableNameError2() throws Exception {
        CtClass ctClass = pool.get(TEST_CLASS_NAME);
        CtClass params = pool.get("java.lang.String");
        CtMethod setParams = ctClass.getDeclaredMethod("testVariableNameError2_testAPI", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);

        logger.debug(setParams.getName());
        logger.debug(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.debug(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "sql");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.debug(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.debug(s);
    }


    @Test
    public void testGetParameterDescription2() throws Exception {
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        logger.debug(clsDescription);
        Assert.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

}
