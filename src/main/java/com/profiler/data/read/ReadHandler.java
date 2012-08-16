package com.profiler.data.read;

import org.apache.thrift.TBase;

import java.net.DatagramPacket;

public interface ReadHandler {
    void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket);
}
