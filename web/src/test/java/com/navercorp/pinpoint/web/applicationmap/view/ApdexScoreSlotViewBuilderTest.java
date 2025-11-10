package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.web.applicationmap.view.ApdexScoreSlotViewBuilder.MAX_SLOTS;
import static com.navercorp.pinpoint.web.applicationmap.view.ApdexScoreSlotViewBuilder.MIN_INTERVAL;

public class ApdexScoreSlotViewBuilderTest {

    private final long currentTimeMillis = System.currentTimeMillis();
    private final Application application = new Application("testApp", ServiceType.TEST);

    private List<TimeHistogram> createEmptyTimeHistogramList(Range range, ServiceType serviceType) {
        TimeWindow timeWindow = new TimeWindow(range);
        List<TimeHistogram> timeHistogramList = new ArrayList<>(timeWindow.getWindowRangeCount());
        for (long timeStamp : timeWindow) {
            timeHistogramList.add(new TimeHistogram(serviceType, timeStamp));
        }
        return timeHistogramList;
    }

    @Test
    public void minIntervalSlotTest() {
        final Range range = Range.between(currentTimeMillis, currentTimeMillis + 5 * MIN_INTERVAL);
        List<TimeHistogram> histogramList = createEmptyTimeHistogramList(range, application.getServiceType());

        List<Double> apdexScores = new ApdexScoreSlotViewBuilder(range, application, histogramList).build();

        Assertions.assertThat(apdexScores).hasSize(6); // 5 + 1 (inclusive end)
    }

    @Test
    public void maxSlotTest1() {
        final Range range = Range.between(currentTimeMillis, currentTimeMillis + (MAX_SLOTS + 1) * MIN_INTERVAL);
        List<TimeHistogram> histogramList = createEmptyTimeHistogramList(range, application.getServiceType());

        List<Double> apdexScores = new ApdexScoreSlotViewBuilder(range, application, histogramList).build();

        Assertions.assertThat(apdexScores).hasSize(MAX_SLOTS);
    }

    @Test
    public void maxSlotTest2() {
        final Range range = Range.between(currentTimeMillis, currentTimeMillis + (MAX_SLOTS + 1) * 13 * MIN_INTERVAL);
        List<TimeHistogram> histogramList = createEmptyTimeHistogramList(range, application.getServiceType());

        List<Double> apdexScores1 = new ApdexScoreSlotViewBuilder(range, application, histogramList).build();

        Assertions.assertThat(apdexScores1).hasSize(MAX_SLOTS);
    }

    @Test
    public void emptyHistogramTest() {
        final Range range = Range.between(currentTimeMillis, currentTimeMillis + (MAX_SLOTS + 1) * MIN_INTERVAL);
        List<TimeHistogram> histogramList = createEmptyTimeHistogramList(range, application.getServiceType());

        ApdexScoreSlotViewBuilder builder = new ApdexScoreSlotViewBuilder(range, application, histogramList);
        List<Double> apdexScores = builder.build();

        Assertions.assertThat(apdexScores).hasSize(MAX_SLOTS);
        Assertions.assertThat(apdexScores).allMatch(score -> score < 0); // All scores should be UNCOLLECTED_VALUE (-1.0)
    }
}
