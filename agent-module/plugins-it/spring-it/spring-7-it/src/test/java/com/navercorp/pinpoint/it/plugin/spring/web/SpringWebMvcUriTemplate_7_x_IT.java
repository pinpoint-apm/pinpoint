/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import java.lang.reflect.Method;
import java.util.Collections;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * Spring MVC URI-template recording on Spring Framework 7, driven straight through
 * {@code DispatcherServlet.service} (which runs {@code FrameworkServlet.doGet/processRequest} and
 * the handler mappings the plugin instruments) with a mock request.
 *
 * <p>Three recording paths, all off unless {@code profiler.uri.stat.spring.webmvc.enable=true}:
 * the annotated-controller pattern from {@code AbstractHandlerMethodMapping.lookupHandlerMethod},
 * the legacy {@code Controller} pattern from {@code AbstractUrlHandlerMapping.exposePathWithinMapping},
 * and the application-supplied {@code pinpoint.metric.uri-template} attribute read by
 * {@code FrameworkServlet.processRequest} when {@code useuserinput=true}.
 *
 * <p>Only the {@code FrameworkServlet.doGet} event is pinned: the {@code InvocableHandlerMethod.invokeForRequest}
 * interceptor records a span event only on the Servlet async path (it needs the {@code AsyncContext}
 * request attribute), which a synchronous mock request never has.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-webmvc:[7.0.0,7.max]", "org.springframework:spring-test", "jakarta.servlet:jakarta.servlet-api:6.1.0"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
@PinpointConfig("pinpoint-webmvc-uristat.config")
public class SpringWebMvcUriTemplate_7_x_IT {

    private static final String SPRING_MVC = "SPRING_MVC";
    static final String USER_INPUT_ATTRIBUTE = "pinpoint.metric.uri-template";

    private static AnnotationConfigWebApplicationContext context;
    private static DispatcherServlet servlet;

    @BeforeAll
    public static void beforeClass() throws Exception {
        context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);
        servlet = new DispatcherServlet(context);
        servlet.init(new MockServletConfig());
    }

    @AfterAll
    public static void afterClass() {
        if (servlet != null) {
            servlet.destroy();
        }
    }

    @Test
    public void annotatedController_recordsTheBestMatchingPattern() throws Exception {
        MockHttpServletResponse response = get("/users/42");
        Assertions.assertEquals("user-42", response.getContentAsString());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // AbstractHandlerMethodMapping.lookupHandlerMethod -> HandlerMapping.bestMatchingPattern
        verifier.verifyUriTemplate("/users/{id}");
        verifier.verifyDiscreteTrace(event(SPRING_MVC, doGet()));
    }

    @Test
    public void legacyController_recordsTheUrlHandlerMappingPattern() throws Exception {
        MockHttpServletResponse response = get("/legacy/items");
        Assertions.assertEquals("legacy", response.getContentAsString());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // AbstractUrlHandlerMapping.exposePathWithinMapping(bestMatchingPattern, pathWithinMapping, request)
        verifier.verifyUriTemplate("/legacy/*");
        verifier.verifyDiscreteTrace(event(SPRING_MVC, doGet()));
    }

    @Test
    public void userInputAttribute_overridesTheRecordedTemplate() throws Exception {
        MockHttpServletResponse response = get("/orders/7");
        Assertions.assertEquals("order-7", response.getContentAsString());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // FrameworkServlet.processRequest (useuserinput=true) force-records the attribute the controller set,
        // replacing the "/orders/{id}" pattern recorded earlier by lookupHandlerMethod.
        verifier.verifyUriTemplate("/custom/orders");
        verifier.verifyDiscreteTrace(event(SPRING_MVC, doGet()));
    }

    private static MockHttpServletResponse get(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        request.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        Assertions.assertEquals(200, response.getStatus(), "unexpected status for " + uri);
        return response;
    }

    private static Method doGet() throws NoSuchMethodException {
        return FrameworkServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig {
        @Bean
        public UserController userController() {
            return new UserController();
        }

        @Bean
        public SimpleUrlHandlerMapping legacyHandlerMapping() {
            SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
            mapping.setOrder(0);
            mapping.setUrlMap(Collections.singletonMap("/legacy/*", new LegacyController()));
            return mapping;
        }
    }

    @RestController
    static class UserController {
        @GetMapping("/users/{id}")
        public String user(@PathVariable("id") String id) {
            return "user-" + id;
        }

        @GetMapping("/orders/{id}")
        public String order(@PathVariable("id") String id, HttpServletRequest request) {
            request.setAttribute(USER_INPUT_ATTRIBUTE, "/custom/orders");
            return "order-" + id;
        }
    }

    static class LegacyController implements Controller {
        @Override
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.getWriter().write("legacy");
            return null;
        }
    }
}
