package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.RawCallData;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentTimeSeriesHistogramBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;

    public AgentTimeSeriesHistogramBuilder(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;
        this.window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);
    }


    public AgentTimeSeriesHistogram build(List<ResponseTime> responseHistogramList) {
        Map<String, List<TimeHistogram>> agentLevelMap = new HashMap<String, List<TimeHistogram>>();
        for (ResponseTime responseTime : responseHistogramList) {
            Set<Map.Entry<String,Histogram>> agentHistogram = responseTime.getAgentHistogram();
            for (Map.Entry<String, Histogram> agentEntry : agentHistogram) {
                List<TimeHistogram> histogramList = agentLevelMap.get(agentEntry.getKey());
                if (histogramList == null) {
                    histogramList = new ArrayList<TimeHistogram>();
                    agentLevelMap.put(agentEntry.getKey(), histogramList);
                }
                Histogram histogram = agentEntry.getValue();

                TimeHistogram timeHistogram = new TimeHistogram(application.getServiceType(), responseTime.getTimeStamp());
                timeHistogram.add(histogram);
                histogramList.add(timeHistogram);
            }
        }

        Map<String, List<TimeHistogram>> histogramMap = interpolation(agentLevelMap);

        if (logger.isTraceEnabled()) {
            for (Map.Entry<String, List<TimeHistogram>> agentListEntry : agentLevelMap.entrySet()) {
                String agentName = agentListEntry.getKey();
                logger.trace("agentName:{}", agentName);
                List<TimeHistogram> value = agentListEntry.getValue();
                for (TimeHistogram histogram : value) {
                    logger.trace("histogram:{}", histogram);
                }
            }
        }
        AgentTimeSeriesHistogram agentTimeSeriesHistogram = new AgentTimeSeriesHistogram(application, range, histogramMap);
        return agentTimeSeriesHistogram;
    }

    public AgentTimeSeriesHistogram build(Collection<RawCallData> rawCallDataMap) {

        Map<String, List<TimeHistogram>> agentLevelMap = new HashMap<String, List<TimeHistogram>>();
        for (RawCallData rawCallData : rawCallDataMap) {
            List<TimeHistogram> sourceHistogramList = agentLevelMap.get(rawCallData.getSource());
            if (sourceHistogramList == null) {
                sourceHistogramList = new ArrayList<TimeHistogram>();
                logger.debug("-----------source:{}, target:{}", rawCallData.getSource(), rawCallData.getTarget());
                agentLevelMap.put(rawCallData.getSource(), sourceHistogramList);
            }
            // 주의 copy본이 아니라 원본 수정시 데이터가 틀릴수 있음.
            // interpolation에서 객체를 재 생성하므로 현재는 상관없음.
            sourceHistogramList.addAll(rawCallData.getTimeHistogram());
        }
        Map<String, List<TimeHistogram>> histogramMap = interpolation(agentLevelMap);
        AgentTimeSeriesHistogram agentTimeSeriesHistogram = new AgentTimeSeriesHistogram(application, range, histogramMap);
        return agentTimeSeriesHistogram;
    }

    private Map<String, List<TimeHistogram>> interpolation(Map<String, List<TimeHistogram>> agentLevelMap) {
        if (agentLevelMap.size() == 0) {
            return agentLevelMap;
        }
        Map<String, Map<Long, TimeHistogram>> windowTimeMap = new HashMap<String, Map<Long, TimeHistogram>>();
        // window 공간생성.
        // list로 할수도 있으나, filter일 경우 range를 초과하는 경우가 발생할 가능성이 있어 map으로 생성한다.
        // 좀더 나은 방인이 있으면 변경하는게 좋을듯.
        for (String key : agentLevelMap.keySet()) {
            Map<Long, TimeHistogram> value = new HashMap<Long, TimeHistogram>();
            for (Long time : window) {
                value.put(time, new TimeHistogram(application.getServiceType(), time));
            }
            windowTimeMap.put(key, value);
        }

        for (Map.Entry<String, List<TimeHistogram>> entry : agentLevelMap.entrySet()) {
            List<TimeHistogram> histogramList = entry.getValue();
            for (TimeHistogram timeHistogram : histogramList) {
                long time = window.refineTimestamp(timeHistogram.getTimeStamp());
//                int windowIndex = window.getWindowIndex(time);
                Map<Long, TimeHistogram> findSlot = windowTimeMap.get(entry.getKey());
                TimeHistogram windowHistogram = findSlot.get(time);
                if (windowHistogram == null) {
                    windowHistogram = new TimeHistogram(application.getServiceType(), time);
                    findSlot.put(time, windowHistogram);
                }
                windowHistogram.add(timeHistogram);
            }
        }

        Map<String, List<TimeHistogram>> result = new HashMap<String, List<TimeHistogram>>();
        for (Map.Entry<String, Map<Long, TimeHistogram>> windowMapEntry : windowTimeMap.entrySet()) {
            final String key = windowMapEntry.getKey();
            List<TimeHistogram> histogramList = result.get(key);
            if(histogramList == null) {
                histogramList = new ArrayList<TimeHistogram>();
                result.put(key, histogramList);
            }
            Map<Long, TimeHistogram> timeHistogramMap = windowMapEntry.getValue();
            for (TimeHistogram timeHistogram : timeHistogramMap.values()) {
                histogramList.add(timeHistogram);
            }
        }
        sortList(result);

        return result;
    }

    private void sortList(Map<String, List<TimeHistogram>> agentLevelMap) {
        Collection<List<TimeHistogram>> values = agentLevelMap.values();
        for (List<TimeHistogram> value : values) {
            Collections.sort(value, TimeHistogram.ASC_COMPARATOR);
        }
    }






    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
