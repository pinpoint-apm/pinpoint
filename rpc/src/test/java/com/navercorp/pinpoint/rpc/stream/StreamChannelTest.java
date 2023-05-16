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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.RecordedStreamChannelMessageListener;
import org.jboss.netty.channel.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Taejin Koo
 */
public class StreamChannelTest {

    @Test
    public void stateChangeTest() throws Exception {
        Channel mockChannel = Mockito.mock(Channel.class);
        NettyClientStreamChannel sc = new NettyClientStreamChannel(mockChannel, 1, new StreamChannelRepository(), new RecordedStreamChannelMessageListener(0));

        sc.init();
        Assertions.assertEquals(StreamChannelStateCode.OPEN, sc.getCurrentState());

        boolean isChanged = sc.changeStateConnected();
        Assertions.assertFalse(isChanged);
        Assertions.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, sc.getCurrentState());
    }

    @Test
    public void testName() throws Exception {
        Assertions.assertThrows(StreamException.class, () -> {
            Channel mockChannel = Mockito.mock(Channel.class);
            Mockito.when(mockChannel.write(Mockito.any())).thenReturn(null);

            RecordedStreamChannelMessageListener recordEventHandler = new RecordedStreamChannelMessageListener(0);

            NettyClientStreamChannel sc = new NettyClientStreamChannel(mockChannel, 1, new StreamChannelRepository(), recordEventHandler);

            sc.init();
            Assertions.assertEquals(StreamChannelStateCode.OPEN, recordEventHandler.getCurrentState());

            sc.connectAndAwait(new byte[0], 3000);
        });
    }

}
