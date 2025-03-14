package com.navercorp.pinpoint.inspector.collector.model.kafka;

import com.navercorp.pinpoint.common.model.SortKeyUtils;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.Objects;

public class AgentStatBuilder {
    private final String tenantId;
    private final StatDataPoint dataPoint;
    private final String sortKey;

    public AgentStatBuilder(String tenantId, StatDataPoint dataPoint) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.dataPoint = Objects.requireNonNull(dataPoint, "dataPoint");
        this.sortKey = SortKeyUtils.generateKeyForAgentStat(dataPoint);
    }

    public AgentStat build(AgentStatField fieldName, double fieldValue) {
        return build(fieldName, fieldValue, AgentStat.EMPTY_TAGS);
    }

    public AgentStat build(AgentStatField fieldName, double fieldValue, List<Tag> tags) {
        DataPoint point = dataPoint.getDataPoint();
        String metricName = dataPoint.getAgentStatType().getChartType();
        return new AgentStat(tenantId, sortKey,
                point.getApplicationName(),
                point.getAgentId(),
                metricName, fieldName.getFieldName(), fieldValue, point.getTimestamp(), tags);
    }
}
