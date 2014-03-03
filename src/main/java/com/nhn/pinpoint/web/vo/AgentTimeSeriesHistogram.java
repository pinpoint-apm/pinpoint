package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModel;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentTimeSeriesHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;

    private Map<String, List<TimeHistogram>> histogramMap = Collections.emptyMap();

    public AgentTimeSeriesHistogram(Application application, Range range) {
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

    public void build(List<ResponseTime> responseHistogramList) {
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
        this.histogramMap = interpolation(agentLevelMap);

        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<TimeHistogram>> agentListEntry : agentLevelMap.entrySet()) {
                String agentName = agentListEntry.getKey();
                logger.debug("agentName:{}", agentName);
                List<TimeHistogram> value = agentListEntry.getValue();
                for (TimeHistogram histogram : value) {
                    logger.debug("histogram:{}", histogram);
                }
            }
        }

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

    public List<AgentResponseTimeViewModel> createViewModel() {
        final List<AgentResponseTimeViewModel> result = new ArrayList<AgentResponseTimeViewModel>();
        for (Map.Entry<String, List<TimeHistogram>> entry : histogramMap.entrySet()) {
            AgentResponseTimeViewModel model = createAgentResponseTimeViewModel(entry.getKey(), entry.getValue());
            result.add(model);
        }
        Collections.sort(result, new Comparator<AgentResponseTimeViewModel>() {
            @Override
            public int compare(AgentResponseTimeViewModel o1, AgentResponseTimeViewModel o2) {
                return o1.getAgentName().compareTo(o2.getAgentName());
            }
        });
        return result;
    }

    private AgentResponseTimeViewModel createAgentResponseTimeViewModel(String agentName, List<TimeHistogram> timeHistogramList) {
        List<ResponseTimeViewModel> responseTimeViewModel = createResponseTimeViewModel(timeHistogramList);
        AgentResponseTimeViewModel agentResponseTimeViewModel = new AgentResponseTimeViewModel(agentName, responseTimeViewModel);
        return agentResponseTimeViewModel;
    }

    public List<ResponseTimeViewModel> createResponseTimeViewModel(List<TimeHistogram> timeHistogramList) {
        final List<ResponseTimeViewModel> value = new ArrayList<ResponseTimeViewModel>(5);
        ServiceType serviceType = application.getServiceType();
        HistogramSchema schema = serviceType.getHistogramSchema();
        value.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(SlotType.FAST, timeHistogramList)));
        value.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(SlotType.NORMAL, timeHistogramList)));
        value.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(SlotType.SLOW, timeHistogramList)));
        value.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(SlotType.VERY_SLOW, timeHistogramList)));
        value.add(new ResponseTimeViewModel(schema.getErrorSlot().getSlotName(), getColumnValue(SlotType.ERROR, timeHistogramList)));
        return value;

    }

    public List<ResponseTimeViewModel.TimeCount> getColumnValue(SlotType slotType, List<TimeHistogram> timeHistogramList) {
        List<ResponseTimeViewModel.TimeCount> result = new ArrayList<ResponseTimeViewModel.TimeCount>(timeHistogramList.size());
        for (TimeHistogram timeHistogram : timeHistogramList) {
            result.add(new ResponseTimeViewModel.TimeCount(timeHistogram.getTimeStamp(), getCount(timeHistogram, slotType)));
        }
        return result;
    }

    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
