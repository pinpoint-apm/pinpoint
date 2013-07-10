package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public interface StreamChannelMessageListener {
    void handleStream(StreamChannel streamChannel, byte[] bytes);
}
