package com.navercorp.pinpoint.grpc.channelz;


public interface ChannelzRegistry {

    void register(long logId, String serverName);

    Long getLogId(String serverName);

    String getServerName(long logId);

}
