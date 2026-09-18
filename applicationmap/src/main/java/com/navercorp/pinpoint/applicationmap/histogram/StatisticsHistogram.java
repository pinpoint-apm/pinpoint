package com.navercorp.pinpoint.applicationmap.histogram;

public interface StatisticsHistogram {

    long getTotalCount();

    long getSumElapsed();

    long getMaxElapsed();

    long getAvgElapsed();

}
