package com.nhncorp.lucy.nimm.connector.agent;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import com.nhncorp.lucy.nimm.connector.MessageFuture;
import com.nhncorp.lucy.nimm.connector.NimmSocket;
import com.nhncorp.lucy.nimm.connector.agent.provider.AgentInfo;
import com.nhncorp.lucy.nimm.connector.agent.provider.NimmAgentArchetype;
import com.nhncorp.lucy.nimm.connector.message.NimmMessage;
import com.nhncorp.lucy.nimm.connector.worker.NimmAbstractWorker;
import com.nhncorp.lucy.nimm.connector.worker.NimmWorker;

@AgentInfo( domainId = 7, socketId = 1)
public class AnnotatedMgmtAgent extends NimmAgentArchetype implements ManagementAgentAPI {

	private static final Logger LOGGER = Logger.getLogger(AnnotatedMgmtAgent.class.getName());
	
	private final NimmWorker worker;
	
	public AnnotatedMgmtAgent() {
		this.worker = new AnnotatedMgmtWorker();
	}
	@Override
	public void destroy() {
		LOGGER.info("AnnotatedMgmtAgent.destroy()");
	}

	@Override
	public NimmWorker getNimmWorker() {
		return worker;
	}

	@Override
	public void init() {
		LOGGER.info("AnnotatedMgmtAgent.init()");
	}

	public String getName() {
		return "AnnotatedMgmtAgent";
	}

	public String getVersion() {
		return "1.0.0";
	}

	private class AnnotatedMgmtWorker extends NimmAbstractWorker {

		@Override
		public void destroy() {
			LOGGER.info("AnnotatedMgmtWorker.destroy()");
		}

		@Override
		public void init() {
			LOGGER.info("AnnotatedMgmtWorker.init()");
		}

		@Override
		protected void processMessage(NimmMessage message) throws Exception {
			getMySocket().send(message.getSourceAddress(), message.getMessage());
		}

		@Override
		protected ByteBuffer responseMessage(NimmMessage request)
				throws Exception {
			return request.getMessage();
		}

	}

	public boolean loopMessage() throws Exception {
		NimmSocket localSocket = createSocket();

		byte[] msg = "UlaUlaWowWow".getBytes();
		MessageFuture future = localSocket.request(getMySocket(), msg);

		future.await();

		return Arrays.equals(future.getResponse().getMessageAsArray(),msg);
	}
}
