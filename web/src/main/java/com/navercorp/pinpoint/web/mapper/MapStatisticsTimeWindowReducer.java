package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowReducer;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.util.TimeWindow;

public class MapStatisticsTimeWindowReducer implements RowReducer<LinkDataMap> {

    private final LinkDataMap result;

    public MapStatisticsTimeWindowReducer(TimeWindow timeWindow) {
        result = new LinkDataMap(timeWindow);
    }

    @Override
    public LinkDataMap reduce(LinkDataMap map) throws Exception {
        result.addLinkDataMap(map);
        return result;
    }
}