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

import java.io.IOException;
import java.net.Socket;

import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.rpc.DiscardPipelineFactory;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.springframework.util.SocketUtils;

/**
 * @author emeroad
 */
public class PinpointServerSocketTest {
    
    private static int bindPort;
    
    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }
    
    @Test
    public void testBind() throws Exception {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setPipelineFactory(new DiscardPipelineFactory());
        serverAcceptor.bind("127.0.0.1", bindPort);

        Socket socket = new Socket("127.0.0.1", bindPort);
        socket.getOutputStream().write(new byte[10]);
        socket.getOutputStream().flush();
        socket.close();

        Thread.sleep(1000);
        PinpointRPCTestUtils.close(serverAcceptor);
    }


}
