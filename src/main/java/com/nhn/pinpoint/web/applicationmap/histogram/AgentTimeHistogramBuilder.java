package com.nhn.pinpoint.web.applicationmap.histogram;

import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentTimeHistogramBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;

    public AgentTimeHistogramBuilder(Application application, Range range) {
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


    public AgentTimeHistogram build(List<ResponseTime> responseHistogramList) {
        AgentHistogramList agentHistogramList = new AgentHistogramList();
        for (ResponseTime responseTime : responseHistogramList) {
            for (Map.Entry<String, TimeHistogram> agentEntry : responseTime.getAgentHistogram()) {
                TimeHistogram timeHistogram = agentEntry.getValue();
                agentHistogramList.addAgentHistogram(agentEntry.getKey(), application.getServiceType(), timeHistogram);
            }
        }

        Map<Application, List<TimeHistogram>> histogramMap = interpolation(agentHistogramList, window);

        if (logger.isTraceEnabled()) {
            for (AgentHistogram agentListEntry : agentHistogramList.getAgentHistogramList()) {
                Application agentName = agentListEntry.getAgentId();
                logger.trace("agentName:{}", agentName);
                Collection<TimeHistogram> value = agentListEntry.getTimeHistogram();
                for (TimeHistogram histogram : value) {
                    logger.trace("histogram:{}", histogram);
                }
            }
        }
        AgentTimeHistogram agentTimeHistogram = new AgentTimeHistogram(application, range, histogramMap);
        return agentTimeHistogram;
    }

    public AgentTimeHistogram buildSource(LinkCallDataMap linkCallDataMap) {
        if (linkCallDataMap == null) {
            throw new NullPointerException("linkCallDataMap must not be null");
        }
        return build(linkCallDataMap.getSourceList());
    }

    public AgentTimeHistogram buildTarget(LinkCallDataMap linkCallDataMap) {
        if (linkCallDataMap == null) {
            throw new NullPointerException("linkCallDataMap must not be null");
        }
        return build(linkCallDataMap.getTargetList());
    }


    private AgentTimeHistogram build(AgentHistogramList agentHistogramList) {

        Map<Application, List<TimeHistogram>> histogramMap = interpolation(agentHistogramList, window);
        AgentTimeHistogram agentTimeHistogram = new AgentTimeHistogram(application, range, histogramMap);
        return agentTimeHistogram;
    }


    private Map<Application, List<TimeHistogram>> interpolation(AgentHistogramList agentLevelMap, TimeWindow window) {
        if (agentLevelMap.size() == 0) {
            return Collections.emptyMap();
        }

        Map<Application, Map<Long, TimeHistogram>> windowTimeMap = new HashMap<Application, Map<Long, TimeHistogram>>();
        // window 공간생성.
        // list로 할수도 있으나, filter일 경우 range를 초과하는 경우가 발생할 가능성이 있어 map으로 생성한다.
        // 좀더 나은 방인이 있으면 변경하는게 좋을듯.
        for (AgentHistogram agentHistogram : agentLevelMap.getAgentHistogramList()) {
            Map<Long, TimeHistogram> timeMap = new HashMap<Long, TimeHistogram>();
            for (Long time : window) {
                timeMap.put(time, new TimeHistogram(application.getServiceType(), time));
            }
            windowTimeMap.put(agentHistogram.getAgentId(), timeMap);
        }

        for (AgentHistogram agentHistogram : agentLevelMap.getAgentHistogramList()) {
            Collection<TimeHistogram> histogramList = agentHistogram.getTimeHistogram();
            for (TimeHistogram timeHistogram : histogramList) {
                long time = window.refineTimestamp(timeHistogram.getTimeStamp());
//                int windowIndex = window.getWindowIndex(time);
                Map<Long, TimeHistogram> findSlot = windowTimeMap.get(agentHistogram.getAgentId());
                TimeHistogram windowHistogram = findSlot.get(time);
                if (windowHistogram == null) {
                    windowHistogram = new TimeHistogram(application.getServiceType(), time);
                    findSlot.put(time, windowHistogram);
                }
                windowHistogram.add(timeHistogram);
            }
        }

        Map<Application, List<TimeHistogram>> result = new HashMap<Application, List<TimeHistogram>>();
        for (Map.Entry<Application, Map<Long, TimeHistogram>> windowMapEntry : windowTimeMap.entrySet()) {
            final Application key = windowMapEntry.getKey();
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

    private void sortList(Map<Application, List<TimeHistogram>> agentLevelMap) {
        Collection<List<TimeHistogram>> values = agentLevelMap.values();
        for (List<TimeHistogram> value : values) {
            Collections.sort(value, TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
        }
    }



    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
