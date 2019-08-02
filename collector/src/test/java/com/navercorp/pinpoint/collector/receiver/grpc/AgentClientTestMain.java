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

/**
 * @author jaehong.kim
 */
public class AgentClientTestMain {
    private static final int MAX = Integer.MAX_VALUE;

    public static void main(String[] args) throws Exception {
        AgentClientMock clientMock = new AgentClientMock("localhost", 9997, true);
        clientMock.info(999999);

//        clientMock.apiMetaData(1);
//        clientMock.sqlMetaData(1);
//        clientMock.stringMetaData(1);

//        clientMock.pingPoing();

        TimeUnit.SECONDS.sleep(60 * 60);
        clientMock.stop();
    }
}
