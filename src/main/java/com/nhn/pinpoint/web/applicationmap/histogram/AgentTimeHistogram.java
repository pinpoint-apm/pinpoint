package com.nhn.pinpoint.web.applicationmap.histogram;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModel;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentTimeHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;

    private final Map<String, List<TimeHistogram>> histogramMap;

    public AgentTimeHistogram(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;
        this.window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);
        this.histogramMap = Collections.emptyMap();
    }

    public AgentTimeHistogram(Application application, Range range, Map<String, List<TimeHistogram>> histogramMap) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (histogramMap == null) {
            throw new NullPointerException("histogramMap must not be null");
        }
        this.application = application;
        this.range = range;
        this.window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);
        this.histogramMap = histogramMap;
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
