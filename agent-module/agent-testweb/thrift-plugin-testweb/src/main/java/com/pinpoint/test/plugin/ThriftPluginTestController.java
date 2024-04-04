/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import com.pinpoint.test.plugin.client.AsyncEchoTestClient;
import com.pinpoint.test.plugin.client.EchoTestClient;
import com.pinpoint.test.plugin.client.HttpEchoTestClient;
import com.pinpoint.test.plugin.client.SyncEchoTestClient;
import com.pinpoint.test.plugin.server.AsyncEchoTestServer;
import com.pinpoint.test.plugin.server.EchoTestServer;
import com.pinpoint.test.plugin.server.HttpEchoTestServer;
import com.pinpoint.test.plugin.server.SyncEchoTestServer;
import com.pinpoint.test.plugin.server.ThriftEchoTestServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ThriftPluginTestController {
    public static final String MESSAGE = "MESSAGE";
    private TestEnvironment testEnvironment = new TestEnvironment();

    @RequestMapping(value = "/server/sync")
    public String serverSync() throws Exception {
        final SyncEchoTestServer server = SyncEchoTestServer.SyncEchoTestServerFactory.simpleServer(testEnvironment);
        invoke(server, false);
        return "OK";
    }

    @RequestMapping(value = "/server/async/thread-selector")
    public String threadSelectorAsyncServer() throws Exception {
        final AsyncEchoTestServer server = AsyncEchoTestServer.AsyncEchoTestServerFactory.threadedSelectorServer(testEnvironment);
        invoke(server);
        return "OK";
    }

    @RequestMapping(value = "/server/async/half")
    public String halfAsyncServer() throws Exception {
        final AsyncEchoTestServer server = AsyncEchoTestServer.AsyncEchoTestServerFactory.halfSyncHalfAsyncServer(testEnvironment);
        invoke(server);
        return "OK";
    }

    @RequestMapping(value = "/server/async/non-blocking")
    public String nonblockingAsyncServer() throws Exception {
        final AsyncEchoTestServer server = AsyncEchoTestServer.AsyncEchoTestServerFactory.nonblockingServer(testEnvironment);
        invoke(server);
        return "OK";
    }

    @RequestMapping(value = "/server/http")
    public String httpServer() throws Exception {
        final HttpEchoTestServer server = HttpEchoTestServer.createServer(testEnvironment);
        invoke(server);

        return "OK";
    }

    private void invoke(EchoTestServer server) throws Exception {
        invoke(server, true);
    }

    private void invoke(EchoTestServer server, boolean async) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.start(executor);

        if (server instanceof ThriftEchoTestServer) {
            ThriftEchoTestServer thriftEchoTestServer = (ThriftEchoTestServer) server;
            final SyncEchoTestClient client = thriftEchoTestServer.getSynchronousClient();
            echo(client);

            if (async) {
                final AsyncEchoTestClient asyncEchoTestClient = thriftEchoTestServer.getAsynchronousClient();
                echo(asyncEchoTestClient);
            }
        } else if (server instanceof HttpEchoTestServer) {
            HttpEchoTestServer httpEchoTestServer = (HttpEchoTestServer) server;
            HttpEchoTestClient client = httpEchoTestServer.getHttpClient();
            echo(client);
        }

        server.stop();
        executor.shutdown();
        Thread.sleep(500L);
    }

    @RequestMapping(value = "/client/sync")
    public String clientSync() throws Exception {
        // When
        final SyncEchoTestServer server = SyncEchoTestServer.SyncEchoTestServerFactory.simpleServer(testEnvironment);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.start(executor);

        final SyncEchoTestClient client = server.getSynchronousClient();
        echo(client);
        server.stop();
        executor.shutdown();
        return "OK";
    }

    @RequestMapping(value = "/client/async")
    public String clientAsync() throws Exception {
        // When
        final SyncEchoTestServer server = SyncEchoTestServer.SyncEchoTestServerFactory.simpleServer(testEnvironment);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.start(executor);

        final AsyncEchoTestClient client = server.getAsynchronousClient();
        echo(client);
        server.stop();
        executor.shutdown();
        return "OK";
    }

    @RequestMapping(value = "/client/http")
    public String clientHttp() throws Exception {
        // When
        final HttpEchoTestServer server = HttpEchoTestServer.createServer(testEnvironment);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.start(executor);

        final HttpEchoTestClient client = server.getHttpClient();
        echo(client);
        server.stop();
        executor.shutdown();
        return "OK";
    }

    private void echo(EchoTestClient client) throws Exception {
        try {
            client.echo(MESSAGE);
        } finally {
            client.close();
            // give a chance to flush out span data
            Thread.sleep(500L);
        }
    }

}
