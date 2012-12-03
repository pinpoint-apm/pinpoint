package com.nhn.hippo.web.calltree.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.TerminalRequest;
import com.profiler.common.ServiceType;
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
	private final Map<String, ServerRequest> serverRequests = new HashMap<String, ServerRequest>();
	private final List<SpanBo> spans = new ArrayList<SpanBo>();
	private final BusinessTransactions businessTransactions = new BusinessTransactions();

	private boolean isBuilt = false;

	private final Map<String, TerminalRequest> terminalRequests = new HashMap<String, TerminalRequest>();

	public void addTerminal(TerminalRequest terminal) {
		if (terminalRequests.containsKey(terminal.getId())) {
			TerminalRequest req = terminalRequests.get(terminal.getId());
			req.getStatistics().getHistogram().mergeWith(terminal.getStatistics().getHistogram());
		} else {
			terminalRequests.put(terminal.getId(), terminal);
		}
	}

	public void addSpan(SpanBo span) {
		/**
		 * make Servers
		 */
		// TODO: 여기에서 이러지말고 수집할 때 처음부터 table에 저장해둘 수 있나??
		Server server = new Server(span);

		if (server.getId() == null) {
			return;
		}

		// TODO: remove this later.
		// if (server.getId().contains("mysql:jdbc:") ||
		// server.getId().contains("favicon")) {
		// return;
		// }

		if (!servers.containsKey(server.getId())) {
			servers.put(server.getId(), server);
		} else {
			servers.get(server.getId()).mergeWith(server);
		}
		spanIdToServerId.put(String.valueOf(span.getSpanId()), server.getId());

		// TODO: remove client node
		// if (span.getParentSpanId() == -1) {
		// Server client = new Server(PREFIX_CLIENT + span.getAgentID(),
		// span.getEndPoint(), false);
		// servers.put(client.getId(), client);
		// spanIdToServerId.put(PREFIX_CLIENT + span.getSpanID(),
		// client.getId());
		// }

		/**
		 * Preparing makes link (ServerRequests)
		 */
		if (span.getParentSpanId() == -1) {
			businessTransactions.add(span);
		} else {
			spans.add(span);
		}
	}

	public ServerCallTree build() {
		if (isBuilt)
			return this;

		// add terminal servers
		for (Entry<String, TerminalRequest> entry : terminalRequests.entrySet()) {
			TerminalRequest terminal = entry.getValue();
			Server server = new Server(terminal.getTo(), terminal.getTo(), "UNKNOWN", ServiceType.parse(terminal.getToServiceType()), terminal.getStatistics().getAgentIds());
			servers.put(server.getId(), server);
		}

		// mark server index
		int i = 0;
		for (Entry<String, Server> entry : servers.entrySet()) {
			entry.getValue().setSequence(i++);
		}

		// add terminal requests
		for (Entry<String, TerminalRequest> entry : terminalRequests.entrySet()) {
			TerminalRequest terminal = entry.getValue();
			ServerRequest request = new ServerRequest(servers.get(terminal.getFrom()), servers.get(terminal.getTo()), terminal.getStatistics().getHistogram());
			serverRequests.put(request.getId(), request);
		}

		// add non-terminal requests
		for (SpanBo span : spans) {
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
				continue;
			}
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer);

			// TODO: local call인 경우 보여주지 않음.
			if (serverRequest.isSelfCalled()) {
				continue;
			}

			if (serverRequests.containsKey(serverRequest.getId())) {
				serverRequests.get(serverRequest.getId()).addRequest(span.getElapsed());
			} else {
				serverRequests.put(serverRequest.getId(), serverRequest);
			}
		}

		isBuilt = true;
		return this;
	}

	public Collection<Server> getNodes() {
		return this.servers.values();
	}

	public Collection<ServerRequest> getLinks() {
		return this.serverRequests.values();
	}

	public BusinessTransactions getBusinessTransactions() {
		return businessTransactions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Server=").append(servers);
		sb.append("\n");
		sb.append("ServerRequest=").append(serverRequests.values());

		return sb.toString();
	}
}
