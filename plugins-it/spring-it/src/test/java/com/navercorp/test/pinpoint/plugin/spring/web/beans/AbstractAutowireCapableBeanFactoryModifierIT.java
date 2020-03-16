/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.test.pinpoint.plugin.spring.web.beans;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-context:[3.0.7.RELEASE],[3.1.4.RELEASE],[3.2.14.RELEASE],[4.0.9.RELEASE],[4.1.7.RELEASE],[4.2.0.RELEASE,4.max]", "cglib:cglib-nodep:3.1"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class AbstractAutowireCapableBeanFactoryModifierIT {

    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");
        
        Maru maru = context.getBean(Maru.class);
        maru.publicMethod();
        maru.protectedMethod();
        
        Morae morae = (Morae)context.getBean("morae");
        morae.doSomething();
        
        Morae duplicatedMorae = (Morae)context.getBean("duplicatedMorae");
        duplicatedMorae.doSomething();

        Mozzi mozzi = (Mozzi)context.getBean("mozzi");
        mozzi.doSomething();
        
        Excluded excluded = (Excluded)context.getBean("excluded");
        excluded.doSomething();
        
        Outer outer = (Outer)context.getBean("outer");
        outer.doSomething();
        
        Inner inner = outer.getInner();
        inner.doSomething();
        
        ProxyTarget proxyTarget = context.getBean(ProxyTarget.class);
        proxyTarget.doSomething();
        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("setInner", Inner.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Maru.class.getMethod("publicMethod")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Morae.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Morae.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Mozzi.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Outer.class.getMethod("getInner")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Inner.class.getMethod("doSomething")));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", ProxyTarget.class.getMethod("doSomething")));
        
        verifier.verifyTraceCount(0);
    }
}