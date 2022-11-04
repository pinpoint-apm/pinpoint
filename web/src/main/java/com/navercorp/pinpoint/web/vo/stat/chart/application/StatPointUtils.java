package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;

public final class StatPointUtils {
    private StatPointUtils() {
    }

    public static LongApplicationStatPoint toLongStatPoint(long timestamp, JoinLongFieldBo field) {
        return new LongApplicationStatPoint(timestamp, field);
    }

    public static IntApplicationStatPoint toIntStatPoint(long timestamp, JoinIntFieldBo field) {
        return new IntApplicationStatPoint(timestamp, field);
    }

    public static DoubleApplicationStatPoint toDoubleStatPoint(long timestamp, JoinDoubleFieldBo field) {
        return new DoubleApplicationStatPoint(timestamp, field);
    }


    public static DoubleApplicationStatPoint longToDoubleStatPoint(long timestamp, JoinLongFieldBo field) {
        JoinDoubleFieldBo doubleFieldBo = toDoubleFieldBo(field);
        return new DoubleApplicationStatPoint(timestamp, doubleFieldBo);
    }

    static JoinDoubleFieldBo toDoubleFieldBo(JoinLongFieldBo field) {
        return new JoinDoubleFieldBo((double) field.getAvg(), (double) field.getMin(), field.getMinAgentId(),
                (double) field.getMax(), field.getMaxAgentId());
    }
}
