package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

import java.net.DatagramPacket;

public interface TraceIndex {

    void insert(Span span);

}
