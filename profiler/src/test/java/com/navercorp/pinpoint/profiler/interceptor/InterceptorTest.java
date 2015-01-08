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

package com.navercorp.pinpoint.profiler.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.test.util.LoaderUtils;
import javassist.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author emeroad
 */
public class InterceptorTest {

    private final Logger logger = LoggerFactory.getLogger(InterceptorTest.class.getName());


    //    @Test
    public void methodName() throws NoSuchMethodException {
        Method[] toString = Map.class.getDeclaredMethods();
        for (Method m : toString) {
            logger.info("methodObject:{}", m);
            logger.info("methodObject:{}", m.toGenericString());
        }
    }

    //    @Test
    public void ctClassName() throws NotFoundException {
        ClassPool pool = new ClassPool(true);

        CtClass ctClass = pool.get("java.lang.String");
        logger.info("ctClass:{}", ctClass);
        logger.info("ctClass:{}", ctClass.getName());
        logger.info("ctClass:{}", ctClass.getSimpleName());
    }

    //    @Deprecated
    public void interceptor() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, IOException, ClassNotFoundException, NoSuchMethodException {
        SimpleAroundInterceptor aroundInterceptor = new SimpleAroundInterceptor() {

            @Override
            public void before(Object target, Object[] args) {
                logger.info("before target:" + target + " args:" + Arrays.toString(args));
            }

            @Override
            public void after(Object target, Object[] args, Object result, Throwable throwable) {
                logger.info("after target: " + target + " args:" + Arrays.toString(args) + " result:" + result + " throwalbe:" + throwable);
            }
        };
        int interceptorId = InterceptorRegistry.addSimpleInterceptor(aroundInterceptor);


        final ClassPool classPool = new ClassPool(true);
        CtClass throwable = classPool.get(Throwable.class.getName());

        CtClass ctClass = classPool.get("com.navercorp.pinpoint.profiler.interceptor.JavaAssistTestObject");

        final CtMethod hello = ctClass.getMethod("hello", "(Ljava/lang/String;)Ljava/lang/String;");
        logger.debug("longName:{}", hello.getLongName());
        logger.debug("name:{}", hello.getName());

        String interceptorClassName = SimpleAroundInterceptor.class.getName();
        CtClass interceptor = classPool.get(interceptorClassName);
        hello.addLocalVariable("interceptor", interceptor);

        CtClass object = classPool.get(Object.class.getName());
        hello.addLocalVariable("result", object);

//        hello.insertBefore("{ System.out.println(\"before\"); }");
        hello.insertBefore("{" +
                "interceptor = (" + interceptorClassName + ") " + InterceptorRegistry.class.getName() + ".getSimpleInterceptor(" + interceptorId + ");" +
                "interceptor.before(this, $args);" +
        "}");
//        hello.addCatch("{" +
////            " interceptor.after(ctx);"+
////           " AroundInterceptor a = (AroundInterceptor) " + InterceptorRegistry.class.getName() + ".getInterceptor(\"a\");"+
//                " throw $e;" +
//                "}", throwable);
//        hello.insertAfter("{" +
//                "interceptor.after(this,  $args, ($w)$_, null); " +
//                "}");

//       hello.setBody(generatedAroundInterceptor("TestObject", "hello"));
//       hello.setBody("{ System.out.println(\"ddd\");  }", ClassMap map );
//       hello.insertBefore(" System.out.println(\" before +  \");");
//       hello.insertAfter(" System.out.println($_);");
//       hello.insertAfter(" System.out.println($r);");
//       hello.insertAfter(" System.out.println($w);");
//       hello.insertAfter(" System.out.println($sig);");
//       hello.insertAfter(" System.out.println($type);");
//       hello.insertAfter(" System.out.println($class);");
//       hello.instrument(new ExprEditor() {
//         public void edit(MethodCall m)
//         throws CannotCompileException
//         {
//             try {
//                 System.out.println("method call" + m.getMethod().getName());
//             } catch (NotFoundException e) {
//                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//             }
//             String code = generatedAroundInterceptor("TestObject", "hello");
//             m.replace(code);
//         }


//         });
//        hello.addCatch("System.out.println(\"catch\"); throw $e;", throwable);

//       hello.setName("__hello");
//       CtMethod method = CtNewMethod.make("public void hello() { try {__hello(); } catch(Throwable th){throw th;}}", ctClass);

//         CtMethod method = CtNewMethod.make("public void hello() { System.out.println(\"ddd\"); } catch(Throwable th){throw th;}}", ctClass);
//       ctClass.addMethod(method);


//        ctClass.freeze();
//       ctClass.writeFile("./debug");
//       ctClass.debugWriteFile("./debug");
        Loader loader = LoaderUtils.createLoader(classPool);
        loader.delegateLoadingOf("com.navercorp.pinpoint.bootstrap.");

        Class aClass = loader.loadClass(ctClass.getName());
        Object testObject = aClass.newInstance();

        Method helloMethod = testObject.getClass().getDeclaredMethod("hello", String.class);

        try {
            helloMethod.invoke(testObject, "hello~~");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

//       o.hello();
    }

    private String generatedAroundInterceptor(String className, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("  ctx = new InterceptorContext();");
        sb.append("  ctx.setParameter($args);");
        sb.append("  ctx.setTarget(this);");
        sb.append(" ");
//        sb.append("  ctx.setMethodName(\"" + methodName + "\");");
//        sb.append("  System.out.println(\"args check : \" + $args );");
//        sb.append("  System.out.println(\"0 check : \" + $0 );");
//        sb.append("  System.out.println(\"1 check : \" + $1 );");
//        sb.append("  System.out.println(\"sig check : \" + $sig );");
//        sb.append("  System.out.println(\"class check : \" + $class );");
//        sb.append("  System.out.println(\" r check : \" + $r);");

        sb.append("}");
        sb.append("{");
        sb.append("  interceptor = (AroundInterceptor) " + InterceptorRegistry.class.getName() + ".getInterceptor(\"a\");");
        sb.append("  interceptor.before(ctx);");
        sb.append("  result = null;");
//        println(sb, "before systemout \"ttt\"");
        sb.append("}");
        sb.append("try {");
        sb.append("  $_ = $proceed($$);");
        sb.append("  result = $_;");
        sb.append("}");
//        sb.append("catch(Throwable th) {");
//        sb.append("  System.out.println(\"test11\" + th);");
//        sb.append("  ctx.setErrCode(th);");
//        sb.append("  System.out.println(\"catch\");");
//        sb.append("}");
        sb.append("finally {");
//        sb.append("  System.out.println(\"finally\");");
        sb.append("  ctx.setReturnValue(result);");
        sb.append("  interceptor.after(ctx);");
        sb.append("}");
//        System.out.println(sb);
        return sb.toString();
    }

    public void println(StringBuilder sb, String out) {
        sb.append("System.out.println(\"" + out.replace("\"", "\\\"") + "\");");
    }
}
