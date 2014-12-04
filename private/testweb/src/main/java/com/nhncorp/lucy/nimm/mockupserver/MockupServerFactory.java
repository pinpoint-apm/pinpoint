package com.nhncorp.lucy.nimm.mockupserver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.nhncorp.lucy.nimm.connector.address.AddressAdmin;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.address.AbnormalNimmAddressException;

class MockupServerFactory {

	private volatile int idcId;

	private final Map<NimmAddress.Species, AtomicInteger> serverIds;

	private AddressAdmin addressAdmin = NimmAddress.getHandle();

	public static int serverDomainId = 1;

	public static int serverSocketId = 0;

	MockupServerFactory(int idcId) {
		this.idcId = idcId;
		this.serverIds = new HashMap<NimmAddress.Species, AtomicInteger>();

		for (NimmAddress.Species species : NimmAddress.Species.values()) {
			this.serverIds.put(species, new AtomicInteger(1));
		}
	}

	int getIdcId() {
		return this.idcId;
	}

	void setIdcId(int idcId) {
		this.idcId = idcId;
	}

	NimmMockupServer createMockUpServer(NimmAddress.Species species, int portNo) {

		if (species == null)
			throw new NullPointerException("species");

		NimmMockupServer mockupServer = null;

		NimmAddress serverAddress = null;
		try {
			serverAddress = addressAdmin.retrieveAddressInstance(
				NimmAddress.Species.Management, serverDomainId, this.idcId,
					this.serverIds.get(species).getAndIncrement(), serverSocketId,
					0);
		}
		catch (AbnormalNimmAddressException e) {
			throw new RuntimeException("address create error", e);
		}

		switch (species) {
		case Management:
			mockupServer = new NimmMGMTMockupServer(portNo, serverAddress);
			break;
		case Service:
			mockupServer = new NimmSvcMockupServer(portNo, serverAddress);
			break;
		}

		return mockupServer;
	}

}
