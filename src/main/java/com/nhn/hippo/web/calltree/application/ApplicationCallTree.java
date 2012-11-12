package com.nhn.hippo.web.calltree.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.hippo.web.vo.BusinessTransactions;
import com.profiler.common.bo.SpanBo;

/**
 * Call Tree
 * 
 * @author netspider
 */
public class ApplicationCallTree {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String PREFIX_CLIENT = "CLIENT:";

	private final Map<String, Application> applications = new HashMap<String, Application>();
	private final Map<String, String> spanIdToApplication = new HashMap<String, String>();
	private final Map<String, ApplicationRequest> applicationRequests = new HashMap<String, ApplicationRequest>();

	private final List<SpanBo> spans = new ArrayList<SpanBo>();
	private final BusinessTransactions businessTransactions = new BusinessTransactions();

	private boolean isBuilt = false;

	public void addSpan(SpanBo span) {
		Application app = new Application(span.getAgentId(), span.getServiceName(), span.getEndPoint(), span.isTerminal());

		if (app.getId() == null) {
			return;
		}

		if (!applications.containsKey(app.getId())) {
			applications.put(app.getId(), app);
		}
		spanIdToApplication.put(String.valueOf(span.getSpanId()), app.getId());

		if (span.getParentSpanId() == -1) {
			businessTransactions.add(span);
		} else {
			spans.add(span);
		}
	}

	public ApplicationCallTree build() {
		if (isBuilt)
			return this;

		int i = 0;
		for (Entry<String, Application> entry : applications.entrySet()) {
			entry.getValue().setSequence(i++);
		}

		for (SpanBo span : spans) {
			String from = String.valueOf(span.getParentSpanId());
			String to = String.valueOf(span.getSpanId());

			Application fromServer = applications.get(spanIdToApplication.get(from));
			Application toServer = applications.get(spanIdToApplication.get(to));

			if (fromServer == null) {
				fromServer = applications.get(spanIdToApplication.get(PREFIX_CLIENT + to));
			}

			// TODO 없는 url에 대한 호출이 고려되어야 함. 일단 임시로 회피.
			if (fromServer == null) {
				logger.debug("invalid form server {}", from);
				continue;
			}
			ApplicationRequest serverRequest = new ApplicationRequest(fromServer, toServer);

			// TODO: local call인 경우 보여주지 않음.
			if (serverRequest.isSelfCalled()) {
				continue;
			}

			if (applicationRequests.containsKey(serverRequest.getId())) {
				applicationRequests.get(serverRequest.getId()).increaseCallCount();
			} else {
				applicationRequests.put(serverRequest.getId(), serverRequest);
			}
		}

		isBuilt = true;
		return this;
	}

	public Collection<Application> getNodes() {
		return this.applications.values();
	}

	public Collection<ApplicationRequest> getLinks() {
		return this.applicationRequests.values();
	}

	public BusinessTransactions getBusinessTransactions() {
		return businessTransactions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Server=").append(applications);
		sb.append("\n");
		sb.append("ServerRequest=").append(applicationRequests.values());

		return sb.toString();
	}
}
