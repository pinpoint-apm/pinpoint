package com.navercorp.pinpoint.grpc.server;

import io.grpc.netty.InternalNettyServerBuilder;
import io.grpc.netty.NettyServerBuilder;

public class NettyStatsOptions
{
    public static void disableStats(NettyServerBuilder builder) {
        InternalNettyServerBuilder.setTracingEnabled(builder, false);
        InternalNettyServerBuilder.setStatsEnabled(builder, false);
        InternalNettyServerBuilder.setStatsRecordRealTimeMetrics(builder, false);
        InternalNettyServerBuilder.setStatsRecordStartedRpcs(builder, false);
    }
}
