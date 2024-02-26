
package com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.List;
import java.util.Objects;

public class MapResponseSimplifiedNodeHistogramDataSource implements WasNodeHistogramDataSource {

    private final MapResponseDao mapResponseDao;

    public MapResponseSimplifiedNodeHistogramDataSource(MapResponseDao mapResponseDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    @Override
    public NodeHistogram createNodeHistogram(Application application, Range range) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
        final NodeHistogram nodeHistogram = new NodeHistogram(application, range);
        nodeHistogram.setApplicationHistogram(responseTimes);
        nodeHistogram.setAgentHistogramMap(responseTimes);
        return nodeHistogram;
    }
}
