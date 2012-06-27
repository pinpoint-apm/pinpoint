package com.profiler.sender;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.profiler.config.TomcatProfilerConfig;
import com.profiler.dto.AgentInfoDTO;

public class AgentInfoSender extends Thread{
	boolean isAgentStart;
	public AgentInfoSender(boolean isAgentStart) {
		this.isAgentStart=isAgentStart;
	}
	Socket requestSocket=null;
	public void run() {
		if(isAgentStart) {
			sendAgentStartInfo();
		} else {
			sendAgentStopInfo();
		}
	}
	private void sendAgentStopInfo() {
		try {
			connectToServer();
			ObjectOutputStream stream=new ObjectOutputStream(requestSocket.getOutputStream());
			AgentInfoDTO dto=new AgentInfoDTO();
			dto.setIsDead();
			log(dto.toString());
			stream.writeObject(dto);
			stream.close();
			log("Agent Stopped message is sent");
		} catch(Exception e) {
			log("AgentInfoSender Exception occured:"+e.getMessage());
		} finally {
			closeSocket();
		}
	}
	private void sendAgentStartInfo() {

		while(connectToServer()) {
			try {
			 	Thread.sleep(TomcatProfilerConfig.SERVER_CONNECT_RETRY_GAP);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		try {
			ObjectOutputStream stream=new ObjectOutputStream(requestSocket.getOutputStream());
//			stream.write(("AGENT_HASH="+JVMInfoDTO.hostHashCode).getBytes());
//			stream.write(("AGENT_IP="+JVMInfoDTO.hostIP).getBytes());
//			stream.write(("AGENT_PORT="+JVMInfoDTO.portNumber).getBytes());
			AgentInfoDTO dto=new AgentInfoDTO();
			log(dto.toString());
			stream.writeObject(dto);
			stream.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(requestSocket!=null) {
			closeSocket();
		}
	}
	private void log(String message) {
		System.out.println("*** "+message);
	}
	private boolean connectToServer() {
		try {
			requestSocket=new Socket(TomcatProfilerConfig.SERVER_IP,TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
			log("Connected to server ");
			return false;
		} catch(java.net.ConnectException ce) {
			log("Connect to TomcatProfiler server is failed ***");
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return true;
		}
	}
	private void closeSocket() {
		try {
			requestSocket.close();
			log("TCP RequestSocket is closed");
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
}
