package com.navercorp.pinpoint.web.applicationmap.rawdata;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;

public class LinkCallDataTest {

    private static final long ONE_MINUTE = 6000 * 10;
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long SIX_HOURS = TimeUnit.HOURS.toMillis(6);
    private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long TWO_DAY = TimeUnit.DAYS.toMillis(2);

    
    @Test
    public void addCallData() {
        LinkKey key = new LinkKey("fromApplication", ServiceType.STAND_ALONE, "toApplication", ServiceType.STAND_ALONE);

        long currentTime = System.currentTimeMillis();
        
        LinkCallData data1 = new LinkCallData(key);
        data1.addCallData(currentTime, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data1.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        
        System.out.println(data1.getTimeHistogram().size());
        
        Range range = new Range(currentTime, currentTime + SIX_HOURS);
        TimeWindow window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        LinkCallData data2 = new LinkCallData(key, window);
        data2.addCallData(currentTime, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);
        data2.addCallData(currentTime + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE + ONE_MINUTE, (short) 100, 1L);

        
        System.out.println(data2.getTimeHistogram().size());

    }

}
