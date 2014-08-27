package com.nhncorp.lucy.nimm.mockupserver;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.nhncorp.lucy.nimm.connector.NimmRunTimeException;
import com.nhncorp.lucy.nimm.connector.address.AbnormalNimmAddressException;
import com.nhncorp.lucy.nimm.connector.address.AddressAdmin;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress.Species;

class ClientInformation {

	private final int domainId;
	private final int idcId;
	private final int serverId;
	private NimmAddress representativeAddress;
	private Set<Integer> domainIdSet;

	private final AddressAdmin addressAdmin = NimmAddress.getHandle();


	ClientInformation(Species species, int domainId, int idcId, int serverId) {
		this.domainId = domainId;
		this.idcId = idcId;
		this.serverId = serverId;
		this.domainIdSet = Collections.synchronizedSet(new TreeSet<Integer>());

		try {
			this.representativeAddress =  addressAdmin.retrieveAddressInstance(species,0,
						this.idcId, this.serverId, 0, 0);
		} catch (AbnormalNimmAddressException e) {
			throw new NimmRunTimeException(e);
		}
	}


	void addDomainId(int domainId) {
		this.domainIdSet.add(domainId);
	}


	int getIdcId() {
		return idcId;
	}

	int getServerId() {
		return serverId;
	}

	int getDomainId() {
		return domainId;
	}


	NimmAddress getRepresentativeAddress() {
		return this.representativeAddress;
	}


}
