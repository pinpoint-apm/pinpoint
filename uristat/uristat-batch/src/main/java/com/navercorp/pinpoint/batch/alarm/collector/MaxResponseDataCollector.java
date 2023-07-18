package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.batch.alarm.dao.UriStatDao;
import com.navercorp.pinpoint.batch.alarm.vo.UriStatQueryParams;
import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.concurrent.atomic.AtomicBoolean;

public class MaxResponseDataCollector implements PinotDataCollector<Long> {
    private final String tenantId;
    private final UriStatDao uriStatDao;
    private final AtomicBoolean init = new AtomicBoolean(false); // need to consider a race condition when checkers start simultaneously.

    public MaxResponseDataCollector(String tenantId, UriStatDao uriStatDao) {
        this.tenantId = tenantId;
        this.uriStatDao = uriStatDao;
    }

    @Override
    public Long collect(String serviceName, String applicationName, String targetUri, Range range) {
        if (init.get()) {
            return null;
        }

        UriStatQueryParams params = new UriStatQueryParams(tenantId, serviceName, applicationName, targetUri, range);
        Double retVal = uriStatDao.selectMaxResponse(params);
        return retVal.longValue();
    }
}
