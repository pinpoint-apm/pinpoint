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

import com.navercorp.pinpoint.profiler.util.ApiUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class JavaAssistUtilsTest {
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
        logger.info(ctDescription);

        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{int.class});
        logger.info(clsDescription);
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


    @Test
    public void testGetLineNumber() throws Exception {
//        pool.appendClassPath(new ClassClassPath(AbstractHttpClient.class));
        CtClass ctClass = pool.get("org.apache.http.impl.client.AbstractHttpClient");
        CtClass params = pool.get("org.apache.http.params.HttpParams");
        // non-javadoc, see interface HttpClient
//        public synchronized final HttpParams getParams() {
//            if (defaultParams == null) {
//                defaultParams = createHttpParams();
//            }
//            return defaultParams;
//        }

        CtMethod setParams = ctClass.getDeclaredMethod("setParams", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.info("line:{}", lineNumber);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "params");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }

    @Test
    public void testVariableNameError1() throws Exception {
        CtClass ctClass = pool.get("com.mysql.jdbc.ConnectionImpl");
        CtMethod setParams = ctClass.getDeclaredMethod("setAutoCommit", new CtClass[]{CtClass.booleanType});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.info("line:{}", lineNumber);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "autoCommitFlag");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }

    @Test
    public void testVariableNameError2() throws Exception {
        CtClass ctClass = pool.get("com.mysql.jdbc.StatementImpl");
        CtClass params = pool.get("java.lang.String");
        CtMethod setParams = ctClass.getDeclaredMethod("executeQuery", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "sql");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }


    @Test
    public void testGetParameterDescription2() throws Exception {
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        logger.info(clsDescription);
        Assert.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

}
