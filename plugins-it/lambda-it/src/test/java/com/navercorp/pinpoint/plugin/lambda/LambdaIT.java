/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.lambda;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.test.pinpoint.Bean1;
import com.navercorp.test.pinpoint.Bean2;
import com.navercorp.test.pinpoint.Model;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Predicate;

@JvmVersion(8)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-lambda-test.config")
@Dependency({"org.springframework:spring-context:[4.2.0.RELEASE]"})
public class LambdaIT {

    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("lambda-test.xml");

        Bean1 bean1 = context.getBean(Bean1.class);
        Bean2 bean2 = context.getBean(Bean2.class);
        bean1.test(bean2);
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Bean1.class.getMethod("test", Bean2.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Bean2.class.getMethod("test", Predicate.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Model.class.getMethod("getAge")));
        
        verifier.verifyTraceCount(0);
    }

    public static void main(String args[]) throws Exception {
        new LambdaIT().test();
    }
    
}
