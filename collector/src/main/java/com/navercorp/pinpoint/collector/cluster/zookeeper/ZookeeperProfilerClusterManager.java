package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.ProfilerClusterPoint;
import com.nhn.pinpoint.collector.cluster.WorkerState;
import com.nhn.pinpoint.collector.cluster.WorkerStateContext;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.nhn.pinpoint.collector.receiver.tcp.AgentPropertiesType;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.nhn.pinpoint.rpc.server.SocketChannelStateChangeEventListener;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ZookeeperProfilerClusterManager implements SocketChannelStateChangeEventListener  {

	private static final Charset charset = Charset.forName("UTF-8");

	private static final String PROFILER_SEPERATOR = "\r\n";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ZookeeperClient client;
	private final ZookeeperLatestJobWorker worker;

	private final WorkerStateContext workerState;

	private final ProfilerClusterPoint clusterPoint;

	// 단순하게 하자 그냥 RUN이면 등록 FINISHED면 경우 삭제 그외 skip
	// 만약 상태가 안맞으면(?) 보정 들어가야 하는데 leak detector 같은걸 worker내부에 둘 까도 고민중 
	//
	// RUN_DUPLEX에서만 생성할수 있게 해야한다.
	// 지금은 RUN 상대방의 상태를 알수 없는 상태이기 때문에 이상황에서 등록
	public ZookeeperProfilerClusterManager(ZookeeperClient client, String serverIdentifier, ProfilerClusterPoint clusterPoint) {
		this.workerState = new WorkerStateContext();
		this.clusterPoint = clusterPoint;
		
		this.client = client;
		this.worker = new ZookeeperLatestJobWorker(client, serverIdentifier);
	}

	public void start() {
		switch (this.workerState.getCurrentState()) {
			case NEW:
				if (this.workerState.changeStateInitializing()) {
					logger.info("{} initialization started.", this.getClass().getSimpleName());
	
					if (worker != null) {
						worker.start();
					}
	
					workerState.changeStateStarted();
					logger.info("{} initialization completed.", this.getClass().getSimpleName());
					
					break;
				}
			case INITIALIZING:
				logger.info("{} already initializing.", this.getClass().getSimpleName());
				break;
			case STARTED:
				logger.info("{} already started.", this.getClass().getSimpleName());
				break;
			case DESTROYING:
				throw new IllegalStateException("Already destroying.");
			case STOPPED:
				throw new IllegalStateException("Already stopped.");
			case ILLEGAL_STATE:
				throw new IllegalStateException("Invalid State.");
		}		
	}
	
	public void stop() {
		if (!(this.workerState.changeStateDestroying())) {
			WorkerState state = this.workerState.getCurrentState();
			
			logger.info("{} already {}.", this.getClass().getSimpleName(), state.toString());
			return;
		}

		logger.info("{} destorying started.", this.getClass().getSimpleName());

		if (worker != null) {
			worker.stop();
		}

		this.workerState.changeStateStoped();
		logger.info("{} destorying completed.", this.getClass().getSimpleName());

		return;

	}
	
	@Override
	public void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
		if (workerState.isStarted()) {
			logger.info("eventPerformed ChannelContext={}, State={}", channelContext, stateCode);

			Map agentProperties = channelContext.getChannelProperties();
			
			// 현재는 AgentProperties에 값을 모를 경우 skip 
			if (skipAgent(agentProperties)) {
				return;
			}
			
			if (PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION == stateCode) {
				UpdateJob job = new UpdateJob(channelContext, new byte[0]);
				worker.putJob(job);
				
				clusterPoint.registerChannelContext(channelContext);
			} else if (PinpointServerSocketStateCode.isFinished(stateCode)) {
				DeleteJob job = new DeleteJob(channelContext);
				worker.putJob(job);
				
				clusterPoint.unregisterChannelContext(channelContext);
			} 
		} else {
			WorkerState state = this.workerState.getCurrentState();
			logger.info("{} invalid state {}.", this.getClass().getSimpleName(), state.toString());
			return;
		}
	}
	
	public List<String> getClusterData() {
		byte[] contents = worker.getClusterData();
		if (contents == null) {
			return Collections.emptyList();
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
		String applicationName = MapUtils.getString(agentProperties, AgentPropertiesType.APPLICATION_NAME.getName());
		String agentId = MapUtils.getString(agentProperties, AgentPropertiesType.AGENT_ID.getName());

		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId)) {
			return true;
		}

		return false;
	}

}
