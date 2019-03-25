/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taejin Koo
 */
public class StreamChannelRepository {

    private final ConcurrentMap<Integer, StreamChannel> channelMap = new ConcurrentHashMap<Integer, StreamChannel>();

    public void registerIfAbsent(StreamChannel streamChannel) throws StreamException {
        Assert.requireNonNull(streamChannel, "streamChannel must not be null");

        int streamId = streamChannel.getStreamId();
        if (channelMap.putIfAbsent(streamId, streamChannel) != null) {
            throw new StreamException(StreamCode.ID_DUPLICATED);
        }
    }

    public StreamChannel unregister(StreamChannel streamChannel) {
        Assert.requireNonNull(streamChannel, "streamChannel must not be null");
        return unregister(streamChannel.getStreamId());
    }

    public StreamChannel unregister(int streamId) {
        return channelMap.remove(streamId);
    }

    public StreamChannel getStreamChannel(int channelId) {
        return this.channelMap.get(channelId);
    }

    public Set<Integer> getStreamIdSet() {
        return channelMap.keySet();
    }

}
