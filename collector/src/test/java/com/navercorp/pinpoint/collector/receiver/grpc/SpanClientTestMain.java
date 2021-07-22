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
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpanClientTestMain {
    public static final Logger logger = Logger.getLogger(SpanClientTestMain.class.getName());
    static {
        logger.setLevel(Level.FINE);
    }
    private static final int MAX = Integer.MAX_VALUE;

    public static void main(String[] args) throws Exception {
        logger.info("START");

        SpanClientMock clientMock = new SpanClientMock("localhost", 9993);
        TimeUnit.SECONDS.sleep(3);

        long startTime = System.currentTimeMillis();
        clientMock.span(MAX);

        TimeUnit.SECONDS.sleep(999999999);

//        clientMock.stop();
    }
}
