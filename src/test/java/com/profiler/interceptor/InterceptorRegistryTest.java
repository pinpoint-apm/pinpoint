package com.profiler.interceptor;

import javassist.*;
import javassist.compiler.ast.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.junit.Test;
import org.omg.PortableInterceptor.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterceptorRegistryTest {
    @Test
    public void methodName() throws NoSuchMethodException {
        Method[] toString = Map.class.getDeclaredMethods();
        for(Method m : toString) {

            System.out.println(m);

            System.out.println(m.toGenericString());
        }

    }
   @Test
    public void intercet() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {

       InterceptorRegistry.addInterceptor("TargetGetClass", new Interceptor() {
            @Override
            public void before(InterceptorContext ctx) {
                System.out.println("before");
            }

            @Override
            public void after(InterceptorContext ctx) {
                System.out.println("after");
            }
        });


        ClassPool p = ClassPool.getDefault();
       CtClass throwable = p.get(Throwable.class.getName());
       CtClass ctx = p.get(InterceptorContext.class.getName());
       CtClass ctClass = p.get("com.profiler.interceptor.TestObject");
//       System.out.println(ctClass);
       CtMethod hello = ctClass.getMethod("hello", "()V");
       hello.addLocalVariable("ctx", ctx);

       hello.insertBefore("{" +
               "ctx = new com.profiler.interceptor.InterceptorContext();" +
               "ctx.setParameter($args);" +
               "System.out.println(ctx); " +
               InterceptorRegistry.class.getName() + ".getInterceptor(\"a\").before(null);" +
               "}");
       hello.insertAfter("{" +
                "System.out.println(ctx); " +
               InterceptorRegistry.class.getName() + ".getInterceptor(\"a\").after(null);" +
               "}");

       hello.addCatch("{" +
           InterceptorRegistry.class.getName() + ".getInterceptor(\"a\").after(null);" +
           " throw $e;" +
           "}", throwable);


       ctClass.freeze();

       Class aClass = ctClass.toClass();
       TestObject o = (TestObject) aClass.newInstance();

       o.hello();

       o.hello();
   }
}
                                     ;