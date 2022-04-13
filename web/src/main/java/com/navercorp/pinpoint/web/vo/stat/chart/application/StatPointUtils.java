package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.IntApplicationStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.LongApplicationStatPoint;

public final class StatPointUtils {
    private StatPointUtils() {
    }

    public static ApplicationStatPoint<Long> toLongStatPoint(long timestamp, JoinLongFieldBo field) {
        return new LongApplicationStatPoint(timestamp, field.getMin(), field.getMinAgentId(),
                field.getMax(), field.getMaxAgentId(), field.getAvg());
    }

    public static ApplicationStatPoint<Integer> toIntStatPoint(long timestamp, JoinIntFieldBo field) {
        return new IntApplicationStatPoint(timestamp, field.getMin(), field.getMinAgentId(),
                field.getMax(), field.getMaxAgentId(), field.getAvg());
    }

    public static ApplicationStatPoint<Double> toDoubleStatPoint(long timestamp, JoinDoubleFieldBo field) {
        return new DoubleApplicationStatPoint(timestamp, field.getMin(), field.getMinAgentId(),
                field.getMax(), field.getMaxAgentId(), field.getAvg());
    }


    public static ApplicationStatPoint<Double> longToDoubleStatPoint(long timestamp, JoinLongFieldBo field) {
        return new DoubleApplicationStatPoint(timestamp, (double) field.getMin(), field.getMinAgentId(),
                (double) field.getMax(), field.getMaxAgentId(), (double) field.getAvg());
    }
}
