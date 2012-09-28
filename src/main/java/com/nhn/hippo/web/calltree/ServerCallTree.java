package com.nhn.hippo.web.calltree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.profiler.common.dto.thrift.Span;

/**
 * Call Tree
 * 
 * @author netspider
 * 
 */
public class ServerCallTree {

	private final String PREFIX_CLIENT = "CLIENT:";

	private final Map<String, Server> servers = new HashMap<String, Server>();
	private final Map<String, String> spanIdToServerId = new HashMap<String, String>();
	private final Map<String, ServerRequest> ServerRequests = new HashMap<String, ServerRequest>();
	private final List<Span> spans = new ArrayList<Span>();

	private boolean isBuilt = false;

	public void addSpan(Span span) {
		System.out.println("Add span to calltree=" + span + "\n");

		/**
		 * make Servers
		 */
		// TODO: 여기에서 이러지말고 수집할 때 처음부터 table에 저장해둘 수 있나??
		Server Server = new Server(span.getAgentID(), span.getEndPoint());

		// TODO: remove this later.
		if (Server.getId().contains("mysql:jdbc:") || Server.getId().contains("favicon")) {
			return;
		}

		if (!servers.containsKey(Server.getId())) {
			servers.put(Server.getId(), Server);
		}
		spanIdToServerId.put(String.valueOf(span.getSpanID()), Server.getId());

		if (span.getParentSpanId() == -1) {
			Server client = new Server(PREFIX_CLIENT + span.getAgentID(), span.getEndPoint());
			servers.put(client.getId(), client);
			spanIdToServerId.put(PREFIX_CLIENT + span.getSpanID(), client.getId());
		}

		/**
		 * Preparing makes link (ServerRequests)
		 */
		spans.add(span);
	}

	public ServerCallTree build() {
		if (isBuilt)
			return this;

		int i = 0;
		for (Entry<String, Server> entry : servers.entrySet()) {
			entry.getValue().setSequence(i++);
		}

		for (Span span : spans) {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanID());

			Server fromServer = servers.get(spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

			if (fromServer == null) {
				fromServer = servers.get(spanIdToServerId.get(PREFIX_CLIENT + to));
			}

			ServerRequest ServerRequest = new ServerRequest(fromServer, toServer);
			if (ServerRequests.containsKey(ServerRequest.getId())) {
				ServerRequests.get(ServerRequest.getId()).increaseCallCount();
			} else {
				ServerRequests.put(ServerRequest.getId(), ServerRequest);
			}
		}

		isBuilt = true;
		return this;
	}

	public Collection<Server> getNodes() {
		return this.servers.values();
	}

	public Collection<ServerRequest> getLinks() {
		return this.ServerRequests.values();
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
