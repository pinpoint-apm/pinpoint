package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author emeroad
 */
public class RangeFactory {
    @Autowired
    private TimeSlot timeSlot;

    /**
     * 역방향 분단위 통계 ragne 를 생성한다.
     * @param range
     * @return
     */
    public Range createStatisticsRange(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        // hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
        // 단 key가 역으로 치환되어 있으므로 startTime에 -1을 해야함.
        final long startTime = timeSlot.getTimeSlot(range.getFrom()) - 1;
        final long endTime = timeSlot.getTimeSlot(range.getTo());
        return Range.createUncheckedRange(startTime, endTime);
    }

}
