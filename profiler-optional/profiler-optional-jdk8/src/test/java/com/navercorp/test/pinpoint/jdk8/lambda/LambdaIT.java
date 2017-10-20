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

package com.navercorp.test.pinpoint.jdk8.lambda;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Predicate;

@RunWith(PinpointPluginTestSuite.class)
@JvmVersion({8})
@PinpointConfig("pinpoint-lambda-test.config")
@Dependency({"org.springframework:spring-context:[4.2.0.RELEASE]"})
public class LambdaIT {

    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("lambda-test.xml");

        Maru maru = context.getBean(Maru.class);
        Morae morae = context.getBean(Morae.class);
        maru.test(morae);
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Maru.class.getMethod("test", Morae.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Morae.class.getMethod("test", Predicate.class)));
        verifier.verifyTrace(Expectations.event("SPRING_BEAN", Mozzi.class.getMethod("getAge")));
        
        verifier.verifyTraceCount(0);
    }

    public static void main(String args[]) throws Exception {
        new LambdaIT().test();
    }
    
}
