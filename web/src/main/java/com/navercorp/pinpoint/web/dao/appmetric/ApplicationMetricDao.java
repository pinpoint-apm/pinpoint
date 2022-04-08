package com.navercorp.pinpoint.web.dao.appmetric;

import com.navercorp.pinpoint.web.service.stat.ChartTypeSupport;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import java.util.List;

public interface ApplicationMetricDao<T extends AggregationStatData> extends ChartTypeSupport {
    List<T> getApplicationStatList(String applicationId, TimeWindow timeWindow);
}
