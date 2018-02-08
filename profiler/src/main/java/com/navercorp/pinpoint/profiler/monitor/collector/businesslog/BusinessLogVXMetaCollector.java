package com.navercorp.pinpoint.profiler.monitor.collector.businesslog;

import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;

/**
 * [XINGUANG]Created by Administrator on 2017/7/21.
 */
public interface BusinessLogVXMetaCollector <T extends TBase<?, ? extends TFieldIdEnum>> {

    List<T> collect();

    void initDailyLogLineMap();

    void saveLogMark();
}
