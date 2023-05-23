package com.navercorp.pinpoint.common.profiler.message;

import java.util.function.BiConsumer;

/**
 * @author emeroad
 */
public interface EnhancedDataSender<REQ, RES> extends DataSender<REQ> {

    boolean request(REQ data);

    boolean request(REQ data, int retry);

    boolean request(REQ data, BiConsumer<RES, Throwable> listener);

}
