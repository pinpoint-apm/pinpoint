package com.nhn.pinpoint.profiler.modifier.spring.beans;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;
import com.nhn.pinpoint.profiler.junit4.PinpointJUnit4ClassRunner;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor.AbstractAutowireCapableBeanFactoryInterceptor;
import com.nhn.pinpoint.profiler.util.ClassTransformHelper;
import com.nhn.pinpoint.profiler.util.MockAgent;

@RunWith(PinpointJUnit4ClassRunner.class)
public class AbstractAutowireCapableBeanFactoryModifierTest extends BasePinpointTest {

    @Test
    public void test() throws Exception {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");
        ClassFileRetransformer retransformer = mock(ClassFileRetransformer.class);
        Modifier beanModifier = mock(Modifier.class);
        Interceptor interceptor = AbstractAutowireCapableBeanFactoryInterceptor.get(agent.getProfilerConfig(), retransformer, beanModifier); 
        
        AbstractAutowireCapableBeanFactoryModifier modifier = new AbstractAutowireCapableBeanFactoryModifier(agent.getByteCodeInstrumentor(), agent, interceptor);
        
        ClassLoader loader = getClass().getClassLoader();
        ClassTransformHelper.transformClass(loader, "org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory", modifier);
        
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");
        
        verify(retransformer).retransform(Maru.class, beanModifier);
        verify(retransformer).retransform(Morae.class, beanModifier);
        verify(retransformer).retransform(Outer.class, beanModifier);
        verify(retransformer).retransform(Inner.class, beanModifier);
        verifyNoMoreInteractions(retransformer);

        context.getBean("mozzi");
        context.getBean("mozzi");
        
        verify(retransformer).retransform(Mozzi.class, beanModifier);
        verifyNoMoreInteractions(retransformer);
    }
}
