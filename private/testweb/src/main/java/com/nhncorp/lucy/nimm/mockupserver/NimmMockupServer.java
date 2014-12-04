package com.nhncorp.lucy.nimm.mockupserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nhncorp.lucy.nimm.connector.NimmRunTimeException;
import com.nhncorp.lucy.nimm.connector.util.NimmLoggingFilter;
import com.nhncorp.lucy.nimm.connector.address.AbnormalNimmAddressException;
import com.nhncorp.lucy.nimm.connector.address.AddressAdmin;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress.Species;
import com.nhncorp.lucy.nimm.connector.record.NimmApplicationRecord;
import com.nhncorp.lucy.nimm.connector.record.NimmControlRecord;
import com.nhncorp.lucy.nimm.connector.record.NimmControlRecordFactory;
import com.nhncorp.lucy.nimm.connector.record.NimmRecord;
import com.nhncorp.lucy.nimm.connector.record.NimmRecordDecoder;
import com.nhncorp.lucy.nimm.connector.record.NimmRecordEncoder;
import com.nhncorp.lucy.nimm.connector.record.NimmRecordHeader;
import com.nhncorp.lucy.nimm.connector.record.NimmRecordHeaderFactory;
import com.nhncorp.lucy.nimm.connector.record.NimmControlRecord.ControlCode;

import external.org.apache.mina.common.DummySession;
import external.org.apache.mina.common.IdleStatus;
import external.org.apache.mina.common.IoBuffer;
import external.org.apache.mina.common.IoHandlerAdapter;
import external.org.apache.mina.common.IoSession;
import external.org.apache.mina.filter.codec.ProtocolCodecFilter;
import external.org.apache.mina.transport.socket.nio.NioSocketAcceptor;

abstract class NimmMockupServer {
	private final Logger logger = Logger.getLogger(getClass().getName());

	private final int portNo;
	private final IoHandler ioHandler;
	private final NioSocketAcceptor socketAcceptor;

	private final NimmAddress serverNimmAddress;

	private final ConcurrentMap<ClientKey, IoSession> ioSessionLookUpMap;
//	private final ConcurrentMap<AnycastKey, List<IoSessionPair>> ioSessionAnycastLookUpMap;
	private final AnycastRoutingTable anycastTable;

	private final Set<IoSession>  connectedIoSession = new CopyOnWriteArraySet<IoSession>();

	private final ConcurrentMap<IoSession, NimmAddress> representAddress;

	protected AddressAdmin addressAdmin = NimmAddress.getHandle();

//	private final String ROUTING_STATE = "_ROUTING_STATE";

	protected NimmMockupServer(int portNo, NimmAddress serverAddress) {
		this.portNo = portNo;
		this.serverNimmAddress = serverAddress;

		this.ioSessionLookUpMap = new ConcurrentHashMap<ClientKey, IoSession>();
//		this.ioSessionAnycastLookUpMap = new ConcurrentHashMap<AnycastKey, List<IoSessionPair>>();
		this.anycastTable = new AnycastRoutingTable();

		this.representAddress = new ConcurrentHashMap<IoSession, NimmAddress>();

		this.ioHandler = new IoHandler();

		this.socketAcceptor = new NioSocketAcceptor();
		this.socketAcceptor.setDefaultLocalAddress(new InetSocketAddress(portNo));
		this.socketAcceptor.setHandler(this.ioHandler);

		this.socketAcceptor.getFilterChain().addLast("raw-log", new NimmLoggingFilter("mockup-server"));
		this.socketAcceptor.getFilterChain().addLast(
				"nimm-record-codec",
				new ProtocolCodecFilter(new NimmRecordEncoder(),
						new NimmRecordDecoder()));

	}

	public void start() throws IOException {
		this.socketAcceptor.bind();
	}

	public void gracefulStop() {
		broadcast(new IoSessionTask(){
			public void execute(IoSession ioSession) {
				sendStopSending(ioSession);
			}
		});

		broadcast(new IoSessionTask(){
			public void execute(IoSession ioSession) {
				sendBye(ioSession);
			}
		});
		try {
			Thread.sleep(1000*2);
		}
		catch (InterruptedException e) {
			logger.log(Level.WARNING, "InterruptedException", e);
		}
		stop();
	}

	public void stop() {
		this.socketAcceptor.dispose();
	}

	public int getPortNo() {
		return this.portNo;
	}

	protected abstract NimmAddress createAddress(byte[] binform) throws AbnormalNimmAddressException;

	protected abstract Species getSpecies();

	protected NimmAddress createAddress(Species species, byte[] bytesForm) throws AbnormalNimmAddressException {

		if (species == null) {
			throw new NullPointerException();
		}
		if (bytesForm == null) {
			throw new NullPointerException();
		}
		if (bytesForm.length != 12) {
			throw new IllegalArgumentException();
		}

		ByteBuffer bb = ByteBuffer.wrap(bytesForm);
		int domainId = bb.getInt();
		int idcId = (int) bb.getShort();
		int serverId = (int) bb.getShort();
		int socketId = (int) bb.getShort();
		int checkSum = (int) bb.getShort();

		return addressAdmin.retrieveAddressInstance(species, domainId, idcId, serverId, socketId, checkSum);
	}

	private void processControlRecord(NimmControlRecord controlRecord, IoSession ioSession) throws Exception {

		ControlCode controlCode = controlRecord.getControlCode();

		switch (controlCode) {
			case HELLO:
				handleHello(controlRecord, ioSession);
				break;
			case LEAVE:
				hadleLeave(ioSession);
				break;
			case START_ROUTING:
				handleStartRouting(controlRecord, ioSession);
				break;
			case STOP_ROUTING:
				handleStopRouting(controlRecord, ioSession);
				break;
			default:
				if(logger.isLoggable(Level.INFO)){
					logger.info("controlCode:" + controlCode);
				}
				// BEGIN_LARGE_MESSAGE 등의 controlmessage는 target NimmConnector까지 전달되어야 하므로 그대로 라우팅한다.
				routeNimmRecord(controlRecord, ioSession);
		}
	}

	private void handleHello(NimmControlRecord controlRecord, IoSession ioSession) throws AbnormalNimmAddressException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("HELLO received source:" + controlRecord.getSourceAddress()
					+ " destination:" + controlRecord.getDestinationAddress());
		}
		IoBuffer payloadBuffer = controlRecord.getPayloadBuffer();
		int numberOfAddresses = controlRecord.getControlParameterAsNumber();
		int addressSize = NimmAddress.getSizeOfBinForm();

		// To find duplicated address
		boolean isDuplicated = false;
		NimmAddress firstDuplicated = null;

		for (int i = 0; i < numberOfAddresses; ++i) {
			byte[] binFormAddress = new byte[addressSize];
			payloadBuffer.get(binFormAddress);
			NimmAddress address = createAddress(binFormAddress);
			if(logger.isLoggable(Level.FINE)){
				logger.fine("Hello payload address:" + address);
			}
			int domainId = address.getDomainId();
			int idcId = this.serverNimmAddress.getIdcId();
			int serverId = address.getServerId();

			if (i == 0) {
				NimmAddress representAddress = NimmAddress.getHandle().retrieveAddressInstance(address.getSpecies(),
						0, idcId, serverId, 0, 0);
				this.representAddress.put(ioSession, representAddress);
			}

			RoutingState routingState = RoutingState.getRoutingState(ioSession);
			routingState.register(domainId);

			ClientKey key = new ClientKey(domainId, idcId, serverId);
			// Duplicated address check
			if (this.ioSessionLookUpMap.containsKey(key)) {
				logger.warning("Duplicated detected:" + key);
				isDuplicated = true;
				if (firstDuplicated == null) {
					firstDuplicated = address;
				}
			}

			this.ioSessionLookUpMap.put(key, ioSession);

			AnycastKey anyKey = new AnycastKey(domainId, idcId);
//			List<IoSessionPair> list = this.ioSessionAnycastLookUpMap.get(anyKey);
//			if (list == null) {
//				list = new CopyOnWriteArrayList<IoSessionPair>();
//			}
//			list.add(new IoSessionPair(ioSession, address));
//			this.ioSessionAnycastLookUpMap.put(anyKey, list);
			this.anycastTable.register(anyKey, new IoSessionPair(ioSession, address));
		}

		if (isDuplicated) {
			// Duplicated address BYE message
			NimmRecordHeader header = NimmRecordHeaderFactory.createBYEwithDuplicatedAddr(firstDuplicated,
					this.serverNimmAddress);
			byte[] payload = new byte[12];
			ByteBuffer buf = ByteBuffer.wrap(payload);
			buf.putInt(firstDuplicated.getDomainId());
			buf.putShort((short)this.serverNimmAddress.getIdcId());
			buf.putShort((short)firstDuplicated.getServerId());
			buf.putShort((short)0);
			buf.putShort((short)0);
			NimmControlRecord byeRecord = NimmControlRecordFactory.create(header, payload);

			ioSession.write(byeRecord);
			ioSession.close();
		} else {
			NimmControlRecord welcomeRecord = NimmControlRecord.createControlMessage(this.serverNimmAddress,
							this.representAddress.get(ioSession),
							ControlCode.WELCOME, null);

			ioSession.write(welcomeRecord);

			sendStartSending(ioSession);
		}
	}

	private void hadleLeave(IoSession ioSession) {
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("LEAVE(Control) received IoSession:" + ioSession);
		}
		Iterator<Map.Entry<ClientKey, IoSession>> iter = this.ioSessionLookUpMap.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<ClientKey, IoSession> entry = iter.next();

			if (entry.getValue().equals(ioSession)) {
				iter.remove();
			}
		}

		NimmControlRecord byeRecord = NimmControlRecord.createControlMessage(this.serverNimmAddress,
						this.representAddress.get(ioSession),
						ControlCode.BYE, null);

		ioSession.write(byeRecord);
		this.representAddress.remove(ioSession);
	}

	private void handleStopRouting(NimmControlRecord controlRecord, IoSession ioSession) {
		final NimmAddress destination = controlRecord.getDestinationAddress();
		final NimmAddress source = controlRecord.getSourceAddress();
		// TODO 추가 처리 필요.
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("STOP_ROUTING received source:" + source + " destination:" + destination);
		}
		RoutingState routingState = RoutingState.getRoutingState(ioSession);
		routingState.routingEnable(source.getDomainId(), false);

	}

	private void handleStartRouting(NimmControlRecord controlRecord, IoSession ioSession) {
		final NimmAddress destination = controlRecord.getDestinationAddress();
		final NimmAddress source = controlRecord.getSourceAddress();
		// TODO 추가 처리 필요.
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("START_ROUTING received source:" + source + " destination:" + destination);
		}
		RoutingState routingState = RoutingState.getRoutingState(ioSession);
		routingState.routingEnable(source.getDomainId(), true);

	}

	private void sendStartSending(IoSession ioSession) {
		NimmControlRecord startSending = NimmControlRecord.createControlMessage(this.serverNimmAddress,
					this.representAddress.get(ioSession), NimmControlRecord.ControlCode.START_SENDING, null);
		ioSession.write(startSending);
	}

	private void sendStopSending(IoSession ioSession) {
		NimmControlRecord startSending = NimmControlRecord.createControlMessage(this.serverNimmAddress,
					this.representAddress.get(ioSession), NimmControlRecord.ControlCode.STOP_SENDING, null);
		if(logger.isLoggable(Level.INFO)) {
			logger.info("sendStopSending:" + ioSession + " IoSession:" + ioSession);
		}
		ioSession.write(startSending);
	}

	private void sendBye(IoSession ioSession) {
		NimmControlRecord bye = NimmControlRecord.createControlMessage(this.serverNimmAddress,
					this.representAddress.get(ioSession), NimmControlRecord.ControlCode.BYE, null);
		if(logger.isLoggable(Level.INFO)) {
			logger.info("sendBye:" + ioSession + " IoSession:" + ioSession);
		}
		ioSession.write(bye);
	}

	private interface IoSessionTask {
		void execute(IoSession ioSession);
	}

	private void broadcast(IoSessionTask task) {
		for(IoSession ioSession: connectedIoSession) {
			task.execute(ioSession);
		}
	}

	private void routeNimmRecord(NimmRecord nimmRecord, IoSession ioSession) throws AbnormalNimmAddressException {

		final NimmAddress destionationAddress = nimmRecord.getDestinationAddress();

		int idcId = destionationAddress.getIdcId();
		int domainId = destionationAddress.getDomainId();
		int serverId = destionationAddress.getServerId();

		if (idcId != 0 && serverId != 0) {
			ClientKey ioSessionLookUpKey = new ClientKey(domainId, idcId, serverId);
			IoSession targetIoSession = this.ioSessionLookUpMap.get(ioSessionLookUpKey);
			if (targetIoSession == null) {
				throw new NimmRunTimeException("idcId:" + idcId + " domainId:" + domainId + " serverId:" + serverId);
			} else {
				targetIoSession.write(nimmRecord);
			}
			return;
		}
		else if (idcId == 0 && serverId != 0) {
			final int connectorid = serverNimmAddress.getIdcId();
			final ClientKey ioSessionLookUpKey = new ClientKey(domainId, connectorid, serverId);
			final IoSession targetIoSession = this.ioSessionLookUpMap.get(ioSessionLookUpKey);
			if (targetIoSession == null) {
				throw new NimmRunTimeException("idcId:" + idcId + " domainId:" + domainId + " serverId:" + serverId);
			} else {
				NimmAddress unicastAddress = NimmAddress.createUnicastAddress(destionationAddress.getDomainId(), connectorid,
						destionationAddress.getServerId(), destionationAddress.getSocketId());
				nimmRecord.setDestinationAddress(unicastAddress);
				targetIoSession.write(nimmRecord);
			}
			return;
		}
		else if (serverId == 0) {
			if(idcId == 0) {
				idcId = serverNimmAddress.getIdcId();
			}
			AnycastKey key = new AnycastKey(domainId, idcId);
			IoSessionPair pair = null;

			if(nimmRecord instanceof NimmApplicationRecord) {
				pair = this.anycastTable.roundRobinSelect(key, true);
			} else {
				if(nimmRecord instanceof NimmControlRecord.TypeLarge) {
					// LARGE_SESSION은 ??
					pair = this.anycastTable.roundRobinSelect(key, true);
				} else {
					pair = this.anycastTable.roundRobinSelect(key, false);
				}
			}
			if(pair == null) {
				//TODO TARGET_SERVER_NO_EXIST?? NACK을 던저야 한다.
				throw new NimmRunTimeException("idcId:" + idcId + " domainId:" + domainId + " serverId:" + serverId + " Record:" + nimmRecord);
			}
			NimmAddress address = pair.getAddress();
			NimmAddress uniAddress = NimmAddress.createUnicastAddress(address.getDomainId(), address.getIdcId(),
						address.getServerId(), destionationAddress.getSocketId());
			nimmRecord.setDestinationAddress(uniAddress);
			pair.getIoSession().write(nimmRecord);
			return;
		}
		throw new RuntimeException("serverid:" + serverId);
	}

	/**
	 * 주어진 주소를 가진  Client (NimmConnector) 가  이미 연결되어 있는 것으로 만드는 메소드
	 * 중복 주소 오류를 체크하기 위해 사용한다.
	 * IoSession은 DummySession이 사용된다.
	 *
	 * @param domainId dummy client Domain Id
	 * @param serverId dummy client Server Id
	 */
	void addDummyClientManually(int domainId, int serverId) {
		int idcId = this.serverNimmAddress.getIdcId();
		ClientKey key = new ClientKey(domainId, idcId, serverId);
		IoSession session = new DummySession();
		this.ioSessionLookUpMap.put(key, session);
	}

	/**
	 * 주어진 주소를 가진  Client (NimmConnector) 가  이미 연결되어 있는 것을 삭제 하기 위한 메소드
	 * IoSession은 DummySession이 사용된다.
	 *
	 * @param domainId dummy client Domain Id
	 * @param serverId dummy client Server Id
	 */
	void removeDummyClientManually(int domainId, int serverId) {
		int idcId = this.serverNimmAddress.getIdcId();
		ClientKey key = new ClientKey(domainId, idcId, serverId);
		this.ioSessionLookUpMap.remove(key);
	}

	private class IoHandler extends IoHandlerAdapter {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			// TODO Auto-generated method stub
			super.exceptionCaught(session, cause);
		}



		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {

			NimmRecord nimmRecord = (NimmRecord) message;
			if (nimmRecord instanceof NimmControlRecord) {
				processControlRecord((NimmControlRecord) nimmRecord, session);
			} else if (nimmRecord instanceof NimmApplicationRecord) {
				routeNimmRecord(nimmRecord, session);
			}
		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {
			// TODO Auto-generated method stub
			super.messageSent(session, message);
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			// TODO Auto-generated method stub
			connectedIoSession.remove(session);
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			// TODO Auto-generated method stub
			super.sessionCreated(session);
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			// TODO Auto-generated method stub
			super.sessionIdle(session, status);
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {

			InetSocketAddress[] socketAddresses = new InetSocketAddress[1];
			socketAddresses[0] = (InetSocketAddress)session.getRemoteAddress();
			// TODO 전역공유객체
			addressAdmin.addInetAddressSpecies(getSpecies(), socketAddresses);
			connectedIoSession.add(session);

//			session.setAttribute(ROUTING_STATE, new ConcurrentHashMap<Integer, AtomicBoolean>());
			RoutingState.createRoutingState(session);
		}
	}

}
