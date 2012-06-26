package com.profiler.receiver.tcp;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.data.store.hbase.create2.AgentTableCreator;
import com.profiler.dto.AgentInfoDTO;
public class TCPReceiver extends Thread{
	private static final Logger logger = Logger.getLogger("com.profiler.receiver.tcp.TCPReceiver");
	
	ServerSocket serverSocket=null;
	
	public TCPReceiver() {}
	public void run() {
		try {
			serverSocket=new ServerSocket(TomcatProfilerReceiverConfig.SERVER_TCP_LISTEN_PORT, 100);
			System.out.println("Waiting for Agent data");
			while(true) {
				Socket socket=serverSocket.accept();
				InputStream stream=socket.getInputStream();
				ObjectInputStream objStream=new ObjectInputStream(stream);
				Object receivedObj=objStream.readObject();
				if(receivedObj instanceof AgentInfoDTO) {
					AgentTableCreator creator=new AgentTableCreator();
					creator.setAgentTable((AgentInfoDTO)receivedObj);
				}
				logger.debug(receivedObj.toString());
//				System.out.println(new String(readData));
			}
//		} catch(InterruptedException ie) {
			
		} catch(Exception e) {
			
			e.printStackTrace();
		}
	}
	
}
