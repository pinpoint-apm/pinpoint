package com.navercorp.pinpoint.uristat.web.view;

import com.google.common.primitives.Doubles;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.timeseries.array.DoubleArray;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UriStatValue implements TimeSeriesValueView {
    private static final double NULL = -1D;

    private final String fieldName;
    private final List<Double> values;

    public static List<TimeSeriesValueView> createChartValueList(TimeWindow timeWindow, List<UriStatChartValue> uriStats,
                                                                 List<String> fieldNames) {

        final int bucketSize = uriStats.get(0).getValues().size();
        List<TimeSeriesValueView> values = new ArrayList<>(bucketSize);
        for (int i = 0 ; i < bucketSize; i++) {
            final String fieldName = fieldNames.get(i);
            final double[] data = DoubleArray.newArray(timeWindow.getWindowRangeCount(), NULL);

            for (UriStatChartValue uriStat : uriStats) {
                int index = timeWindow.getWindowIndex(uriStat.getTimestamp());
                data[index] = uriStat.getValues().get(i);
            }
            values.add(new UriStatValue(fieldName, Doubles.asList(data)));
        }
        return values;
    }


    public UriStatValue(String fieldName, List<Double> uriStats) {
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.values = Objects.requireNonNull(uriStats, "uriStats");
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public List<String> getTags() {
        return Collections.emptyList();
    }

    @Override
    public List<Double> getValues() {
        return values;
    }
}
