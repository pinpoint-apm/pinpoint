package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface AgentStatSampler<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> {

    List<S> sampleDataPoints(TimeWindow timeWindow, List<T> dataPoints);
}
