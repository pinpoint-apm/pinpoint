package com.navercorp.pinpoint.web.dao.businesslog;

import java.util.List;

/**
 * [XINGUANG]Created by Administrator on 2017/6/14.
 */
public interface BusinessLogDao {
    List<String> getBusinessLog(String agentId,String transactionId,String spanId,long time);
}
