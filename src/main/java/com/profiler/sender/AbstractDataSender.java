package com.profiler.sender;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public abstract class AbstractDataSender {
	public void send() {
		try {
			byte[] sendData = getSendData();
			int sendDataLength = sendData.length;

			// System.out.println("sendDataLength="+sendDataLength);

			InetSocketAddress address = getAddress();
			DatagramPacket packet = new DatagramPacket(sendData,
					sendDataLength, address);

			DatagramSocket udpSocket = new DatagramSocket();
			// System.out.println("sendBufferSize="+udpSocket.getSendBufferSize());
			udpSocket.send(packet);
			// if(this instanceof RequestDataSender || this instanceof
			// RequestTransactionDataSender ) {
			// System.out.println(this.getClass().getName()+" Send bufferSize="+udpSocket.getSendBufferSize()+" dataLength="+sendDataLength);
			// }
			udpSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract byte[] getSendData() throws Exception;

	protected abstract InetSocketAddress getAddress() throws Exception;

	public void log(String message) {
		// System.out.println("[AbstractDataSenderThread] "+message);
	}
}
