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
        AgentHistogramList agentHistogramList = new AgentHistogramList(application, responseHistogramList);
        return build(agentHistogramList);
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
        AgentHistogramList histogramList = interpolation(agentHistogramList, window);
        AgentTimeHistogram agentTimeHistogram = new AgentTimeHistogram(application, range, histogramList);
        return agentTimeHistogram;
    }


    private AgentHistogramList interpolation(AgentHistogramList agentHistogramList, TimeWindow window) {
        if (agentHistogramList.size() == 0) {
            return new AgentHistogramList();
        }

        // window 공간생성. AgentHistogramList 사용이전에는 그냥 생짜 자료구조를 사용함.
        // list로 할수도 있으나, filter일 경우 range를 초과하는 경우가 발생할 가능성이 있어 map으로 생성한다.
        // 좀더 나은 방인이 있으면 변경하는게 좋을듯.
        final AgentHistogramList resultAgentHistogramList = new AgentHistogramList();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            List<TimeHistogram> timeHistogramList = new ArrayList<TimeHistogram>();
            for (Long time : window) {
                timeHistogramList.add(new TimeHistogram(application.getServiceType(), time));
            }
            resultAgentHistogramList.addTimeHistogram(agentHistogram.getAgentId(), timeHistogramList);
        }

        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            for (TimeHistogram timeHistogram : agentHistogram.getTimeHistogram()) {
                final Long time = window.refineTimestamp(timeHistogram.getTimeStamp());
                Application agentId = agentHistogram.getAgentId();
                TimeHistogram windowHistogram = new TimeHistogram(timeHistogram.getHistogramSchema(), time);
                windowHistogram.add(timeHistogram);
                resultAgentHistogramList.addTimeHistogram(agentId, windowHistogram);
            }
        }

        return resultAgentHistogramList;
    }


    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
