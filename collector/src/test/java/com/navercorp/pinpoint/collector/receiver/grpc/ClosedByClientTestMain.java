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

public class ClosedByClientTestMain {

    public void running() throws Exception {
        // Start RunningServerTestMain

        AgentClientMock agentClientMock = new AgentClientMock(RunningServerTestMain.IP, RunningServerTestMain.PORT);
        for (int i = 0; i < 100; i++) {
            agentClientMock.info();
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public void close() throws Exception {
        // Start RunningServerTestMain

        AgentClientMock agentClientMock = new AgentClientMock(RunningServerTestMain.IP, RunningServerTestMain.PORT);
        agentClientMock.info();

        TimeUnit.SECONDS.sleep(10);
        agentClientMock.stop();
    }

    public void unexpectedClose() throws Exception {
        AgentClientMock agentClientMock = new AgentClientMock(RunningServerTestMain.IP, RunningServerTestMain.PORT);
        agentClientMock.info();

        TimeUnit.SECONDS.sleep(30);
    }


    public static void main(String[] args) {
        ClosedByClientTestMain main = new ClosedByClientTestMain();
        try {
//            main.close();
            main.running();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
