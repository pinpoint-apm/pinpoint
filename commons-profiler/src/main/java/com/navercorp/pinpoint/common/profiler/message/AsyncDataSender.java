package com.navercorp.pinpoint.common.profiler.message;

import java.util.concurrent.CompletableFuture;

/**
 * @author emeroad
 */
public interface AsyncDataSender<REQ, RES> extends DataSender<REQ> {

    CompletableFuture<RES> request(REQ data);
}
