package com.profiler.receiver.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractUDPReceiver extends Thread{
	private final int corePoolSize=10;
	private final int maximumPoolSize=1024;
	private final long keepAliveTime=10;
	private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
	protected ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,queue);
	
	protected DatagramSocket udpSocket=null;
	protected String threadName;
	protected int dataBufferSize=1024;
	public AbstractUDPReceiver(int dataBufferSize) {
		this.dataBufferSize=dataBufferSize;
	}
	long rejectedExecutionCount=0;
	public void run() {
		try {
			initializeSocket();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(udpSocket!=null) {
			
			System.out.println("Waiting for "+threadName);
			while(true) {
				byte[] buffer=new byte[dataBufferSize];
				int bufferLength=buffer.length;
				try {
//					System.out.println("ReceiveBufferSize="+udpSocket.getReceiveBufferSize());
					DatagramPacket packet=new DatagramPacket(buffer,bufferLength);
					udpSocket.receive(packet);
					execute(packet);
				} catch (RejectedExecutionException ree) {
					rejectedExecutionCount++;
					if(rejectedExecutionCount>1000) {
						System.out.println("rejectedExecutionCount=1000");
						rejectedExecutionCount=0;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("There is problem with making UDP Socket connection.");
		}
	}
	protected abstract void initializeSocket() throws Exception ;
	protected abstract void execute(DatagramPacket packet) throws Exception ;
}
