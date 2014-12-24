/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.TimeSlot;

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
