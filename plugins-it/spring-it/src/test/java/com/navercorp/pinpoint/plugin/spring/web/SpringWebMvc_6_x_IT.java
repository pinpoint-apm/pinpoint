package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import java.lang.reflect.Method;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-webmvc:[6.0.0.RELEASE,)", "org.springframework:spring-test", "jakarta.servlet:jakarta.servlet-api:6.0.0"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class SpringWebMvc_6_x_IT {
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
        DispatcherServlet.class.getMethod("init", ServletConfig.class).invoke(servlet, config);

        DispatcherServlet.class.getMethod("service", ServletRequest.class, ServletResponse.class).invoke(servlet, req, res);

        Method method = FrameworkServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(Expectations.event(SPRING_MVC, method));
        verifier.verifyTraceCount(0);
    }
}
