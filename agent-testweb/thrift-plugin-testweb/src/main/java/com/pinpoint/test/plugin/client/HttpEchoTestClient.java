/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin.client;

import com.pinpoint.test.plugin.TestEnvironment;
import com.pinpoint.test.plugin.dto.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;


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
    public void close() {
        httpClient.close();
    }

    public static HttpEchoTestClient create(TestEnvironment environment) throws TTransportException {
        return new HttpEchoTestClient(environment, new THttpClient(environment.getHttpUrl()));
    }
}
