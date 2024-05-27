package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScatterDataMapView {
    private final Map<Application, ScatterData> dataMap;

    public ScatterDataMapView(Map<Application, ScatterData> dataMap) {
        this.dataMap = Objects.requireNonNull(dataMap, "dataMap");
    }

    public Map<String, ScatterDataView> getDataMap() {

        return dataMap.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> nodeName(e.getKey()), e -> scatterDataView(e.getValue())));
    }

    private String nodeName(Application application) {
        return NodeName.of(application).getName();
    }

    private ScatterDataView scatterDataView(ScatterData data) {
        return new ScatterDataView(data.getFrom(), data.getTo(), data.getOldestAcceptedTime(), data.getLatestAcceptedTime(), data);
    }

    public record ScatterDataView(long from, long to, long resultFrom, long resultTo, ScatterData scatter) {
    }
}
