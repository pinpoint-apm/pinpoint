package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.List;
import java.util.Objects;

public abstract class AbstractSampledAgentStatDao<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> implements SampledAgentStatDao<S> {
    private final AgentStatType statType;
    private final HbaseAgentStatDaoOperationsV2 operations;
    private final AgentStatDecoder<T> decoder;
    private final ResultsExtractorSupplier<T, S> resultExtractor;

    public AbstractSampledAgentStatDao(AgentStatType statType,
                                       HbaseAgentStatDaoOperationsV2 operations,
                                       AgentStatDecoder<T> decoder,
                                       ResultsExtractorSupplier<T, S> resultExtractor) {
        this.statType = Objects.requireNonNull(statType, "statType");
        this.operations = Objects.requireNonNull(operations, "operations");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.resultExtractor = Objects.requireNonNull(resultExtractor, "resultExtractor");
    }

    @Override
    public List<S> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<T> mapper = operations.createRowMapper(decoder, range);

        ResultsExtractor<List<S>> resultExtractor = this.resultExtractor.apply(timeWindow, mapper);
        return operations.getSampledAgentStatList(statType, resultExtractor, agentId, range);
    }

    public interface ResultsExtractorSupplier<T extends AgentStatDataPoint, S>  {
        ResultsExtractor<List<S>> apply(TimeWindow timeWindow, AgentStatMapperV2<T> mapper);
    }
}
