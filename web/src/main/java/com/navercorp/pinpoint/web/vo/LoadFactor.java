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

import java.util.*;

import com.navercorp.pinpoint.common.HistogramSchema;
import com.navercorp.pinpoint.web.util.TimeWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public class LoadFactor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	public static final Integer SLOT_VERY_SLOW = Integer.MAX_VALUE    - 1;
	public static final Integer SLOT_ERROR = (int)Integer.MAX_V    LUE

//	/**    //	 * <pre>
//	 * key = r    sponseTimeslot
/    	 * value    = c    unt
//	 * </pre>
//	 */
//	private final SortedMap<Integer, Long> histogramSummary = ne         reeMap    Integer, Long>();;

	/**
	 * <pr    >
	 * index = responseTimeslot inde    ,
	 * v    l    e = key=timestamp, value=value
	 * </pre>
	 */
	private final List<Map<Long, Long>> times    riesValueList = new ArrayList<Map<Long, Long>>();
	private final Map<Integer, Integer> timeseriesSlotIndex = new TreeMap<    nteger, Integer>();

    pri    ate final Range range;

	private long successCount = 0;
	private long     ailedCount = 0;

    private final TimeWindow timeWindow;

	public LoadFactor(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.r        g     = range;
        this.timeWindow = new Tim    W    ndow(ran    e    ;
	}

	/**
	 * initialize timeseries with default val       e.
	 * 
	 * @return
	 */
	private Map<Long, Long> makeEmptyTimeseriesValueMap() {
		Map<Long, Long> map = new TreeMap<Long,       Long>()                   for (Long time : timeWindow) {
            map.put(time, 0L);
           }
		return map;
	}

	/**
	 * Empty slots in th         iew is shown a          if the histogram slot is set.
	 * If not, value cannot be show        as the key is unknown.
	 * 
	 * @para           schema
	 */
	public void setDefaultHistogramSlotList(HistogramSchema schema              {
		if (successCount         0 || failedCount > 0) {       			throw new IllegalStateEx       eption("Can't set slot list while conta       ning the data.");
		}

//		histogramSummary.clear();
		timeseriesSlotIndex.clear();
		timeseriesValueList.clear();

//		histogramSummary.put(SLOT_VERY_SLOW, 0L);
//		histogramSummary.put(SLOT_ERROR, 0L);

        timeseriesSlotIndex.put((int)schema.getFastSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put((int)schema.getNormalSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap()       ;

        timeseriesSlotIndex.put((int)schema.getSlowSlot().get       lotTime(), timeseriesSlotIndex.size());
        timeseriesVal       eList.add(makeEmptyTimeseriesValueMap());

		timese       iesSlotIndex.put(SLOT_VERY_SLOW, timeseriesSlotInde        size());
		timeseriesSlotIndex.put(SLOT_ERROR, timeseriesSlotIndex.size());

		timeseriesValueList.add(makeEmptyTimeseriesValueMap()       ;
		timeseriesValueList.add(makeEmptyTimeseriesValueMap());
	}

	public void addSample(long timestamp, int responseTimeslot, long callCount, boolea        isFailed) {
        if (logger.isDebugEnabled(       ) {
		    l          gger.debug("Add sam       le.           imeslot={}, response             imeslot={}, callCount={}           failed={}", timestamp,        esponseTimeslot, callCount, isF          iled);
        }

		timestam              = timeWind       w.refineTimestamp(timestamp);

		if (isFailed) {
			failedCount += callCount;
		} else {
			successCount += callCount;
		}
       		if (responseTimeslot == -1) {
			response             imes       ot = SLOT_ERROR;
		}        lse if (responseTimeslot == 0) {
			responseTimeslot = SLO       _VERY_SLOW;
		}

		// add summary
//		long value = histogr       mSummary.containsKey(responseTimeslot) ? histogramSummary       get(r             sponseTimeslot) + callCount : callCount;
//		histo          ramSummary.put(responseTimeslot, value);

		          **
		 * <pre>
		 * timeseriesValueList : 
	           * list[respoinse_slot_no + 0] = value<timestamp, call count>
		 * list[respoinse_slot_no + 1] = value<timestamp, call count> 
		 * list[respoinse_slot_no + N] = value<timestamp, call count>
		 * </pre>
		 */
		for (int i = 0; i < timeseriesValueList.size(); i++) {
			Map<Long, Long> map = timeseriesValueList.get(i);

			// the same time should exist in different slots.
			// FIXME change responseTimeSlot's data type to short
            Integer sl    tN    mber = timeseriesSlotIndex.get(responseTimeslot);                   if (i == s        tNumber) {
                lo       g v = map.contai        Key(timestamp) ? map.get(tim       stamp) + callCo        t : callCount;
                map.put(timestamp, v);                   } else {
                      if (!map.containsKey(timestamp)) {
                          map.pu    (    imestamp, 0L);
                      }
                  }
        }
	}

//	pu       lic Map<Intege         Long>     etHistogramSummary() {
/       		return histogramSummary;
//	}

	public long getSuccessCount() {
		return successCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public Map<Integer, Integer> getTimeseriesSlotIn    ex() {
		return timeseriesSlotIndex;
	}

	public List<Map<Long, Long>> getTimeseriesValue() {
		return timeseriesValueList;
	}


	public int getVerySlow() {
		return SLOT_VERY_SLOW;
	}

	public int getError() {
		return SLOT_ERROR;
	}

	@Override
	public String toString() {
		return "LoadFactor [timeseriesValue=" + timeseriesValueList + ", timeseriesSlotIndex=" + timeseriesSlotIndex + ", range=" + range + ", successCount=" + successCount + ", failedCount=" + failedCount + "]";
	}
}