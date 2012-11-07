package com.nhn.hippo.web.calltree.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.hippo.web.vo.BusinessTransactions;
import com.profiler.common.bo.SpanBo;

/**
 * Call Tree
 * 
 * @author netspider
 */
public class ServerCallTree {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String PREFIX_CLIENT = "CLIENT:";

	private final Map<String, Server> servers = new HashMap<String, Server>();
	private final Map<String, String> spanIdToServerId = new HashMap<String, String>();
	private final Map<String, ServerRequest> ServerRequests = new HashMap<String, ServerRequest>();
	private final BusinessTransactions businessTransactions = new BusinessTransactions();

	public void addSpan(SpanBo span) {
		Server server = new Server(span.getAgentId(), span.getApplicationName(), span.getEndPoint(), span.isTerminal());

		if (server.getId() == null) {
			return;
		}

		if (!servers.containsKey(server.getId())) {
			server.setSequence(servers.size());
			servers.put(server.getId(), server);
		}
		spanIdToServerId.put(String.valueOf(span.getSpanId()), server.getId());

		// TODO: remove client node
//		if (span.getParentSpanId() == -1) {
//			Server client = new Server(PREFIX_CLIENT + span.getAgentId(), span.getApplicationName(), span.getEndPoint(), false);
//			servers.put(client.getId(), client);
//			spanIdToServerId.put(PREFIX_CLIENT + span.getSpanId(), client.getId());
//		}

		/**
		 * Preparing makes link (ServerRequests)
		 */
		if (span.getParentSpanId() == -1) {
			businessTransactions.add(span);
		} else {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanId());

			Server fromServer = servers.get(spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

			if (fromServer == null) {
				fromServer = servers.get(spanIdToServerId.get(PREFIX_CLIENT + to));
			}

			// TODO 없는 url에 대한 호출이 고려되어야 함. 일단 임시로 회피.
			if (fromServer == null) {
				logger.debug("invalid form server {}", from);
				return;
			}
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer);

			// TODO: local call인 경우 보여주지 않음.
			if (serverRequest.isSelfCalled()) {
				return;
			}

			if (ServerRequests.containsKey(serverRequest.getId())) {
				ServerRequests.get(serverRequest.getId()).increaseCallCount();
			} else {
				ServerRequests.put(serverRequest.getId(), serverRequest);
			}
		}
	}

	public Collection<Server> getNodes() {
		return this.servers.values();
	}

	public Collection<ServerRequest> getLinks() {
		return this.ServerRequests.values();
	}

	public BusinessTransactions getBusinessTransactions() {
		return businessTransactions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Server=").append(servers);
		sb.append("\n");
		sb.append("ServerRequest=").append(ServerRequests.values());

		return sb.toString();
	}
}
