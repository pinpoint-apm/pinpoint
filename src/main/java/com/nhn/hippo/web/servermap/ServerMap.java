package com.nhn.hippo.web.servermap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.hippo.web.vo.TerminalStatistics;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class ServerMap {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String PREFIX_CLIENT = "UNKNOWN-CLIENT:";
	private final Nodes nodes = new Nodes();
	private final Links links = new Links();

	// temporary variables
	private final List<SpanBo> spans = new ArrayList<SpanBo>();
	private final List<SubSpanBo> subspans = new ArrayList<SubSpanBo>();
	private final Map<String, TerminalStatistics> terminalRequests = new HashMap<String, TerminalStatistics>();

	private boolean isBuilt = false;

	public void addTerminalRequest(TerminalStatistics terminal) {
		if (terminalRequests.containsKey(terminal.getId())) {
			TerminalStatistics req = terminalRequests.get(terminal.getId());
			req.mergeWith(terminal);
		} else {
			terminalRequests.put(terminal.getId(), terminal);
		}
	}

	public void addSubSpan(SubSpanBo span) {
		Node node = new Node(span);

		if (node.getId() == null) {
			return;
		}

		if (span.getServiceType().isRpcClient()) {
			nodes.addNode(span.getEndPoint(), node);
		} else {
			nodes.addNode(span.getServiceName(), node);
		}

		subspans.add(span);
	}

	public void addSpan(SpanBo span) {
		Node node = new Node(span);

		if (node.getId() == null) {
			return;
		}

		nodes.addNode(String.valueOf(span.getSpanId()), node);

		if (span.getParentSpanId() != -1) {
			spans.add(span);
		}
	}

	public ServerMap build() {
		if (isBuilt)
			return this;

		// add terminal to the nodes
		for (Entry<String, TerminalStatistics> entry : terminalRequests.entrySet()) {
			TerminalStatistics terminal = entry.getValue();
			Node node = new Node(terminal.getTo(), terminal.getTo(), "UNKNOWN", ServiceType.parse(terminal.getToServiceType()));
			nodes.addNode(node.getId(), node);
		}

		// indexing node
		int i = 0;
		for (Entry<String, Node> entry : nodes.entrySet()) {
			entry.getValue().setSequence(i++);
		}

		// add terminal requests
		for (Entry<String, TerminalStatistics> entry : terminalRequests.entrySet()) {
			TerminalStatistics terminal = entry.getValue();
			Link link = new Link(nodes.get(terminal.getFrom()), nodes.get(terminal.getTo()), terminal.getRequestCount());
			links.add(link);
		}

		// add non-terminal requests
		for (SpanBo span : spans) {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanId());

			Node fromServer = nodes.get(from);
			Node toServer = nodes.get(to);

			if (fromServer == null) {
				fromServer = nodes.get(PREFIX_CLIENT + to);

				// TODO 없는 url에 대한 호출이 고려되어야 함. 일단 임시로 회피.
				logger.debug("invalid form node {}", from);
				continue;
			}

			Link link = new Link(fromServer, toServer);

			// TODO: local call인 경우 보여주지 않음.
			if (link.isLocalCall()) {
				continue;
			}

			links.add(link, span.getElapsed());
		}

		// add terminal links
		for (SubSpanBo span : subspans) {
			String from = String.valueOf(span.getSpanId());
			String to;

			if (span.getServiceType().isRpcClient()) {
				// this is unknown cloud
				to = String.valueOf(span.getEndPoint());
			} else {
				to = String.valueOf(span.getServiceName());
			}

			Node fromServer = nodes.get(from);
			Node toServer = nodes.get(to);

			Link link = new Link(fromServer, toServer);

			links.add(link, span.getEndElapsed());
		}

		isBuilt = true;
		return this;
	}

	public Collection<Node> getNodes() {
		return this.nodes.values();
	}

	public Collection<Link> getLinks() {
		return this.links.values();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServerMap={\n\tServers=").append(nodes).append(",\n\tServerRequests=").append(links.values()).append("\n}");
		return sb.toString();
	}
}
