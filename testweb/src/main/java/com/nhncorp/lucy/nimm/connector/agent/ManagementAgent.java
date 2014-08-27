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

@AgentInfo(domainId = 7, socketId = 1)
public class ManagementAgent extends NimmAgentArchetype implements ManagementAgentAPI {
	private Logger logger = Logger.getLogger(getClass().getName());

	private final NimmWorker worker;

	public ManagementAgent() {
		this.worker = new ManagementWorker();
	}

	@Override
	public void init() {
		// ManagementAgentTest.initMethodCallCount++;
	}

	@Override
	public void destroy() {
		// ManagementAgentTest.destoryMethodCallCount++;
	}

	@Override
	public NimmWorker getNimmWorker() {
		return this.worker;
	}

	public String getName() {
		return "MGMTAgentTest";
	}

	public String getVersion() {
		return "1.0.0";
	}

	public void setRala(int value) {
		logger.fine(String.valueOf(value));
	}

	public void setWRala(Integer value) {
		logger.fine(String.valueOf(value));
	}

	public void setCooKoo(String value) {
		logger.fine(String.valueOf(value));
	}

	public void setYoYo(float value) {
		logger.fine(String.valueOf(value));
	}

	public void setWYoYo(Float value) {
		logger.fine(String.valueOf(value));
	}

	public void setOhoo(double value) {
		logger.fine(String.valueOf(value));
	}

	public void setWohoo(Double value) {
		logger.fine(String.valueOf(value));
	}

	public void setAwesome(char value) {
		logger.fine(String.valueOf(value));
	}

	public void setWawesome(Character value) {
		logger.fine(String.valueOf(value));
	}

	public void setActive(boolean value) {
		logger.fine(String.valueOf(value));
	}

	public void setWactive(Boolean value) {
		logger.fine(String.valueOf(value));
	}

	public void setBinary(byte value) {
		logger.fine(String.valueOf(value));
	}

	public void setWbinary(Byte value) {
		logger.fine(String.valueOf(value));
	}

	public void setShortint(short value) {
		logger.fine(String.valueOf(value));
	}

	public void setWshortint(Short value) {
		logger.fine(String.valueOf(value));
	}

	public void setLongint(long value) {
		logger.fine(String.valueOf(value));
	}

	public void setWlongint(Long value) {
		logger.fine(String.valueOf(value));
	}

	private class ManagementWorker extends NimmAbstractWorker {
		@Override
		public void destroy() {
		}

		@Override
		public void init() {
		}

		@Override
		protected void processMessage(NimmMessage message) throws Exception {
			getMySocket().send(message.getSourceAddress(), message.getMessage());
		}

		@Override
		protected ByteBuffer responseMessage(NimmMessage request) throws Exception {
			return request.getMessage();
		}

	}

	public boolean loopMessage() throws Exception {

		NimmSocket localSocket = createSocket();

		byte[] msg = "UlaUlaWowWow".getBytes();
		MessageFuture future = localSocket.request(getMySocket(), msg);

		future.await();

		return Arrays.equals(future.getResponse().getMessageAsArray(), msg);
	}

}
