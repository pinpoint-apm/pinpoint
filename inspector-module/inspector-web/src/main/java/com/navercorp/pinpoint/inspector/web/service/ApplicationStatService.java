package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;

public interface ApplicationStatService {
    InspectorMetricData selectApplicationStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow);

    InspectorMetricGroupData selectApplicationStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow);
}
