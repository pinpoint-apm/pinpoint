package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.RowReducer;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;

public class MapStatisticsTimeWindowReducer implements RowReducer<LinkDataMap> {

    private final LinkDataMap result;

    public MapStatisticsTimeWindowReducer(TimeWindowFunction timeWindow) {
        result = new LinkDataMap(timeWindow);
    }

    @Override
    public LinkDataMap reduce(LinkDataMap map) throws Exception {
        result.addLinkDataMap(map);
        return result;
    }
}