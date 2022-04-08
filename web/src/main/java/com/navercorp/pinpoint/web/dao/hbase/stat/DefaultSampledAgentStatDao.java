package com.navercorp.pinpoint.web.dao.hbase.stat;

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

public class DefaultSampledAgentStatDao<IN extends AgentStatDataPoint, OUT extends SampledAgentStatDataPoint> implements SampledAgentStatDao<OUT> {
    private final AgentStatType statType;
    private final HbaseAgentStatDaoOperations operations;
    private final AgentStatDecoder<IN> decoder;
    private final SampledResultsExtractorSupplier<IN, OUT> resultExtractor;

    public DefaultSampledAgentStatDao(AgentStatType statType,
                                      HbaseAgentStatDaoOperations operations,
                                      AgentStatDecoder<IN> decoder,
                                      SampledResultsExtractorSupplier<IN, OUT> resultExtractor) {
        this.statType = Objects.requireNonNull(statType, "statType");
        this.operations = Objects.requireNonNull(operations, "operations");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.resultExtractor = Objects.requireNonNull(resultExtractor, "resultExtractor");
    }

    @Override
    public List<OUT> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<IN> mapper = operations.createRowMapper(decoder, range);

        ResultsExtractor<List<OUT>> resultExtractor = this.resultExtractor.apply(timeWindow, mapper);
        return operations.getSampledAgentStatList(statType, resultExtractor, agentId, range);
    }

    @Override
    public String getChartType() {
        return statType.getChartType();
    }
}
