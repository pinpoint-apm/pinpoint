package com.profiler.data.reader;

import org.apache.thrift.TBase;

import java.net.DatagramPacket;

public interface Reader {
    void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket);
}
