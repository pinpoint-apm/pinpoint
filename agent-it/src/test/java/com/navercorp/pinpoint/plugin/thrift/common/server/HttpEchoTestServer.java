/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.common.server;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.HttpEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;
import org.apache.thrift.TBaseProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public abstract class HttpEchoTestServer implements EchoTestServer {

    private final TestEnvironment environment;
    private final Server server;

    protected HttpEchoTestServer(TestEnvironment environment, TProcessor processor) {
        this.environment = environment;
        int port = this.environment.getPort();
        String path = this.environment.getHttpPath();
        TProtocolFactory protocolFactory = this.environment.getProtocolFactory();
        this.server = new Server(port);
        this.server.setHandler(new EchoHttpServerHandler(path, processor, protocolFactory));
    }

    public abstract HttpEchoTestClient getHttpClient() throws TTransportException;

    @Override
    public void start(ExecutorService executorService) {
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start EchoHttpServer.", e);
        }
    }

    @Override
    public void stop() {
        if (server.isStarted()) {
            try {
                server.stop();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to stop EchoHttpServer.", e);
            }
        }
    }

    @Override
    public void verifyTraces(PluginTestVerifier verifier) throws Exception {
        // ignore Jetty traces
        verifier.ignoreServiceType("JETTY", "JETTY_METHOD");

        // RootSpan - Jetty Servlet Process (ignore)

        // SpanEvent - (Jetty) Server.handle (ignore)

        // SpanEvent - TBaseProcessor.process
        // refer to TBaseProcessorProcessInterceptor.finalizeSpanEvent(...)
        Method processMethod = TBaseProcessor.class.getDeclaredMethod("process", TProtocol.class, TProtocol.class);
        ExpectedAnnotation thriftUrl = Expectations.annotation(
                "thrift.url", "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo");
        ExpectedTrace tBaseProcessorProcessTrace = event(
                "THRIFT_SERVER_INTERNAL",
                processMethod,
                thriftUrl);

        verifier.verifyDiscreteTrace(tBaseProcessorProcessTrace);
    }

    public static HttpEchoTestServer createServer(final TestEnvironment environment) {
        TProcessor processor = new EchoService.Processor<EchoService.Iface>(new EchoService.Iface() {
            @Override
            public String echo(String message) throws TException {
                return message;
            }
        });
        return new HttpEchoTestServer(environment, processor) {
            @Override
            public HttpEchoTestClient getHttpClient() throws TTransportException {
                return HttpEchoTestClient.create(environment);
            }
        };
    }

    private class EchoHttpServerHandler extends AbstractHandler {

        private final String path;
        private final TServlet servlet;

        private EchoHttpServerHandler(String path, TProcessor processor, TProtocolFactory protocolFactory) {
            this.path = path;
            this.servlet = new TServlet(processor, protocolFactory) {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    super.service(req, resp);
                }
            };
        }

        @Override
        public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
            if (path.equals(target)) {
                servlet.service(httpServletRequest, httpServletResponse);
            } else {
                httpServletResponse.sendError(HttpStatus.NOT_FOUND_404, target + " is not available.");
            }
        }
    }
}
