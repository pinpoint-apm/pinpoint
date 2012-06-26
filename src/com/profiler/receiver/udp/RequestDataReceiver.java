package com.profiler.receiver.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.data.read.ReadRequestData;

public class RequestDataReceiver extends AbstractUDPReceiver {
	
	public RequestDataReceiver(int dataBufferSize) {
		super(dataBufferSize);
		threadName="Request Data";
	}
	protected void initializeSocket() throws Exception {
		udpSocket=new DatagramSocket(TomcatProfilerReceiverConfig.REQUEST_DATA_LISTEN_PORT);
	}
	protected void execute(DatagramPacket packet) throws Exception {
		executor.execute(new ReadRequestData(packet.getData()));
	}
}
