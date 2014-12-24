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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navercorp.pinpoint.test.fork.ForkRunner;
import com.navercorp.pinpoint.test.fork.PinpointConfig;

@RunWith(ForkRunner.class)
@PinpointConfig("pinpoint-spring-bean-test.config")
public class AbstractAutowireCapableBeanFactoryModifierTest {

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
