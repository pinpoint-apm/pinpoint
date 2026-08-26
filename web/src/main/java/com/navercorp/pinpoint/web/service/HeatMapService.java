package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.common.server.uid.Service;

import java.util.List;

public interface HeatMapService {

    LimitedScanResult<List<DotMetaData>> dragTraceIndex(Service service, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, int limit);
}
