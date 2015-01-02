/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.SocketChannel;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ChannelContextClusterPoint implements TargetClusterPoint {

    private final ChannelContext channelContex    ;
	private final SocketChannel socketChan    el;

	private final String applicat    onName;
	private final Stri    g agentId;
	private final long st    rtTimeStamp;

	private final    String version;

	public ChannelContextClusterPoint(ChannelConte       t channelContext) {
		AssertUtils.assertNotNull(channelContext, "ChannelC       ntext may not be null.");
		this.c       annelContext = channelContext;

		this.socketChanne        = channelContext.getSocketChannel();
		AssertUtils.assertNotNull(socket       hannel, "SocketChannel may not be null.");

		Map<Object, Object> p       operties = channelContext.getChannelProperties();
		this.version = MapUtils.getString(pr       perties, AgentHandshakePropertyType.VERSION.getName());
		AssertUtils.assertTrue(!String       tils.isBlank(version), "Version may not be null or empty.");

		this.applicationName = MapUtils.getString       properties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
		AssertUtils.assertTrue(!StringUtil       .isBlank(applicationName), "ApplicationName may not be null or empty.");

		this.agentId         MapUtils.getString(properties, AgentHandshakePropertyType.AGENT_ID.getName());
		Assert       tils.assertTrue(!StringUtils.isBlank(agentId), "AgentId may not be null or empty.");

		this.startTim       Stamp = MapUtils.getLong(properties, AgentHandshakePropertyType.START_TIMESTAMP.getN        e());
	    AssertUtils.assertTrue(startT       meStamp > 0, "StartTimeStamp        s must     reater than zero.");
	}

	@Overrid
	public void send(byte[] data) {
		socket        annel.s    ndMessage(data);
	}

	@Override
	p       blic Future request        yte[] d    ta) {
		return socketChann       l.sendReque        Message(data);
	}

	@Override
	       ublic String getAp        ication    ame() {
		return applicati       nName;
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
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        
        result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        r    sult =     rime * result + (int) (startTimeS       amp ^ (startTi          eStamp             >>> 32));
        result = prime * result + ((          ersion              = null) ? 0 : version.hashCode());
        return result;
    }
    
	@Override
	pub          ic boo             ean equa    s(Object obj) {
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
