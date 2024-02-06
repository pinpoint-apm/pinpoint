package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;

public interface ApplicationStatService {
    InspectorMetricData selectApplicationStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow);

    InspectorMetricGroupData selectApplicationStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow);
}
