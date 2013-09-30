package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * Application의 transaction처리 통계 정보.
 * 
 * @author netspider
 * 
 */
@Deprecated
public class ApplicationStatistics {

	// treemap key 정렬때문에...
	private static final int SLOT_NO_SLOW = Integer.MAX_VALUE - 1;
	private static final int SLOT_NO_ERROR = Integer.MAX_VALUE;

	private String id;
	private final String applicationName;
	private final ServiceType serviceType;

	private long successCount = 0;
	private long failedCount = 0;
	private final SortedMap<Integer, Long> values = new TreeMap<Integer, Long>();
	private final Set<String> hosts;
	private final Set<AgentInfoBo> agents;

	public ApplicationStatistics(String applicationName, short serviceType) {
		this.applicationName = applicationName;
		this.serviceType = ServiceType.findServiceType(serviceType);
		this.hosts = new HashSet<String>();
		this.agents = new HashSet<AgentInfoBo>();
		setDefaultHistogramSlotList(this.serviceType.getHistogram().getHistogramSlotList());
		makeId();
	}

	private void setDefaultHistogramSlotList(List<HistogramSlot> slotList) {
		if (successCount > 0 || failedCount > 0) {
			throw new IllegalStateException("Can't set slot list while containing the data.");
		}
		values.clear();
		values.put(SLOT_NO_ERROR, 0L);
		values.put(SLOT_NO_SLOW, 0L);
		for (HistogramSlot slot : slotList) {
			values.put(slot.getSlotTime(), 0L);
		}
	}

	public void addValue(int slot, long value) {
		if (slot == -1) {
			successCount += value;
			slot = SLOT_NO_ERROR;
		} else if (slot == 0) {
			failedCount += value;
			slot = SLOT_NO_SLOW;
		} else {
			successCount += value;
		}

		long v = (values.containsKey(slot) ? values.get(slot) : 0L);
		v += value;
		values.put(slot, v);
	}

	public ApplicationStatistics mergeWith(ApplicationStatistics applicationStatistics) {
		//if (this.equals(applicationStatistics)) {
			successCount += applicationStatistics.getSuccessCount();
			failedCount += applicationStatistics.getFailedCount();
			hosts.addAll(applicationStatistics.getHosts());
			agents.addAll(applicationStatistics.getAgents());
			for (Entry<Integer, Long> entry : applicationStatistics.getValues().entrySet()) {
				addValue(entry.getKey(), entry.getValue());
			}

			return this;
		//} else {
		//	throw new IllegalArgumentException("Can't merge with different link.");
		//}
	}

	public void makeId() {
		this.id = applicationName + serviceType;
	}

	public String getId() {
		return id;
	}

	public String getFrom() {
		return applicationName;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void clearHosts() {
		this.hosts.clear();
	}

	public long getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(long successCount) {
		this.successCount = successCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(long failedCount) {
		this.failedCount = failedCount;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public Set<String> getHosts() {
		return hosts;
	}

	public Set<AgentInfoBo> getAgents() {
		return agents;
	}

	public SortedMap<Integer, Long> getValues() {
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
		result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
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
		ApplicationStatistics other = (ApplicationStatistics) obj;
		if (applicationName == null) {
			if (other.applicationName != null)
				return false;
		} else if (!applicationName.equals(other.applicationName))
			return false;
		if (serviceType != other.serviceType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ApplicationStatistics [successCount=" + successCount + ", failedCount=" + failedCount + ", id=" + id + ", applicationName=" + applicationName + ", serviceType=" + serviceType + ", values=" + values + ", hosts=" + hosts + ", agents=" + agents + "]";
	}
}
