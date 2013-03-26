package com.nhn.hippo.web.applicationmap;

import java.util.HashSet;
import java.util.Set;

import com.profiler.common.ServiceType;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 */
public class Application implements Comparable<Application> {
	protected int sequence;
	protected final String id;

	protected final Set<String> hosts = new HashSet<String>();
	protected final String applicationName;
	protected final ServiceType serviceType;

	protected long recursiveCallCount;

	public Application(String id, String applicationName, ServiceType serviceType, Set<String> hosts) {
		this.id = id;
		if (serviceType == ServiceType.CLIENT) {
			this.applicationName = "CLIENT";
		} else {
			this.applicationName = applicationName;
		}
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
		this.serviceType = serviceType;
	}

	public String getId() {
		return this.id;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	public Set<String> getHosts() {
		return hosts;
	}

	public void setHosts(Set<String> hosts) {
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
	}

	public String getApplicationName() {
		return applicationName;
	}

	public long getRecursiveCallCount() {
		return recursiveCallCount;
	}

	public void mergeWith(Application application) {
		this.recursiveCallCount += application.recursiveCallCount;
		this.hosts.addAll(application.getHosts());
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void incrRecursiveCallCount(long count) {
		this.recursiveCallCount += count;
	}

	@Override
	public int compareTo(Application server) {
		return id.compareTo(server.id);
	}

	@Override
	public String toString() {
		return "Application [applicationName=" + applicationName + ", serviceType=" + serviceType + ", hosts=" + hosts + ", recursiveCallCount=" + recursiveCallCount + "]";
	}
}
