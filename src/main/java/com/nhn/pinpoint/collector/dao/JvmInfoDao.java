package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.JVMInfoThriftDTO;

public interface JvmInfoDao {
    void insert(JVMInfoThriftDTO jvmInfoThriftDTO, byte[] jvmInfoBytes);
}
