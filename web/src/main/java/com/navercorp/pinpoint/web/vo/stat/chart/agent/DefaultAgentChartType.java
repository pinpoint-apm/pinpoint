package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.Objects;
import java.util.function.Function;

public class DefaultAgentChartType<T, P extends Point> implements StatChartGroup.AgentChartType {

    private final Function<T, P> function;

    DefaultAgentChartType(Function<T, P> function) {
        this.function = Objects.requireNonNull(function, "function");
    }

    public Function<T, P> getFunction() {
        return function;
    }
}
