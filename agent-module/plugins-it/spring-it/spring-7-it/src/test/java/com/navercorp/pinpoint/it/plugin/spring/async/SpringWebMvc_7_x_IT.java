package com.navercorp.pinpoint.it.plugin.spring.async;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.lang.reflect.Method;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-webmvc:[7.0.0,7.max]", "org.springframework:spring-test", "jakarta.servlet:jakarta.servlet-api:6.0.0"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
//@Disabled
public class SpringWebMvc_7_x_IT {
    private static final String SPRING_MVC = "SPRING_MVC";

    // the equivalent of the old spring-web-test.xml (<mvc:annotation-driven/>)
    @Configuration
    @EnableWebMvc
    static class WebConfig {
    }

    @Test
    public void testRequest() throws Exception {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);

        DispatcherServlet servlet = new DispatcherServlet(context);
        servlet.init(new MockServletConfig());

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.setMethod("GET");
        req.setRequestURI("/");
        req.setRemoteAddr("1.2.3.4");
        servlet.service(req, res);

        Method method = FrameworkServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(Expectations.event(SPRING_MVC, method));
        verifier.verifyTraceCount(0);
    }
}
