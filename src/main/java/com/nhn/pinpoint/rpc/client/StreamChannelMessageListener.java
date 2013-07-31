package com.nhn.pinpoint.rpc.client;

/**
 *
 */
public interface StreamChannelMessageListener {
    void handleStreamResponse(StreamChannel streamChannel, byte[] bytes);

    void handleClose(StreamChannel streamChannel, byte[] bytes);
}
