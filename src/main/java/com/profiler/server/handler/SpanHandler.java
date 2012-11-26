package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.server.dao.AgentIdApplicationIndex;
import com.profiler.server.dao.ApplicationTraceIndex;
import com.profiler.server.dao.RootTraceIndexDao;
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
	
	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			Span span = (Span) tbase;

			if (logger.isInfoEnabled()) {
				logger.info("Received SPAN=" + span);
			}

			String applicationName = agentIdApplicationIndexDao.selectApplicationName(span.getAgentId());
//			String applicationName = span.getServiceName();
			
			if (applicationName == null) {
				logger.info("Applicationname '{}' not found. Drop the log.", applicationName);
				return;
			} else {
				logger.info("Applicationname '{}' found. Write the log.", applicationName);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Found Applicationname={}", applicationName);
			}
			
			traceDao.insert(applicationName, span);
			if (span.getParentSpanId() == -1) {
				rootTraceIndexDao.insert(span);
			}
			traceIndexDao.insert(span);
			applicationTraceIndexDao.insert(applicationName, span);
		} catch (Exception e) {
			logger.warn("Span handle error " + e.getMessage(), e);
		}
	}
}
