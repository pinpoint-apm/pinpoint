package com.profiler.server.dao;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;

public interface JvmInfo {
    void insert(JVMInfoThriftDTO jvmInfoThriftDTO, byte[] jvmInfoBytes);
}
