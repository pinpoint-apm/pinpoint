package com.navercorp.pinpoint.web.applicationmap.util;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

public class CancellableHistogramFactory implements NodeHistogramFactory {
    private final NodeHistogramFactory nodeHistogramFactory;
    private volatile boolean cancel;

    public CancellableHistogramFactory(NodeHistogramFactory nodeHistogramFactory) {
        this.nodeHistogramFactory = Objects.requireNonNull(nodeHistogramFactory, "nodeHistogramFactory");
    }

    public NodeHistogram createWasNodeHistogram(Application application, TimeWindow timeWindow) {
        if (isCancel()) {
            return nodeHistogramFactory.createEmptyNodeHistogram(application, timeWindow.getWindowRange());
        }
        return nodeHistogramFactory.createWasNodeHistogram(application, timeWindow);
    }

    @Override
    public NodeHistogram createTerminalNodeHistogram(Application terminalApplication, Range range, LinkList linkList) {
        return nodeHistogramFactory.createTerminalNodeHistogram(terminalApplication, range, linkList);
    }

    @Override
    public NodeHistogram createUserNodeHistogram(Application userApplication, Range range, LinkList linkList) {
        return nodeHistogramFactory.createUserNodeHistogram(userApplication, range, linkList);
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        return nodeHistogramFactory.createQueueNodeHistogram(queueApplication, range, linkList);
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return nodeHistogramFactory.createEmptyNodeHistogram(application, range);
    }

    private boolean isCancel() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }
}
