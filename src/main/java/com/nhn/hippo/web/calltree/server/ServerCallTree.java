package com.nhn.hippo.web.calltree.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.hippo.web.vo.ResponseHistogram;
import com.nhn.hippo.web.vo.TerminalStatistics;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 * Call Tree
 * 
 * @author netspider
 */
public class ServerCallTree {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// private final String PREFIX_CLIENT = "UNKNOWN-CLIENT:";

	private final Map<String, Server> servers = new HashMap<String, Server>();
	private final Map<String, ServerRequest> serverRequests = new HashMap<String, ServerRequest>();
	private final NodeIdGenerator idGenerator;
	private boolean isBuilt = false;

	// temporary variables
	private final List<SpanBo> spans = new ArrayList<SpanBo>();
	private final List<SubSpanBo> subspans = new ArrayList<SubSpanBo>();
	private final Map<String, String> spanIdToServerId = new HashMap<String, String>();
	private final Map<String, String> spanIdToClientId = new HashMap<String, String>();
	private final Map<String, TerminalStatistics> terminalRequests = new HashMap<String, TerminalStatistics>();

	public ServerCallTree(NodeIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public void addTerminalStatistics(TerminalStatistics terminal) {
		if (terminalRequests.containsKey(terminal.getId())) {
			TerminalStatistics req = terminalRequests.get(terminal.getId());
			req.mergeWith(terminal);
		} else {
			terminalRequests.put(terminal.getId(), terminal);
		}
	}

	private void addServer(String spanId, Server server) {
		if (!servers.containsKey(server.getId())) {
			servers.put(server.getId(), server);
		} else {
			servers.get(server.getId()).mergeWith(server);
		}
		spanIdToServerId.put(spanId, server.getId());
	}
	
	private void addClient(String spanId, Server server) {
		if (!servers.containsKey(server.getId())) {
			servers.put(server.getId(), server);
		} else {
			servers.get(server.getId()).mergeWith(server);
		}
		spanIdToClientId.put(spanId, server.getId());
	}

    public void addSubSpanList(List<SubSpanBo> subSpanBoList) {
        for (SubSpanBo subSpanBo : subSpanBoList) {
            this.addSubSpan(subSpanBo);
        }
    }

	public void addSubSpan(SubSpanBo span) {
		Server server = new Server(span, idGenerator);

		if (server.getId() == null) {
			return;
		}

		if (span.getServiceType().isRpcClient()) {
			addServer(span.getEndPoint(), server);
		} else {
			addServer(span.getServiceName(), server);
		}

		subspans.add(span);
	}

    public void addSpanList(List<SpanBo> spanList) {
        for (SpanBo spanBo : spanList) {
            addSpan(spanBo);
        }
    }

	public void addSpan(SpanBo span) {
		Server server = new Server(span, idGenerator);

		if (server.getId() == null) {
			return;
		}

		String spanId = String.valueOf(span.getSpanId());

		addServer(spanId, server);

		if (span.getParentSpanId() == -1) {
			// TODO client endpoint별로 node가 모두 생기니까 일단 임시로 application name을 사용함.
			// 여기에서 applicationname을 넣으면 여러 클라이언트를 보여줄 수 있지만 하나로 퉁친다...
			addClient(spanId, new Server("CLIENT" /*:" + NodeIdGenerator.BY_APPLICATION_NAME.makeServerId(span)*/, "CLIENT", "", ServiceType.CLIENT));
		}

		spans.add(span);
	}

	public ServerCallTree build() {
		if (isBuilt)
			return this;

		// add terminal to the servers
		for (Entry<String, TerminalStatistics> entry : terminalRequests.entrySet()) {
			TerminalStatistics terminal = entry.getValue();
			Server server = new Server(terminal.getTo(), terminal.getTo(), "UNKNOWN", ServiceType.findServiceType(terminal.getToServiceType()));
			servers.put(server.getId(), server);
		}

		// indexing server
		int i = 0;
		for (Entry<String, Server> entry : servers.entrySet()) {
			entry.getValue().setSequence(i++);
		}

		// add terminal requests
		for (Entry<String, TerminalStatistics> entry : terminalRequests.entrySet()) {
			TerminalStatistics terminal = entry.getValue();
			ServerRequest request = new ServerRequest(servers.get(terminal.getFrom()), servers.get(terminal.getTo()), terminal.getHistogram());
			serverRequests.put(request.getId(), request);
		}
		
		// add non-terminal requests (Span)
		for (SpanBo span : spans) {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanId());

			// span이 rootspan이면 client를 찾고 그렇지 않으면 서버를 찾는다.
			Server fromServer = servers.get((from.equals("-1")) ? spanIdToClientId.get(to) : spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

//			if (fromServer == null) {
//				fromServer = servers.get(spanIdToServerId.get(PREFIX_CLIENT + to));
//			}

			// TODO 없는 url에 대한 호출이 고려되어야 함. 일단 임시로 회피.
			if (fromServer == null) {
				logger.debug("invalid form server {}", from);
				continue;
			}
			
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer, new ResponseHistogram((from.equals("-1") ? ServiceType.CLIENT : span.getServiceType())));

			/*
			// TODO: local call인 경우 보여주지 않음.
			// server map v5 부터는 recursive call을 지원함.
			if (serverRequest.isSelfCalled()) {
				continue;
			}
			*/

			if (serverRequests.containsKey(serverRequest.getId())) {
				serverRequests.get(serverRequest.getId()).getHistogram().addSample(span.getElapsed());
			} else {
				serverRequest.getHistogram().addSample(span.getElapsed());
				serverRequests.put(serverRequest.getId(), serverRequest);
			}
		}

		// add terminal nodes
		for (SubSpanBo span : subspans) {
			String from = String.valueOf(span.getSpanId());
			String to;

			if (span.getServiceType().isRpcClient()) {
				// this is unknown cloud
				to = String.valueOf(span.getEndPoint());
			} else {
				to = String.valueOf(span.getServiceName());
			}

			Server fromServer = servers.get(spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

			ResponseHistogram histogram = new ResponseHistogram(span.getServiceType());
			histogram.addSample(span.getEndElapsed());
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer, histogram);

			if (serverRequests.containsKey(serverRequest.getId())) {
				serverRequests.get(serverRequest.getId()).getHistogram().addSample(span.getEndElapsed());
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ Servers=").append(servers).append(", ServerRequests=").append(serverRequests.values()).append(" }");
		return sb.toString();
	}
}
