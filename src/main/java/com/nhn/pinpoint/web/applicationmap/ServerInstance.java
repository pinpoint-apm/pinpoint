package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * 
 * @author netspider
 * 
 */
public class ServerInstance implements Comparable<ServerInstance>, Mergeable<ServerInstance>, JsonSerializable {

	private final String id;
	private final AgentInfoBo agentInfo;
	private ResponseHistogram histogram;

	public ServerInstance(AgentInfoBo agentInfo, ResponseHistogram histogram) {
		this.id = agentInfo.getAgentId() + agentInfo.getServiceType();
		this.agentInfo = agentInfo;
		this.histogram = histogram;
	}
	
	public ServerInstance(String id, ResponseHistogram histogram) {
		this.id = id;
		this.agentInfo = null;
		this.histogram = histogram;
	}

	public String getId() {
		return this.id;
	}

	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}

	public void setHistogram(ResponseHistogram histogram) {
		this.histogram = histogram;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	@Override
	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"agentId\":\"").append(id).append("\",");
		sb.append("\"agentInfo\":").append((agentInfo == null) ? null : agentInfo.getJson()).append(",");
		sb.append("\"histogram\":").append((histogram == null) ? null : histogram.getJson());
		sb.append("}");
		return sb.toString();
	}

	@Override
	public ServerInstance mergeWith(ServerInstance serverInstance) {
		if (!this.id.equals(serverInstance.getId())) {
			throw new IllegalArgumentException("Server instance id is not equal.");
		}

		if (this.histogram == null) {
			this.histogram = serverInstance.getHistogram();
		} else {
			// this.histogram.mergeWith(serverInstance.getHistogram());
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ServerInstance other = (ServerInstance) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServerInstance [id=" + id + ", agentInfo=" + agentInfo + ", histogram=" + histogram + "]";
	}

	@Override
	public int compareTo(ServerInstance serverInstance) {
		return this.id.compareTo(serverInstance.id);
	}
}
