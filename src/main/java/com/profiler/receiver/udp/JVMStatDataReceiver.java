package com.profiler.receiver.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.data.read.ReadJVMData;

public class JVMStatDataReceiver extends AbstractUDPReceiver {
	public JVMStatDataReceiver(int dataBufferSize) {
		super(dataBufferSize);
		threadName="JVM Stat Data";
	}
	protected void initializeSocket() throws Exception {
		udpSocket=new DatagramSocket(TomcatProfilerReceiverConfig.JVM_DATA_LISTEN_PORT);
	}
	protected void execute(DatagramPacket packet) throws Exception {
		executor.execute(new ReadJVMData(packet));
	}
}
