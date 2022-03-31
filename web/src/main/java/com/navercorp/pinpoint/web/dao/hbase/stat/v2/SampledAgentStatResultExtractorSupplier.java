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

public class SampledAgentStatResultExtractorSupplier<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint>
        implements AbstractSampledAgentStatDao.ResultsExtractorSupplier<T, S> {

    private final AgentStatSampler<T, S> sampler;

    public SampledAgentStatResultExtractorSupplier(AgentStatSampler<T, S> sampler) {
        this.sampler = Objects.requireNonNull(sampler, "sampler");
    }

    @Override
    public ResultsExtractor<List<S>> apply(TimeWindow timeWindow, AgentStatMapperV2<T> mapper) {
        return new SampledAgentStatResultExtractor<>(timeWindow, mapper, sampler);
    }
}
