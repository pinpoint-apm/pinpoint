package com.nhn.pinpoint.collector.cluster;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.nhn.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.rpc.util.AssertUtils;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ChannelContextClusterPoint implements TargetClusterPoint {

	private final ChannelContext channelContext;
	private final SocketChannel socketChannel;

	private final String applicationName;
	private final String agentId;
	private final long startTimeStamp;

	private final String version;

	public ChannelContextClusterPoint(ChannelContext channelContext) {
		AssertUtils.assertNotNull(channelContext, "ChannelContext may not be null.");
		this.channelContext = channelContext;

		this.socketChannel = channelContext.getSocketChannel();
		AssertUtils.assertNotNull(socketChannel, "SocketChannel may not be null.");

		Map<Object, Object> properties = channelContext.getChannelProperties();
		this.version = MapUtils.getString(properties, AgentHandshakePropertyType.VERSION.getName());
		AssertUtils.assertTrue(!StringUtils.isBlank(version), "Version may not be null or empty.");

		this.applicationName = MapUtils.getString(properties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
		AssertUtils.assertTrue(!StringUtils.isBlank(applicationName), "ApplicationName may not be null or empty.");

		this.agentId = MapUtils.getString(properties, AgentHandshakePropertyType.AGENT_ID.getName());
		AssertUtils.assertTrue(!StringUtils.isBlank(agentId), "AgentId may not be null or empty.");

		this.startTimeStamp = MapUtils.getLong(properties, AgentHandshakePropertyType.START_TIMESTAMP.getName());
		AssertUtils.assertTrue(startTimeStamp > 0, "StartTimeStamp is must greater than zero.");
	}

	@Override
	public void send(byte[] data) {
		socketChannel.sendMessage(data);
	}

	@Override
	public Future request(byte[] data) {
		return socketChannel.sendRequestMessage(data);
	}

	@Override
	public String getApplicationName() {
		return applicationName;
	}

	@Override
	public String getAgentId() {
		return agentId;
	}

	public long getStartTimeStamp() {
		return startTimeStamp;
	}

	@Override
	public String gerVersion() {
		return version;
	}

    public ChannelContext getChannelContext() {
        return channelContext;
    }
    
    @Override
    public String toString() {
        return socketChannel.toString();
    }
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ChannelContextClusterPoint)) {
			return false;
		}

		if (this.getChannelContext() == ((ChannelContextClusterPoint) obj).getChannelContext()) {
			return true;
		}

		return false;
	}

}
