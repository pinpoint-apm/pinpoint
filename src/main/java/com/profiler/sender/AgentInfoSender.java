package com.profiler.sender;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.dto.AgentInfoDTO;
import com.profiler.config.TomcatProfilerConfig;

public class AgentInfoSender extends Thread {

	private final Logger logger = Logger.getLogger(AgentInfoSender.class.getName());

	private boolean isAgentStart;
	private Socket requestSocket = null;

	public AgentInfoSender(boolean isAgentStart) {
		this.isAgentStart = isAgentStart;
	}

	public void run() {
        logger.info("send agent info");

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
            if (logger.isLoggable(Level.INFO)) {
			    logger.info("send agent stop info. " + dto.toString());
            }

			stream.writeObject(dto);
			stream.close();

            if (logger.isLoggable(Level.INFO)){
                logger.info("Agent Stopped message is sent. " + dto.toString());
            }

		} catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, "AgentInfoSender Exception occured:" + e.getMessage(), e);
            }
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
			AgentInfoDTO dto = new AgentInfoDTO();

            if (logger.isLoggable(Level.INFO)) {
                logger.info("send agent startup info. " + dto.toString());
            }


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
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Trying to connect server. " + TomcatProfilerConfig.SERVER_IP + ":" + TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
            }
            requestSocket = new Socket(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
            // TODO timeout 처리가 없음. api를 변경해야 될듯.
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Connected to server. " + TomcatProfilerConfig.SERVER_IP + ":" + TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT);
            }

            return false;
		} catch (java.net.ConnectException ce) {

            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Connect to TomcatProfiler server is failed. " + TomcatProfilerConfig.SERVER_IP + ":" + TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT, ce);
            }

            return true;
		} catch (Exception e) {
             if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Connect to TomcatProfiler server is failed. " + TomcatProfilerConfig.SERVER_IP + ":" + TomcatProfilerConfig.SERVER_TCP_LISTEN_PORT, e);
            }
			return true;
		}
	}

	private void closeSocket() {
		try {
			requestSocket.close();
			logger.info("TCP RequestSocket is closed");
		} catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, "closeSocket(). " + e.getMessage(), e);
            }
		}
	}
}
