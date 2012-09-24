package com.profiler.server.data.handler;

import org.apache.thrift.TBase;

import java.net.DatagramPacket;

public interface Handler {
    void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket);
}
