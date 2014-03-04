package com.nhncorp.lucy.nimm.connector.agent;

import java.nio.ByteBuffer;

import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.agent.provider.NimmAgentArchetype;
import com.nhncorp.lucy.nimm.connector.message.NimmMessage;
import com.nhncorp.lucy.nimm.connector.worker.NimmAbstractWorker;
import com.nhncorp.lucy.nimm.connector.worker.NimmWorker;

public class ServiceAgent extends NimmAgentArchetype implements ServiceAgentAPI {

	private final Worker worker = new Worker();

	ServiceAgent() {
	}

	@Override
	public void init() {
		// ServiceAgentTest.initMethodCalled = true;
	}

	@Override
	public void destroy() {
		// ServiceAgentTest.destoryMethodCalled = true;
	}

	public String getVersion() {
		return "ComicSexy";
	}

	public String getName() {
		return "MyNameWillBeForgotten";
	}


	@Override
	public NimmWorker getNimmWorker() {
		return this.worker;
	}


	private class Worker extends NimmAbstractWorker {

		@Override
		public void destroy() {

		}

		@Override
		public void init() {
		}

		@Override
		protected void processMessage(NimmMessage message) throws Exception {

		}

		@Override
		protected ByteBuffer responseMessage(NimmMessage request)
				throws Exception {
			return request.getMessage();
		}

	}


	public void sendMessage(NimmAddress targetAddress, byte[] message) {
		getMySocket().send(targetAddress,message);
	}
}
