package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Span;
import com.profiler.server.dao.AgentIdApplicationIndex;
import com.profiler.server.dao.ApplicationTraceIndex;
import com.profiler.server.dao.RootTraceIndexDao;
import com.profiler.server.dao.TerminalStatistics;
import com.profiler.server.dao.TraceIndex;
import com.profiler.server.dao.Traces;

public class SpanHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(SpanHandler.class.getName());

	@Autowired
	private TraceIndex traceIndexDao;

	@Autowired
	private Traces traceDao;

	@Autowired
	private RootTraceIndexDao rootTraceIndexDao;

	@Autowired
	private ApplicationTraceIndex applicationTraceIndexDao;

	@Autowired
	private AgentIdApplicationIndex agentIdApplicationIndexDao;
	
	@Autowired
	private TerminalStatistics terminalStatistics;

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			Span span = (Span) tbase;

			if (logger.isInfoEnabled()) {
				logger.info("Received SPAN=" + span);
			}

			String applicationName = agentIdApplicationIndexDao.selectApplicationName(span.getAgentId());

			if (applicationName == null) {
				logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
				return;
			} else {
				logger.info("Applicationname '{}' found. Write the log.", applicationName);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Found Applicationname={}", applicationName);
			}

			ServiceType serviceType = ServiceType.parse(span.getServiceType());

			// insert span
			if (serviceType.isTerminal()) {
				traceDao.insertTerminalSpan(applicationName, span);
				
				// if terminal update statistics
				terminalStatistics.update(applicationName, span.getServiceName(), serviceType.getCode());
			} else {
				traceDao.insert(applicationName, span);
			}
			
			// indexing root span
			if (span.getParentSpanId() == -1) {
				rootTraceIndexDao.insert(span);
			}

			// indexing non-terminal span
			if (serviceType.isIndexable()) {
				traceIndexDao.insert(span);
				applicationTraceIndexDao.insert(applicationName, span);
			} else {
				logger.debug("Skip writing index. '{}'", span);
			}
		} catch (Exception e) {
			logger.warn("Span handle error " + e.getMessage(), e);
		}
	}
}
