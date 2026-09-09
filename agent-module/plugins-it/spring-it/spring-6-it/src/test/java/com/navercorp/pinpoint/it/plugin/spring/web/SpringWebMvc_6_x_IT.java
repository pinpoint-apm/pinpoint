package com.navercorp.pinpoint.it.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import java.lang.reflect.Method;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-webmvc:[6.0.0,6.max]", "org.springframework:spring-test", "jakarta.servlet:jakarta.servlet-api:6.0.0"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class SpringWebMvc_6_x_IT {
    private static final String SPRING_MVC = "SPRING_MVC";

    @Test
    public void testRequest() throws Exception {
        MockServletConfig config = new MockServletConfig();
        config.addInitParameter("contextConfigLocation", "classpath:spring-web-test.xml");

        DispatcherServlet servlet = new DispatcherServlet();
        servlet.init(config);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/");
        req.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse res = new MockHttpServletResponse();

        servlet.service(req, res);

        Method method = FrameworkServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(Expectations.event(SPRING_MVC, method));
        verifier.verifyTraceCount(0);
    }
}
