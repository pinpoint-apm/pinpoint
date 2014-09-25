package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;

import java.net.SocketAddress;

/**
 * @author emeroad
 * @author netspider
 */
public interface SocketHandler {

    void setConnectSocketAddress(SocketAddress address);

    void open();

    void initReconnect();

    void setPinpointSocket(PinpointSocket pinpointSocket);

    void sendSync(byte[] bytes);

    Future sendAsync(byte[] bytes);

    void close();

    void send(byte[] bytes);

    Future<ResponseMessage> request(byte[] bytes);

    StreamChannel createStreamChannel();

    void sendPing();

    boolean isConnected();

	void setMessageListener(MessageListener messageListener);
}
