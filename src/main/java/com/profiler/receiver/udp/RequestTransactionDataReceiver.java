package com.profiler.receiver.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.data.read.ReadRequestTransactionData;

public class RequestTransactionDataReceiver extends AbstractUDPReceiver {
	public RequestTransactionDataReceiver(int dataBufferSize) {
		super(dataBufferSize);
		threadName="Transaction Data";
	}
	protected void initializeSocket() throws Exception {
		udpSocket=new DatagramSocket(TomcatProfilerReceiverConfig.REQUEST_TRANSACTION_DATA_LISTEN_PORT);
	}
	protected void execute(DatagramPacket packet) throws Exception {
		executor.execute(new ReadRequestTransactionData(packet.getData()));
	}
}
