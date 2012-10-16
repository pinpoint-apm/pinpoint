package com.profiler.server.handler;

import org.apache.thrift.TBase;

import java.net.DatagramPacket;

public interface Handler {
    void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket);
}
