package com.nhn.pinpoint.profiler.modifier.spring.beans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nhn.pinpoint.profiler.util.ForkRunner;
import com.nhn.pinpoint.profiler.util.PinpointConfig;

@RunWith(ForkRunner.class)
@PinpointConfig("pinpoint-spring-bean-test.config")
public class AbstractAutowireCapableBeanFactoryModifierIT {

    @Test
    public void test() {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");
        
        Maru maru = context.getBean(Maru.class);
        maru.publicMethod();
        
        context.getBean("morae");
        context.getBean("mozzi");
        context.getBean("excluded");
        context.getBean("outer");
    }
}
