package com.nhn.pinpoint.web.config;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author koo.taejin <kr14910>
 */
public class WebConfig {

	@Value("#{pinpointWebProps['cluster.enable'] ?: false}")
	private boolean clusterEnable;
	
	@Value("#{pinpointWebProps['cluster.web.tcp.port'] ?: 0}")
	private int clusterTcpPort;

	@Value("#{pinpointWebProps['cluster.zookeeper.address'] ?: ''}")
	private String clusterZookeeperAddress;

	@Value("#{pinpointWebProps['cluster.zookeeper.sessiontimeout'] ?: -1}")
	private int clusterZookeeperSessionTimeout;

	@Value("#{pinpointWebProps['cluster.zookeeper.retry.interval'] ?: 60000}")
	private int clusterZookeeperRetryInterval;
	
	@PostConstruct
	public void validation() {
		if (isClusterEnable()) {
			assertPort(clusterTcpPort);
			if(StringUtils.isEmpty(clusterZookeeperAddress)) {
				throw new IllegalArgumentException("clusterZookeeperAddress may not be empty =" + clusterZookeeperAddress);
			}
			assertPositiveNumber(clusterZookeeperSessionTimeout);
			assertPositiveNumber(clusterZookeeperRetryInterval);
		}
	}

	private boolean assertPort(int port) {
		if (port > 0 && 65535 > port) {
			return true;
		}
		
		throw new IllegalArgumentException("Invalid Port =" + port);
	}
	
	private boolean assertPositiveNumber(int number) {
		if (number >= 0) {
			return true;
		}
		
		throw new IllegalArgumentException("Invalid Positive Number =" + number);
	}

	public boolean isClusterEnable() {
		return clusterEnable;
	}

	public int getClusterTcpPort() {
		return clusterTcpPort;
	}

	public String getClusterZookeeperAddress() {
		return clusterZookeeperAddress;
	}

	public int getClusterZookeeperSessionTimeout() {
		return clusterZookeeperSessionTimeout;
	}
	
	@Override
	public String toString() {
		return "WebConfig [clusterEnable=" + clusterEnable
				+ ", clusterTcpPort=" + clusterTcpPort
				+ ", clusterZookeeperAddress=" + clusterZookeeperAddress
				+ ", clusterZookeeperSessionTimeout="
				+ clusterZookeeperSessionTimeout + "]";
	}

	public int getClusterZookeeperRetryInterval() {
		return clusterZookeeperRetryInterval;
	}

	public void setClusterZookeeperRetryInterval(int clusterZookeeperRetryInterval) {
		this.clusterZookeeperRetryInterval = clusterZookeeperRetryInterval;
	}

}
