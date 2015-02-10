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

package com.navercorp.pinpoint.profiler.modifier.spring.beans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navercorp.pinpoint.profiler.ClassFileRetransformer;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.modifier.Modifier;
import com.navercorp.pinpoint.profiler.modifier.spring.beans.AbstractAutowireCapableBeanFactoryModifier;
import com.navercorp.pinpoint.test.ClassTransformHelper;
import com.navercorp.pinpoint.test.MockAgent;

public class AbstractAutowireCapableBeanFactoryModifierTest {

    @Test
    public void test() throws Exception {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");
        ClassFileRetransformer retransformer = mock(ClassFileRetransformer.class);
        Modifier beanModifier = mock(Modifier.class);
        
        AbstractAutowireCapableBeanFactoryModifier modifier = AbstractAutowireCapableBeanFactoryModifier.of(agent.getByteCodeInstrumentor(), agent.getProfilerConfig(), retransformer, beanModifier);

        
        ClassLoader loader = getClass().getClassLoader();
        ClassTransformHelper.transformClass(loader, "org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory", modifier);
        
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");
        
        verify(retransformer).retransform(Maru.class, beanModifier);
        verify(retransformer).retransform(Morae.class, beanModifier);
        verify(retransformer).retransform(Outer.class, beanModifier);
        verify(retransformer).retransform(Inner.class, beanModifier);
        verify(retransformer).retransform(ProxyTarget.class, beanModifier);
        verifyNoMoreInteractions(retransformer);

        context.getBean("mozzi");
        context.getBean("mozzi");
        
        verify(retransformer).retransform(Mozzi.class, beanModifier);
        verifyNoMoreInteractions(retransformer);
        
        assertFalse(ProxyTarget.class.equals(context.getBean("proxyTarget").getClass()));
    }
}
