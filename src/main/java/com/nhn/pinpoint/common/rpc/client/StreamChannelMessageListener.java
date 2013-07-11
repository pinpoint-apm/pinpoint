package com.nhn.pinpoint.common.rpc.client;

/**
 *
 */
public interface StreamChannelMessageListener {
    void handleStream(StreamChannel streamChannel, byte[] bytes);
}
