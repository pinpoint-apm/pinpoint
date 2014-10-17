package com.nhn.pinpoint.rpc.client;

import java.net.SocketAddress;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.nhn.pinpoint.rpc.stream.ClientStreamChannelMessageListener;

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

    ClientStreamChannelContext createStreamChannel(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener);

    void sendPing();

    boolean isConnected();

	boolean isSupportServerMode();
	
	void turnOnServerMode();
	
}
