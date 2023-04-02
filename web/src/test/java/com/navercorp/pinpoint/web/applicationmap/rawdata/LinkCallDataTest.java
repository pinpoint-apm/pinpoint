package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkCallDataTest {

    private static final long ONE_MINUTE = 6000 * 10;
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long SIX_HOURS = TimeUnit.HOURS.toMillis(6);
    private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long TWO_DAY = TimeUnit.DAYS.toMillis(2);

    
    @Test
    public void addCallData() {
        LinkKey key = LinkKey.of("fromApplication", ServiceType.STAND_ALONE, "toApplication", ServiceType.STAND_ALONE);

        long currentTime = 1000;

        LinkCallData data1 = new LinkCallData(key);
        data1.addCallData(currentTime, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);

        assertThat(data1.getTimeHistogram()).hasSize(5);

        Range range = Range.between(currentTime, currentTime + SIX_HOURS);
        TimeWindow window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        LinkCallData data2 = new LinkCallData(key, window);
        data2.addCallData(currentTime, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);

        assertThat(data2.getTimeHistogram()).hasSize(1);

    }

}
