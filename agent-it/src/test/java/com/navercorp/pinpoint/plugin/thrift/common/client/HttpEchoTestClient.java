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

package com.navercorp.pinpoint.plugin.thrift.common.client;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import java.lang.reflect.Method;
import java.net.URL;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class HttpEchoTestClient implements EchoTestClient {

    private final TestEnvironment environment;
    private final THttpClient httpClient;

    private HttpEchoTestClient(TestEnvironment environment, THttpClient httpClient) throws TTransportException {
        this.environment = environment;
        this.httpClient = httpClient;
    }

    @Override
    public String echo(String message) throws TException {
        TProtocol protocol = environment.getProtocolFactory().getProtocol(httpClient);
        EchoService.Client client = new EchoService.Client(protocol);
        return client.echo(message);
    }

    @Override
    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception {
        // ignore jdk http url connector traces
        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR");

        // refer to TServiceClientSendBaseInterceptor.getRemoteAddressForTHttpClient(...)
        URL url = new URL(environment.getHttpUrl());
        String hostAndPort = HostAndPort.toHostAndPortString(url.getHost(), url.getPort());

        // SpanEvent - TServiceClient.sendBase
        Method sendBaseMethod = TServiceClient.class.getDeclaredMethod("sendBase", String.class, TBase.class);
        ExpectedAnnotation thriftUrl = Expectations.annotation("thrift.url",
                hostAndPort + "/com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo");
        ExpectedAnnotation thriftArgs = Expectations.annotation("thrift.args",
                "echo_args(message:" + expectedMessage + ")");
        ExpectedTrace tServiceClientSendBaseTrace = event(
                "THRIFT_CLIENT_INTERNAL",
                sendBaseMethod,
                thriftUrl, thriftArgs);

        // SpanEvent - HttpURLConnection.connect (ignore)

        // SpanEvent - TServiceClient.receiveBase
        Method receiveBaseMethod = TServiceClient.class.getDeclaredMethod("receiveBase", TBase.class, String.class);
        ExpectedAnnotation thriftResult = Expectations.annotation(
                "thrift.result", "echo_result(success:" + expectedMessage + ")");
        ExpectedTrace tServiceClientReceiveBaseTrace = event(
                "THRIFT_CLIENT_INTERNAL",
                receiveBaseMethod,
                thriftResult);

        verifier.verifyDiscreteTrace(
                tServiceClientSendBaseTrace,
                tServiceClientReceiveBaseTrace);
    }

    @Override
    public void close() {
        httpClient.close();
    }

    public static HttpEchoTestClient create(TestEnvironment environment) throws TTransportException {
        return new HttpEchoTestClient(environment, new THttpClient(environment.getHttpUrl()));
    }
}
