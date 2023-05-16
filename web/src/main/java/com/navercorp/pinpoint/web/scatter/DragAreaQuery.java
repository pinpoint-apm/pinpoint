package com.navercorp.pinpoint.web.scatter;


import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.Objects;

public class DragAreaQuery {

    private final DragArea dragArea;
    // nullable
    private final String agentId;
    // nullable
    private final Dot.Status dotStatus;

    public DragAreaQuery(DragArea dragArea, String agentId, Dot.Status dotStatus) {
        this.dragArea = Objects.requireNonNull(dragArea, "dragArea");
        this.agentId = agentId;
        this.dotStatus = dotStatus;
    }

    public DragAreaQuery(DragArea dragArea) {
        this(dragArea, null, null);
    }

    public DragArea getDragArea() {
        return dragArea;
    }

    public String getAgentId() {
        return agentId;
    }

    public Dot.Status getDotStatus() {
        return dotStatus;
    }

    @Override
    public String toString() {
        return "DragAreaQuery{" +
                "dragArea=" + dragArea +
                ", agentId='" + agentId + '\'' +
                ", dotStatus=" + dotStatus +
                '}';
    }
}
