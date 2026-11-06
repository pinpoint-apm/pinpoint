package com.navercorp.pinpoint.web.applicationmap.view;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.common.server.bo.Application;

import java.util.Map;
import java.util.Objects;

@JsonSerialize(using = ScatterDataMapView.ScatterDataMapViewSerializer.class)
public class ScatterDataMapView {
    private final Map<Application, ScatterData> dataMap;

    public ScatterDataMapView(Map<Application, ScatterData> dataMap) {
        this.dataMap = Objects.requireNonNull(dataMap, "dataMap");
    }

    private Map<Application, ScatterData> getDataMap() {
        return dataMap;
    }

    private static String nodeName(Application application) {
        return NodeName.of(application).getName();
    }

    public record ScatterDataView(long from, long to, long resultFrom, long resultTo, ScatterData scatter) {
        public static ScatterDataView of(ScatterData scatterData) {
            return new ScatterDataView(
                    scatterData.getFrom(),
                    scatterData.getTo(),
                    scatterData.getOldestAcceptedTime(),
                    scatterData.getLatestAcceptedTime(),
                    scatterData);
        }
    }

    public static class ScatterDataMapViewSerializer extends ValueSerializer<ScatterDataMapView> {
        @Override
        public void serialize(ScatterDataMapView view, JsonGenerator gen, SerializationContext serializers) {
            Map<Application, ScatterData> dataMap = view.getDataMap();
            if (dataMap.isEmpty()) {
                gen.writeStartObject();
                gen.writeEndObject();
                return;
            }
            gen.writeStartObject();
            for (Map.Entry<Application, ScatterData> entry : dataMap.entrySet()) {
                gen.writeName(nodeName(entry.getKey()));
                gen.writePOJO(ScatterDataView.of(entry.getValue()));
            }
            gen.writeEndObject();
        }
    }
}
