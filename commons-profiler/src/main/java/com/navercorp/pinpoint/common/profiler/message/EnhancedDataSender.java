package com.navercorp.pinpoint.common.profiler.message;

/**
 * @author emeroad
 */
public interface EnhancedDataSender<REQ> extends DataSender<REQ> {

    boolean request(REQ data);

    boolean request(REQ data, int retry);

}
