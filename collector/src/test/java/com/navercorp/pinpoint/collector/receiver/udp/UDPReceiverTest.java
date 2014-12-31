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

package com.navercorp.pinpoint.collector.receiver.udp;

import java.io.IOException;
import java.net.*;

import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.udp.BaseUDPReceiver;

import junit.framework.Assert;

import org.apache.thrift.TBase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class UDPReceiverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @Ignore
	public void startStop() {
		try {
			DataReceiver receiver = new BaseUDPReceiver("test", new DispatchHandler() {
                @Override
                public void dispatchSendMessage(TBase<?, ?> tBase, byte[] packet, int offset, int length) {
                }

				@Override
				public TBase dispatchRequestMessage(TBase<?, ?> tBase, byte[] packet, int offset, int length) {
					// TODO Auto-generated method stub
					return null;
				}
				
            }, "127.0.0.1", 10999, 1024, 1, 10);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

    @Test
    public void hostNullCheck() {
        InetSocketAddress address = new InetSocketAddress((InetAddress) null, 90);
        logger.debug(address.toString());
    }

    @Test
    public void socketBufferSize() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        int receiveBufferSize = datagramSocket.getReceiveBufferSize();
        logger.debug("{}", receiveBufferSize);

        datagramSocket.setReceiveBufferSize(64*1024*10);
        logger.debug("{}", datagramSocket.getReceiveBufferSize());

        datagramSocket.close();
    }

    @Test
    public void sendSocketBufferSize() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, 0);

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.connect(new InetSocketAddress("127.0.0.1", 9995));

        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }
}
