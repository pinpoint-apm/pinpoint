package com.nhn.pinpoint.common.io.rpc.client;

import com.nhn.pinpoint.common.io.rpc.client.StreamChannel;

/**
 *
 */
public interface StreamChannelMessageListener {
    void handleStream(StreamChannel streamChannel, byte[] bytes);
}
