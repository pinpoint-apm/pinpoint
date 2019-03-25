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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Taejin Koo
 */
public class StreamChannelTest {

    @Test
    public void stateChangeTest() throws Exception {
        ClientStreamChannel sc = new ClientStreamChannel(null, 1, new StreamChannelRepository(), new RecordedStreamChannelMessageListener(0));

        sc.init();
        Assert.assertEquals(StreamChannelStateCode.OPEN, sc.getCurrentState());

        boolean isChanged = sc.changeStateConnected();
        Assert.assertFalse(isChanged);
        Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, sc.getCurrentState());
    }

    @Test(expected = StreamException.class)
    public void testName() throws Exception {
        TestStateChangeHandler testStateChangeHandler = new TestStateChangeHandler();

        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.write(Mockito.any())).thenReturn(null);

        ClientStreamChannel sc = new ClientStreamChannel(mockChannel, 1, new StreamChannelRepository(), new RecordedStreamChannelMessageListener(0));
        sc.addStateChangeEventHandler(testStateChangeHandler);

        sc.init();
        Assert.assertEquals(StreamChannelStateCode.OPEN, testStateChangeHandler.getLatestEventPerformedStateCode());

        sc.connect(new byte[0], 3000);
    }

    class TestStateChangeHandler implements StreamChannelStateChangeEventHandler {

        private int totalEventPerformedCount;
        private StreamChannelStateCode latestEventPerformedStateCode;


        @Override
        public void eventPerformed(StreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
            this.latestEventPerformedStateCode = updatedStateCode;
            this.totalEventPerformedCount++;
        }

        @Override
        public void exceptionCaught(StreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
        }

        public StreamChannelStateCode getLatestEventPerformedStateCode() {
            return latestEventPerformedStateCode;
        }

        public int getTotalEventPerformedCount() {
            return totalEventPerformedCount;
        }
    }

}
