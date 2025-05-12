package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AgentResponse {

    private final Application application;
    private final Map<Application, List<TimeHistogram>> map;

    AgentResponse(Application application, Map<Application, List<TimeHistogram>> map) {
        this.application = Objects.requireNonNull(application, "application");
        this.map = Objects.requireNonNull(map, "map");
    }

    public Application getApplication() {
        return application;
    }

    public Map<Application, List<TimeHistogram>> getAgentResponse() {
        return map;
    }

    public Histogram getAgentTotalHistogram(Application agentId) {
        Histogram histogram = new Histogram(agentId.getServiceType());
        List<TimeHistogram> timeHistograms = map.get(agentId);
        if (timeHistograms == null) {
            return histogram;
        }
        histogram.addAll(timeHistograms);
        return histogram;
    }

    public static Builder newBuilder(Application application) {
        return new Builder(application);
    }


    public static class Builder {
        private final Application application;
        private final Map<Application, List<TimeHistogram>> map = new HashMap<>();

        public Builder(Application application) {
            this.application = Objects.requireNonNull(application, "application");
        }

        public void addAgentResponse(List<ResponseTime> responseTimes) {
            final ServiceType serviceType = application.getServiceType();

            final int slotSize = responseTimes.size();
            for (ResponseTime responseTime : responseTimes) {
                Set<Map.Entry<String, TimeHistogram>> entry = responseTime.getAgentHistogram();
                for (Map.Entry<String, TimeHistogram> agentEntry : entry) {
                    String agentId = agentEntry.getKey();
                    TimeHistogram timeHistogram = agentEntry.getValue();

                    Application agent = new Application(agentId, serviceType);
                    List<TimeHistogram> timeHistograms = map.computeIfAbsent(agent, k -> new ArrayList<>(slotSize));
                    timeHistograms.add(timeHistogram);
                }
            }
        }

        public AgentResponse build() {
            for (Map.Entry<Application, List<TimeHistogram>> entry : map.entrySet()) {
                List<TimeHistogram> histogramList = entry.getValue();
                histogramList.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
            }
            return new AgentResponse(application, map);
        }
    }
}
