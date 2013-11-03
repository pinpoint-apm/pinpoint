package com.nhn.pinpoint.web.calltree.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.vo.ClientStatistics;
import com.nhn.pinpoint.web.vo.ResponseHistogram;
import com.nhn.pinpoint.web.vo.TerminalStatistics;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * Call Tree
 * 
 * @author netspider
 */
@Deprecated
public class ServerCallTree {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, Server> servers = new HashMap<String, Server>();
	private final Map<String, ServerRequest> serverRequests = new HashMap<String, ServerRequest>();
	private final NodeSelector nodeSelector;
	private boolean isBuilt = false;

	// temporary variables
	private final List<SpanBo> spanList = new ArrayList<SpanBo>();
	private final List<SpanEventBo> spanEventBoList = new ArrayList<SpanEventBo>();
	private final Map<String, String> spanIdToServerId = new HashMap<String, String>();
	private final Map<String, String> clientServerMap = new HashMap<String, String>();
	private final Map<String, TerminalStatistics> terminalRequests = new HashMap<String, TerminalStatistics>();
	private final Map<String, ClientStatistics> clientRequests = new HashMap<String, ClientStatistics>();
	private final Map<String, Set<String>> applicationHosts = new HashMap<String, Set<String>>();
	
	public ServerCallTree(NodeSelector nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public void addClientStatistics(ClientStatistics client) {
		if (clientRequests.containsKey(client.getId())) {
			ClientStatistics req = clientRequests.get(client.getId());
			req.mergeWith(client);
			logger.debug("merge client statistics " + client);
		} else {
			clientRequests.put(client.getId(), client);
			logger.debug("create client statistics " + client);
		}
		
		clientServerMap.put(client.getTo(), client.getId());
		logger.debug("add clientservermap " + client.getTo() + " -> " + client.getId());
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
	
//	private void addClient(String spanId, Server server) {
//		if (!servers.containsKey(server.getTransactionSequence())) {
//			servers.put(server.getTransactionSequence(), server);
//		} else {
//			servers.get(server.getTransactionSequence()).mergeWith(server);
//		}
//		spanIdToClientId.put(spanId, server.getTransactionSequence());
//	}

    public void addSpanEventList(List<SpanEventBo> spanEventBoList) {
        for (SpanEventBo spanEventBo : spanEventBoList) {
            this.addSubSpan(spanEventBo);
        }
    }

	public void addSubSpan(SpanEventBo spanEventBo) {
		Server server = new Server(spanEventBo, nodeSelector);

		if (server.getId() == null) {
			return;
		}

       addServer(spanEventBo.getDestinationId(), server);

		spanEventBoList.add(spanEventBo);
	}

    public void addSpanList(List<SpanBo> spanList) {
        for (SpanBo spanBo : spanList) {
            addSpan(spanBo);
        }
    }

//	public void addClientSpan(SpanBo span) {
//		ClientStatistics stat = new ClientStatistics(nodeSelector.getServerId(span), span.getServiceType().getCode());
//		stat.getHistogram().addSample(span.getElapsed());
//		this.addClientStatistics(stat);
//	}
    
	public void addSpan(SpanBo span) {
		Server server = new Server(span, nodeSelector);

		if (server.getId() == null) {
			return;
		}

		String spanId = String.valueOf(span.getSpanId());

		addServer(spanId, server);

//		if (span.getParentSpanId() == -1) {
			// TODO client endpoint별로 node가 모두 생기니까 일단 임시로 application name을 사용함.
			// 여기에서 applicationname을 넣으면 여러 클라이언트를 보여줄 수 있지만 하나로 퉁친다...
//			addClient(spanId, new Server("CLIENT" /*:" + NodeIdGenerator.BY_APPLICATION_NAME.makeServerId(span)*/, "CLIENT", null, ServiceType.CLIENT));
//		}

		spanList.add(span);
	}
	
	public void addApplicationHosts(String applicationId, Set<String> hosts) {
		applicationHosts.put(applicationId, hosts);
	}

	public ServerCallTree build() {
		if (isBuilt)
			return this;

		// fill WAS hostnames.
		for (Entry<String, Server> entry : servers.entrySet()) {
			Server server = entry.getValue();
			server.setHosts(applicationHosts.get(server.getApplicationName()));
		}
		
		// add terminal to the servers
		for (Entry<String, TerminalStatistics> entry : terminalRequests.entrySet()) {
			TerminalStatistics terminal = entry.getValue();
			Server server = new Server(terminal.getTo(), terminal.getTo(), terminal.getHosts(), ServiceType.findServiceType(terminal.getToServiceType()));
			servers.put(server.getId(), server);
		}
		
		// add client to servers
		for (Entry<String, ClientStatistics> entry : clientRequests.entrySet()) {
			ClientStatistics client = entry.getValue();
			Server server = new Server(client.getId(), client.getTo(), null, ServiceType.USER);
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
		for (SpanBo span : spanList) {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanId());

			logger.debug("add non-terminal requests from=" + from + ", to=" + to);
			
			// span이 rootspan이면 client를 찾고 그렇지 않으면 서버를 찾는다.
			Server fromServer = servers.get((span.isRoot()) ? clientServerMap.get(span.getApplicationId()) : spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

//			if (fromServer == null) {
//				fromServer = servers.get(spanIdToServerId.get(PREFIX_CLIENT + to));
//			}

			// TODO 없는 url에 대한 호출이 고려되어야 함. 일단 임시로 회피.
			if (fromServer == null) {
				logger.debug("invalid fromServer {}", from);
				continue;
			}
			
			// TODO 클라이언트 별로 histogram slot을 세분화 해야하나 일단 CLIENT하나로 퉁침.
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer, new ResponseHistogram(span.isRoot() ? ServiceType.CLIENT : span.getServiceType()));

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
		for (SpanEventBo spanEventBo : spanEventBoList) {
			String from = String.valueOf(spanEventBo.getSpanId());
			String to;

//			if (span.getServiceType().isRpcClient()) {
//				// this is unknown cloud
//				to = String.valueOf(span.getEndPoint());
//			} else {
//				to = String.valueOf(span.getServiceName());
//			}
            to = spanEventBo.getDestinationId();

			Server fromServer = servers.get(spanIdToServerId.get(from));
			Server toServer = servers.get(spanIdToServerId.get(to));

			ResponseHistogram histogram = new ResponseHistogram(spanEventBo.getServiceType());
			histogram.addSample(spanEventBo.getEndElapsed());
			ServerRequest serverRequest = new ServerRequest(fromServer, toServer, histogram);

			if (serverRequests.containsKey(serverRequest.getId())) {
				serverRequests.get(serverRequest.getId()).getHistogram().addSample(spanEventBo.getEndElapsed());
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
