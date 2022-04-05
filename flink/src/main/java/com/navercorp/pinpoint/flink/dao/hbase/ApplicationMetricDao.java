package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;

import java.util.List;

public interface ApplicationMetricDao<T extends JoinStatBo> {
    void insert(String id, long timestamp, List<T> joinStatBoList);

    void insert(JoinApplicationStatBo joinApplicationStatBo);
}
