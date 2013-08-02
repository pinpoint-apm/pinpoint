package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

/**
 *
 */
public interface SendEvent {

    void sendPacketN(Collection<Object> dtoList);

    void sendPacket(Object dto);
}
