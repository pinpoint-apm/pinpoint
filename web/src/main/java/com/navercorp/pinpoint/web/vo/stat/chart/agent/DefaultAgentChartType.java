package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.Objects;
import java.util.function.Function;

public class DefaultAgentChartType<T, P extends Number> implements StatChartGroup.AgentChartType {

    private final Function<T, AgentStatPoint<P>> function;

    DefaultAgentChartType(Function<T, AgentStatPoint<P>> function) {
        this.function = Objects.requireNonNull(function, "function");
    }

    public Function<T, AgentStatPoint<P>> getFunction() {
        return function;
    }
}
