package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.RowReducer;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;

public class LinkTimeWindowReducer implements RowReducer<LinkDataMap> {

    private final LinkDataMap result;

    public LinkTimeWindowReducer(TimeWindowFunction timeWindow) {
        result = new LinkDataMap(timeWindow);
    }

    @Override
    public LinkDataMap reduce(LinkDataMap map) throws Exception {
        result.addLinkDataMap(map);
        return result;
    }
}