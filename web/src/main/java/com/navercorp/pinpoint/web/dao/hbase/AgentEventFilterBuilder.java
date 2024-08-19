package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.service.component.AgentEventQuery;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Set;

public class AgentEventFilterBuilder {

    public AgentEventFilterBuilder() {

    }

    public Filter queryToFilter(AgentEventQuery query) {
        return switch (query.getQueryType()) {
            case INCLUDE -> includeFilter(query.getEventTypes());
            case EXCLUDE -> excludeFilter(query.getEventTypes());
            case ALL -> null;
        };
    }

    public Filter excludeFilter(Set<AgentEventType> excludeEventTypes) {
        if (CollectionUtils.isEmpty(excludeEventTypes)) {
            return null;
        }
        if (excludeEventTypes.size() == 1) {
            AgentEventType event = excludeEventTypes.iterator().next();
            return excludeQualifierFilter(event);
        }

        final FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        for (AgentEventType excludeEventType : excludeEventTypes) {
            Filter filter = excludeQualifierFilter(excludeEventType);
            filterList.addFilter(filter);
        }
        return filterList;
    }

    private Filter excludeQualifierFilter(AgentEventType excludeEventType) {
        byte[] excludeQualifier = Bytes.toBytes(excludeEventType.getCode());
        return new QualifierFilter(CompareOperator.NOT_EQUAL, new BinaryComparator(excludeQualifier));
    }

    public Filter includeFilter(Set<AgentEventType> includeEventTypes) {
        if (CollectionUtils.isEmpty(includeEventTypes)) {
            return null;
        }
        if (includeEventTypes.size() == 1) {
            AgentEventType event = includeEventTypes.iterator().next();
            return includeFilter(event);
        }

        final FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        for (AgentEventType excludeEventType : includeEventTypes) {
            Filter filter = includeFilter(excludeEventType);
            filterList.addFilter(filter);
        }
        return filterList;
    }

    private Filter includeFilter(AgentEventType excludeEventType) {
        byte[] excludeQualifier = Bytes.toBytes(excludeEventType.getCode());
        return new QualifierFilter(CompareOperator.EQUAL, new BinaryComparator(excludeQualifier));
    }

}
