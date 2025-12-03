package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;

import java.util.List;

public interface HeatMapService {

    LimitedScanResult<List<DotMetaData>> dragScatterDataV2(String applicationName, DragAreaQuery dragAreaquery, int limit);

    LimitedScanResult<List<DotMetaData>> dragTraceIndex(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit);
}
