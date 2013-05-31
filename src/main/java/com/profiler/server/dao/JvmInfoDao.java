package com.profiler.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.JVMInfoThriftDTO;

public interface JvmInfoDao {
    void insert(JVMInfoThriftDTO jvmInfoThriftDTO, byte[] jvmInfoBytes);
}
