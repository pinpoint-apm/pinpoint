/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import java.util.concurrent.TimeUnit;

public class RunningServerTestMain {
    public static final String IP = "0.0.0.0";
    public static final int PORT = 28081;

    public void running() throws Exception {
        AgentServer server = new AgentServer();
        server.setBeanName("AgentServer");
        server.setBindIp(IP);
        server.setBindPort(PORT);

        server.afterPropertiesSet();

        server.blockUntilShutdown();
        server.destroy();
    }

    public void closedByServer() throws Exception {
        AgentServer server = new AgentServer();
        server.setBeanName("AgentServer");
        server.setBindIp(IP);
        server.setBindPort(PORT);

        server.afterPropertiesSet();

        TimeUnit.SECONDS.sleep(30);

        server.destroy();
    }

    public static void main(String[] args) throws Exception {
        RunningServerTestMain main = new RunningServerTestMain();
        main.running();
    }
}
