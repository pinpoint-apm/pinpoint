/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.DiscardServerHandler;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import org.junit.jupiter.api.Test;

import java.net.Socket;

/**
 * @author emeroad
 */
public class PinpointServerSocketTest {

    @Test
    public void testBind() throws Exception {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new DiscardServerHandler());
        int bindPort = testPinpointServerAcceptor.bind();

        Socket socket = new Socket("127.0.0.1", bindPort);
        socket.getOutputStream().write(new byte[0]);
        socket.getOutputStream().flush();
        socket.close();

        Thread.sleep(1000);
        testPinpointServerAcceptor.close();
    }


}
