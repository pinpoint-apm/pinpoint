package com.navercorp.pinpoint.collector.rpc.handler;

import java.util.concurrent.Executor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DirectExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
