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
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * WebFlux server side on Spring Framework 6: an annotated controller behind {@code DispatcherHandler}
 * served by the reactor-netty {@code HttpServer}, called with a plain {@code HttpURLConnection}
 * (no client plugin imported, so the server trace is the only trace).
 *
 * <p>Pins the server-side instrumentation that has no other automated coverage on this Spring line:
 * the reactor-netty root span, the SPRING_WEBFLUX span events of {@code DispatcherHandler} and
 * {@code InvocableHandlerMethod}, and the URI template taken from the best-matching {@code PathPattern}.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-webflux:[6.0.0,6.max]",
        // same version as spring-webflux: @EnableWebFlux / WebHttpHandlerBuilder need spring-context
        "org.springframework:spring-context",
        "io.projectreactor.netty:reactor-netty-http:1.2.18",
        PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-webflux-plugin",
        "com.navercorp.pinpoint:pinpoint-reactor-netty-plugin",
        "com.navercorp.pinpoint:pinpoint-netty-plugin",
        "com.navercorp.pinpoint:pinpoint-reactor-plugin"})
@PinpointConfig("pinpoint-webflux-server.config")
public class SpringWebFluxServer_6_x_IT {

    // reactor-netty server root: ServletRequestListener opens the trace with the shared "Servlet Process" api;
    // HttpServerHandle.onStateChange itself is the first REACTOR_NETTY_INTERNAL event under it.
    private static final String SERVLET_PROCESS = "Servlet Process";
    private static final String ON_STATE_CHANGE = "reactor.netty.http.server.HttpServer$HttpServerHandle"
            + ".onStateChange(reactor.netty.Connection, reactor.netty.ConnectionObserver$State)";

    private static final String DISPATCHER_HANDLER = "org.springframework.web.reactive.DispatcherHandler";
    private static final String HANDLE = DISPATCHER_HANDLER + ".handle(org.springframework.web.server.ServerWebExchange)";
    private static final String HANDLE_REQUEST_WITH = DISPATCHER_HANDLER + ".handleRequestWith(org.springframework.web.server.ServerWebExchange, java.lang.Object)";
    private static final String INVOKE = "org.springframework.web.reactive.result.method.InvocableHandlerMethod"
            + ".invoke(org.springframework.web.server.ServerWebExchange, org.springframework.web.reactive.BindingContext, java.lang.Object[])";

    private static AnnotationConfigApplicationContext context;
    private static DisposableServer server;

    @BeforeAll
    public static void beforeClass() {
        context = new AnnotationConfigApplicationContext(WebFluxTestConfig.class);
        HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
        server = HttpServer.create()
                .host("127.0.0.1")
                .port(0)
                .handle(new ReactorHttpHandlerAdapter(httpHandler))
                .bindNow();
    }

    @AfterAll
    public static void afterClass() {
        if (server != null) {
            server.disposeNow();
        }
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void annotatedHandler_recordsDispatcherHandlerEvents_andTheUriTemplate() throws Exception {
        String body = get("/users/42");
        Assertions.assertEquals("user-42", body);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // The controller runs on a netty event loop: wait for the handler event before inspecting the cache.
        verifier.awaitTrace(event("SPRING_WEBFLUX", INVOKE), 20, 5000);
        // The root span is flushed after the response completes, possibly after the client already returned.
        verifier.awaitTrace(root("REACTOR_NETTY", SERVLET_PROCESS, "/users/42", "127.0.0.1:" + server.port(), null), 20, 5000);
        verifier.printCache();

        // The URI template comes from the best-matching PathPattern, not from the raw request path.
        verifier.verifyUriTemplate("/users/{id}");
        // remoteAddr is the client's ephemeral host:port, so it is not pinned.
        verifier.verifyDiscreteTrace(
                root("REACTOR_NETTY", SERVLET_PROCESS, "/users/42", "127.0.0.1:" + server.port(), null,
                        annotation("http.status.code", 200)),
                event("REACTOR_NETTY_INTERNAL", ON_STATE_CHANGE),
                event("SPRING_WEBFLUX", HANDLE),
                event("SPRING_WEBFLUX", HANDLE_REQUEST_WITH),
                event("SPRING_WEBFLUX", INVOKE));
    }

    private String get(String path) throws Exception {
        URL url = new URL("http://127.0.0.1:" + server.port() + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        try (InputStream in = connection.getInputStream()) {
            Assertions.assertEquals(200, connection.getResponseCode());
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    @Configuration
    @EnableWebFlux
    static class WebFluxTestConfig {
        @Bean
        public UserController userController() {
            return new UserController();
        }
    }

    @RestController
    static class UserController {
        @GetMapping("/users/{id}")
        public Mono<String> user(@PathVariable("id") String id) {
            return Mono.just("user-" + id);
        }
    }
}
