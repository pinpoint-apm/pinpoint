package com.profiler.sender;

import java.io.ObjectOutputStream;
import java.net.Socket;

import com.profiler.Logger;
import com.profiler.config.TomcatProfilerConfig;
import com.profiler.dto.AgentInfoDTO;

public class AgentInfoSender extends Thread {

	private static final Logger logger = Logger.getLogger(AgentInfoSender.class);

	boolean isAgentStart;

	public AgentInfoSender(boolean isAgentStart) {
		this.isAgentStart = isAgentStart;
	}

	Socket requestSocket = null;

	public void run() {
		if (isAgentStart) {
			sendAgentStartInfo();
		} else {
			sendAgentStopInfo();
		}
	}

	private void sendAgentStopInfo() {
		try {
			connectToServer();

			ObjectOutputStream stream = new ObjectOutputStream(requestSocket.getOutputStream());
			AgentInfoDTO dto = new AgentInfoDTO();
			dto.setIsDead();

			logger.info("send agent stop info. %s", dto.toString());

			stream.writeObject(dto);
			stream.close();

			logger.info("Agent Stopped message is sent. %s", dto.toString());
		} catch (Exception e) {
			logger.error("AgentInfoSender Exception occured : %s", e.getMessage());
		} finally {
			closeSocket();
		}
	}

	private void sendAgentStartInfo() {

		while (connectToServer()) {
			try {
				Thread.sleep(TomcatProfilerConfig.SERVER_CONNECT_RETRY_GAP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			ObjectOutputStream stream = new ObjectOutputStream(requestSocket.getOutputStream());

			// stream.write(("AGENT_HASH="+JVMInfoDTO.hostHashCode).getBytes());
			// stream.write(("AGENT_IP="+JVMInfoDTO.hostIP).getBytes());
			// stream.write(("AGENT_PORT="+JVMInfoDTO.portNumber).getBytes());

			AgentInfoDTO dto = new AgentInfoDTO();

			logger.info("send agent startup info. %s", dto.toString());

			stream.writeObject(dto);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (requestSocket != null) {
			closeSocket();
		}
	}

	private boolean connectToServer() {
		try {
			logger.info("Trying to connect server. %s:%s", TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);

			requestSocket = new Socket(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);

			logger.info("Connected to server. %s:%s", TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
			return false;
		} catch (java.net.ConnectException ce) {
			logger.fatal("Connect to TomcatProfiler server is failed. %s:%s", TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void closeSocket() {
		try {
			requestSocket.close();
			logger.info("TCP RequestSocket is closed");
		} catch (Exception e) {
			logger.error("closeSocket(). %s", e.getMessage());
		}
	}
}
