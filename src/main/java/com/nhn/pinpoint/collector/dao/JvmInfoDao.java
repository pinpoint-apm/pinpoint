package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TJVMInfoThriftDTO;

public interface JvmInfoDao {
    void insert(TJVMInfoThriftDTO jvmInfoThriftDTO, byte[] jvmInfoBytes);
}
