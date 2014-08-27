package com.nhncorp.lucy.nimm.mockupserver;

/**
 * User: emeroad
 * Date: 2010. 3. 24
 * Time: 오전 11:56:13
 *
 * @author Middleware Platform Dev. Team
 */
public class ClientKey {
	private final int domainId;
	private final int idcId;
	private final int serverId;

	ClientKey(int domainId, int idcId, int serverId) {
		this.domainId = domainId;
		this.idcId = idcId;
		this.serverId = serverId;
	}

	int getDomainId() {
		return domainId;
	}

	int getIdcId() {
		return idcId;
	}

	int getServerId() {
		return serverId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + domainId;
		result = prime * result + idcId;
		result = prime * result + serverId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ClientKey other = (ClientKey) obj;
		if (domainId != other.domainId)
			return false;
		if (idcId != other.idcId)
			return false;
		if (serverId != other.serverId)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "ClientKey{" +
				"domainId=" + domainId +
				", idcId=" + idcId +
				", serverId=" + serverId +
				'}';
	}
}
