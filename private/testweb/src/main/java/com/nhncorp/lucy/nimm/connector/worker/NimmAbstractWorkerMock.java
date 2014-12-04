package com.nhncorp.lucy.nimm.connector.worker;

import java.nio.ByteBuffer;

import com.nhncorp.lucy.nimm.connector.message.NimmMessage;

/**
 * User: emeroad Date: 2009. 2. 27 Time: 오후 10:34:23
 */
public class NimmAbstractWorkerMock extends NimmAbstractWorker {
	private Exception ex;

	public void init() {
	}

	public void destroy() {
	}

	protected void processMessage(NimmMessage message) throws Exception {
		ex = new UnsupportedOperationException("processMessage");
	}

	protected ByteBuffer responseMessage(NimmMessage request) throws Exception {
		ex = new UnsupportedOperationException("responseMessage");
		return ByteBuffer.allocate(0);
	}

	public void checkException() {
		if (ex == null) {
			return;
		}
		// Assert.fail(ex.getMessage());
	}

	protected void setException(Exception ex) {
		this.ex = ex;
	}
}
