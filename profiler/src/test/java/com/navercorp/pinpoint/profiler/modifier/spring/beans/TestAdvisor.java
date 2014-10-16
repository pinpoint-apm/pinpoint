package com.nhn.pinpoint.profiler.modifier.spring.beans;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

@SuppressWarnings("serial")
public class TestAdvisor extends AbstractPointcutAdvisor {

    @Override
    public Pointcut getPointcut() {
        return new Pointcut() {

            @Override
            public ClassFilter getClassFilter() {
                return new ClassFilter() {

                    @Override
                    public boolean matches(Class<?> clazz) {
                        return clazz.equals(ProxyTarget.class);
                    }
                
                };
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {

                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return true;
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object[] args) {
                        return true;
                    }
                };
            }
            
        };
    }

    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {

            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                return invocation.proceed();
            }
            
        };
    }
}
