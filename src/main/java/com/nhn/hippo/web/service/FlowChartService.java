package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.profiler.common.dto.thrift.Span;

public interface FlowChartService {

	public String[] selectAgentIds(String[] hosts);

	public List<byte[]> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to);

	public Map<byte[], List<Span>> selectTraces(List<byte[]> traceIds);
}
