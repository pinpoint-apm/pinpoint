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
package com.navercorp.pinpoint.plugin.spring.web;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Jongho Moon
 *
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-webmvc:[3.0.7.RELEASE],[3.1.4.RELEASE],[3.2.14.RELEASE],[4.0.9.RELEASE],[4.1.7.RELEASE],[4.2.0.RELEASE,4.max]",
        "org.springframework:spring-test", "javax.servlet:javax.servlet-api:3.0.1"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class SpringWebMvc_3_x_to_4_x_IT {
    private static final String SPRING_MVC = "SPRING_MVC";

    @Test
    public void testRequest() throws Exception {
        MockServletConfig config = new MockServletConfig();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        config.addInitParameter("contextConfigLocation", "classpath:spring-web-test.xml");
        req.setMethod("GET");
        req.setRequestURI("/");
        req.setRemoteAddr("1.2.3.4");
        
        DispatcherServlet servlet = new DispatcherServlet();
        servlet.init(config);
        
        servlet.service(req, res);
        
        Method method = FrameworkServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(Expectations.event(SPRING_MVC, method));
        verifier.verifyTraceCount(0);
    }
}
