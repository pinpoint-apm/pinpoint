package com.nhn.pinpoint.rpc.client;

public interface PinpointSocketReconnectEventListener {
	
	// 현재는 Reconnect를 제외한 별다른 Event가 없음 
	// 이후에 별다른 Event가 있을 경우 Event와 함께 넘겨주면 좋을듯함
	void reconnectPerformed(PinpointSocket socket);
	
}
