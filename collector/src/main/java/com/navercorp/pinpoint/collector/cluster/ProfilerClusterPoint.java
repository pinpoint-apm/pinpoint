package com.nhn.pinpoint.collector.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.receiver.tcp.AgentPropertiesType;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ProfilerClusterPoint implements ClusterPoint  {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final CopyOnWriteArrayList<ChannelContext> clusterRepository = new CopyOnWriteArrayList<ChannelContext>();
	
	
	public boolean registerChannelContext(ChannelContext channelContext) {
		boolean isAdd = clusterRepository.addIfAbsent(channelContext);
		
		if (!isAdd) {
			logger.warn("Already registered ChannelContext({}).", channelContext);
		}
		
		return isAdd;
	}
	
	public boolean unregisterChannelContext(ChannelContext channelContext) {
		boolean isRemove = clusterRepository.remove(channelContext);
		
		if (!isRemove) {
			logger.warn("Already unregistered or not registered ChannelContext({}).", channelContext);
		}
		
		return isRemove;
	}
	
	public List<ChannelContext> getChannelContext() {
		return new ArrayList<ChannelContext>(clusterRepository);
	}
	
	public ChannelContext getChannelContext(String applicationName, String agentId) {
		return getChannelContext(applicationName, agentId, -1);
	}

	public ChannelContext getChannelContext(String applicationName, String agentId, long startTimestamp) {
		
		for (ChannelContext channelContext : clusterRepository) {
			if (checkSuitableChannelContext(channelContext, applicationName, agentId, startTimestamp)) {
				return channelContext;
			}
		}
		
		return null;
	}
	
	private boolean checkSuitableChannelContext(ChannelContext channelContext, String applicationName, String agentId, long startTimestamp) {
		Map<Object, Object> properties = channelContext.getChannelProperties();

		if (!applicationName.equals(MapUtils.getString(properties, AgentPropertiesType.APPLICATION_NAME.getName()))) {
			return false;
		}

		if (!agentId.equals(MapUtils.getString(properties, AgentPropertiesType.AGENT_ID.getName()))) {
			return false;
		}
		
		if (startTimestamp <= 0) {
			// Fix Me 
			// startTimestamp도 체크하게 변경해야함
			return false;
		}

		return true;
	}

}
