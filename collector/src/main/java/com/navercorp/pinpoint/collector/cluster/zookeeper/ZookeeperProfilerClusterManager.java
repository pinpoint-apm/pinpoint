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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.ChannelContextClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.WorkerState;
import com.navercorp.pinpoint.collector.cluster.WorkerStateContext;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.navercorp.pinpoint.rpc.server.SocketChannelStateChangeEventListener;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ZookeeperProfilerClusterManager implements SocketChannelStateChangeEventListener  {

    private static final Charset charset = Charset.forName("UTF-8")

	private static final String PROFILER_SEPERATOR = "\r    n";

	private final Logger logger = LoggerFactory.getLogger(this.getCl    ss());

	private final ZookeeperLatestJobWork    r worker;

	private final WorkerStateContext    workerState;

	private final ClusterPointRepository    profileCluster;

	// keep it simple - register on RUN, remove on FINI    HED, skip otherwise
	// should only be instantiated w    en cluster is enabled.
	public ZookeeperProfilerClusterManager(ZookeeperClient client, String serverIdentifier, ClusterPointRe       ository profileCluster) {
		this.workerS       ate = new WorkerStateContext();
	             this.profileCluster = profileCluster;
		
		this.worker = new Z        keeperLatestJobWork       r(client, serverIdentifier);
	}

	public           oid             start() {
		switch (this.workerState.getC                rrentState()) {
			case NEW:
				if (this.workerState.changeState                   nitializin                   ())                                  {
					logger.                nfo("{} initialization started.", this.getClass().getSimpleName());
					if (worker != null) {
						worker.start();
					}
	
                      				wo             kerState.changeStateStarted();
					logger.info("{} initiali                      ation com             leted.", this.getClass().getSimpleName());

					b             eak;
				}
			case INITIALIZING:
				logger.          nfo("{} alrea             y initializing.", this.getClass().getSimpl                      Name());
		       	break;
			case STARTED:
				logger.info("{} a          ready started.", this.getClass().getSimpleName())
				break;
			case DESTROYING:
				throw new IllegalStateException("A          r             ady destroying.");
			case STOPPED:
				throw new IllegalStateExcep       ion("Already stop          ed.");
	             	case ILLEGAL_STATE:
				throw ne        IllegalStateException("Invalid State.");
		}		
	}
	
	public void sto          () {
	    if (!(this.workerState.changeStateDestroying())) {
			WorkerState state = this.workerState.getCurr       ntState();
			
			logger.i          fo("{} already {}.", this.getClass().getSimpleName(), state.toString());
			retu          n;
		}

		logger.info("{} destorying started.", this.g                   tClass().getSimpleName());

		if (worker           = null) {
			worker.stop();                                        		}

		this.workerState.changeStateStopped();
		logger.info             "{} destorying completed.", this.getClass().getSimp             eName());

	
	@Override
	public void eventPerformed(ChannelContext channel          ontext, PinpointServerSocketStateCode stateCode) {
		if (wo             kerState.isStarted()) {
			logger.info             "eventPerfor             ed ChannelContext={}, State={}", channelContext, stateCode);

			Map agent                r          perties = channelContext.getChannelProperties();

			// skip when applicationName and agentId is unknown 
			if (skipAgent(agent          r                perties)) {
				return;
			}
			
       		if (PinpointServerSocketStateCode.RU       _DUPLEX_COMMUNICATI          N == stateCode) {
				Upd             teJob job = new UpdateJob(channelContext,        ew byte[0]);
				worker.putJob(job);
				
				p       ofileCluster.addClusterPoint(new ChannelContextClusterPoint(       hannelContext));
			} else if (PinpointServ          rSocketStateCode.isFinished(stateCode)              {
				DeleteJob job                            =          new DeleteJob(channelContext);
				worker.putJob(job);

				       rofileCluster.removeClusterPoint(new Channel        ntextClusterPoint(channelContext));
			} 
		} else {
			Worker       tate state = this.workerState.getCurrentState();
			logger.info("{} invalid state {}.", this.getClass().getSimpl       Name(), state.toString());
			return;
		}
	}
	
	public List<String> getClusterData() {
		byte[] c       ntents = worker.getClusterData();
		if (contents == null) {
			return C          llecti             ns.empty    ist();
		}

		List<String> result = new ArrayList<String>();

		String clusterData = new String(contents, charset);
		String[] allClusterData = clusterData.split(PROFILER_SEPERATOR);
		for (String eachClusterData : allClusterData) {
			if (!StringUtils.isBlank(eachClusterData)) {
				result.add(eachClusterData);
			}
		}
		
		return result;
	}
	
	public List<ChannelContext> getRegisteredChannelContextList() {
		return worker.getRegisteredChannelContextList();
	}

	private boolean skipAgent(Map<Object, Object> agentProperties) {
		String applicationName = MapUtils.getString(agentProperties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
		String agentId = MapUtils.getString(agentProperties, AgentHandshakePropertyType.AGENT_ID.getName());

		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId)) {
			return true;
		}

		return false;
	}

}
