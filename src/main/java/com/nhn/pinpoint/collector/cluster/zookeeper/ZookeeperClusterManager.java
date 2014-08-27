package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.nhn.pinpoint.collector.receiver.tcp.AgentPropertiesType;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.nhn.pinpoint.rpc.server.SocketChannelStateChangeEventListener;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ZookeeperClusterManager implements SocketChannelStateChangeEventListener  {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final InetAddress localHost;
	
	private final ZookeeperClient client;
	private final ZookeeperLatestJobWorker worker;

	private final ObjectMapper objectmapper = new ObjectMapper();


	// 단순하게 하자 그냥 RUN이면 등록 FINISHED면 경우 삭제 그외 skip
	// 만약 상태가 안맞으면(?) 보정 들어가야 하는데 leack detector 같은걸 worker내부에 둘 까도 고민중 
	//
	// RUN에서만 생성할수 있게 해야한다.
	// 지금은 RUN_WITHOUT_REGISTER면 상대방의 상태를 알수 없는 상태이기 때문에 이상황에서 등록
	
	public ZookeeperClusterManager(String hostPort, int sessionTimeout) throws KeeperException, IOException, InterruptedException {
		// 디폴트값으로 사용할 ip같은거 지정해 주는게 좋을듯
		this.localHost = InetAddress.getLocalHost();
		
		this.client = new ZookeeperClient(hostPort, sessionTimeout, new ClusterManagerWatcher());
		this.worker = new ZookeeperLatestJobWorker(client);
		worker.start();
	}

	@Override
	public void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
		logger.info("eventPerformed ChannelContext={}, State={}", channelContext, stateCode);

		Map agentProperties = channelContext.getChannelProperties();
		
		// 현재는 AgentProperties에 값을 모를 경우 skip 
		if (skipAgent(agentProperties)) {
			return;
		}
		
		if (PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION == stateCode) {
			byte[] contents = serializeContents(agentProperties, stateCode);
			if (contents == null) {
				return;
			}
			
			UpdateJob job = new UpdateJob(channelContext, contents);
			worker.putJob(job);
		} else if (PinpointServerSocketStateCode.isFinished(stateCode)) {
			DeleteJob job = new DeleteJob(channelContext);
			worker.putJob(job);
		} 
	}
	
	public Map getData(ChannelContext channelContext) {
		byte[] contents = worker.getData(channelContext);
		
		if (contents == null) {
			return Collections.EMPTY_MAP;
		}
		
		return deserializeContents(contents);
	}

	private boolean skipAgent(Map<Object, Object> agentProperties) {
		String applicationName = MapUtils.getString(agentProperties, AgentPropertiesType.APPLICATION_NAME.getName());
		String agentId = MapUtils.getString(agentProperties, AgentPropertiesType.AGENT_ID.getName());

		if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(agentId)) {
			return true;
		}

		return false;
	}
	
	private byte[] serializeContents(Map agentProperties, PinpointServerSocketStateCode state) {
		Map<Object, Object> contents = new HashMap<Object, Object>();
		contents.put(localHost.getHostAddress(), agentProperties);
		contents.put("state", state.name());
		
		try {
			return objectmapper.writeValueAsBytes(contents);
		} catch (JsonProcessingException e) {
			logger.warn(e.getMessage(), e);
		}
		
		return null;
	}

	private Map deserializeContents(byte[] contents) {
		try {
			return objectmapper.readValue(contents, Map.class);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		return Collections.EMPTY_MAP;
	}
	
	public void close() {
		client.close();
		worker.stop();
	}

	// 쥬키퍼의 모든 노드를 EPHEMERAL 형태로 만들기 때문에 연결이 새로 연결되면 원래 가지고 있던 것들 재등록 함 
	// 연결이 종료 될때는 별다른 액션을 취하지 않는다. 
	class ClusterManagerWatcher implements ZookeeperEventWatcher {

		private final AtomicBoolean connected = new AtomicBoolean(false);

		// 여기서 고민을 좀 해봐야 할듯
		// 이전에 있던 job을 다 지운다. 어떻게 지울까? 
		// 새로 생긴 job을 모두 새로 만든다 어떻게 만들지? 추가적으로 이걸 지워질수는 없나? 
		// 타이밍이 안좋아서 문제가 생길수 있나? 
		// 
		
		@Override
		public void process(WatchedEvent event) {
			KeeperState state = event.getState();

			// 상태가 되면 ephemeral 노드가 사라짐
			// 문서에 따라 자동으로 연결이 되고, 연결되는 이벤트는 process에서 감지가 됨
			if (state == KeeperState.Disconnected || state == KeeperState.Expired) {
				connected.compareAndSet(true, false);
				return;
			}

			if (state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) {
				// 이전상태가 RUN일수 있기 때문에 유지해도 됨
				boolean changed = connected.compareAndSet(false, true);
				if (changed) {
					// 여기서 데이터가 있으면 다 넣어줘야함
					List<ChannelContext> currentChannelContextList = worker.getRegisteredChannelContextList();
					
					for (ChannelContext channelContext : currentChannelContextList) {
						eventPerformed(channelContext, channelContext.getCurrentStateCode());
					}
				}

				return;
			}
		}

		@Override
		public boolean isConnected() {
			return connected.get();
		}

	}
	
}
