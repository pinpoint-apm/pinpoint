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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.rpc.DiscardServerHandler;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

/**
 * @author Taejin Koo
 */
public class PipelineFactoryTest {

    private static int bindPort;

    private static final char START_KEY = '!';

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    @Test
    public void testBind() throws Exception {
        PinpointServerAcceptor serverAcceptor = null;
        Socket socket = null;
        try {
            serverAcceptor = new PinpointServerAcceptor(ChannelFilter.BYPASS, new TestPipelineFactory());
            final DiscardServerHandler discardServerHandler = new DiscardServerHandler();
            serverAcceptor.setMessageHandler(discardServerHandler);
            serverAcceptor.bind("127.0.0.1", bindPort);

            socket = new Socket("127.0.0.1", bindPort);
            socket.getOutputStream().write((START_KEY + "Test").getBytes());
            socket.getOutputStream().flush();

            assertMessageReceivedCount(1, discardServerHandler);

            socket.getOutputStream().write(('@' + "Test").getBytes());
            socket.getOutputStream().flush();

            try {
                assertMessageReceivedCount(2, discardServerHandler);
                Assert.fail();
            } catch (ConditionTimeoutException e) {
                // ignore
            }

        } finally {
            IOUtils.closeQuietly(socket);

            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    private void assertMessageReceivedCount(int expectedCount, final DiscardServerHandler discardServerHandler) {
        Awaitility.await()
                .ignoreExceptions()
                .until(new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return discardServerHandler.getMessageReceivedCount();
                    }
                }, is(expectedCount));
    }

    private static class TestPipelineFactory implements PipelineFactory {

        @Override
        public ChannelPipeline newPipeline() {
            ChannelPipeline pipeline = Channels.pipeline();

            pipeline.addLast("decoder", new FrameDecoder() {
                @Override
                protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
                    byte b = buffer.readByte();

                    if (((char) b == START_KEY)) {
                        int availableSize = buffer.readableBytes();

                        byte[] data = new byte[availableSize];
                        buffer.readBytes(data);

                        return ChannelBuffers.wrappedBuffer(data);
                    } else {
                        //discard
                        int availableSize = buffer.readableBytes();

                        byte[] data = new byte[availableSize];
                        buffer.readBytes(data);

                        return null;
                    }
                }
            });

            return pipeline;
        }
    }

}
