package com.nhncorp.lucy.nimm.mockupserver;

import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import external.org.apache.mina.common.IoSession;

/**
 * User: emeroad
 * Date: 2010. 3. 24
 * Time: 오전 11:57:46
 *
 * @author Middleware Platform Dev. Team
 */
public class IoSessionPair {
	private IoSession ioSession;
	private NimmAddress address;


	public IoSessionPair(IoSession session, NimmAddress address){
		this.ioSession = session;
		this.address = address;
	}

	public IoSession getIoSession() {
		return ioSession;
	}

	public NimmAddress getAddress() {
		return address;
	}

}
