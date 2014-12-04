package com.nhncorp.lucy.nimm.mockupserver;

import com.nhncorp.lucy.nimm.connector.address.AbnormalNimmAddressException;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress.Species;

class NimmSvcMockupServer extends NimmMockupServer {

	NimmSvcMockupServer(int portNo, NimmAddress serverAddress) {
		super(portNo, serverAddress);

	}

	@Override
	protected NimmAddress createAddress(byte[] binform)
			throws AbnormalNimmAddressException {
		return createAddress(Species.Management, binform);

	}

	@Override
	protected Species getSpecies() {
		return Species.Service;
	}



}
