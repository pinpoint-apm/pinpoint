package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMap;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

public interface HeatMapService {

    LimitedScanResult<List<SpanBo>> dragScatterData(String applicationName, DragAreaQuery dragAreaquery, int limit);

    LimitedScanResult<HeatMap> getHeatMap(String applicationName, Range range, long maxY, int limit);
}
