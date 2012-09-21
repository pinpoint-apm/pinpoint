package com.nhn.hippo.web.service;

import java.util.Iterator;
import java.util.Map;

public interface FlowChartService {

	public String[] selectAgentIds(String[] hosts);

	public Iterator<Map<String, Object>> selectTraces(String[] agentIds, long from, long to);
}
