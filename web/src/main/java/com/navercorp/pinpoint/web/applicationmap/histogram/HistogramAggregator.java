package com.navercorp.pinpoint.web.applicationmap.histogram;

import java.util.Collection;

public class HistogramAggregator {

    public static long countErrorInstances(Collection<? extends Histogram> histograms) {
        long errorInstances = 0;
        for (Histogram histogram : histograms) {
            if (histogram.getTotalErrorCount() > 0) {
                errorInstances++;
            }
        }
        return errorInstances;
    }

}