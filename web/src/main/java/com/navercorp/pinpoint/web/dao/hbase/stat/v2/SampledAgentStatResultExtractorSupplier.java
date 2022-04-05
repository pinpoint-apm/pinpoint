package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.List;
import java.util.Objects;

public class SampledAgentStatResultExtractorSupplier<IN extends AgentStatDataPoint, OUT extends SampledAgentStatDataPoint>
        implements AbstractSampledAgentStatDao.ResultsExtractorSupplier<IN, OUT> {

    private final AgentStatSampler<IN, OUT> sampler;

    public SampledAgentStatResultExtractorSupplier(AgentStatSampler<IN, OUT> sampler) {
        this.sampler = Objects.requireNonNull(sampler, "sampler");
    }

    @Override
    public ResultsExtractor<List<OUT>> apply(TimeWindow timeWindow, AgentStatMapperV2<IN> mapper) {
        return new SampledAgentStatResultExtractor<>(timeWindow, mapper, sampler);
    }
}
