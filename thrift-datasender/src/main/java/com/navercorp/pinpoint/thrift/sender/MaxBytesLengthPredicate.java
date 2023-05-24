package com.navercorp.pinpoint.thrift.sender;

import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.BiPredicate;

public class MaxBytesLengthPredicate<M> implements BiPredicate<byte[], M> {

    private final Logger logger;
    private final int maxPacketLength;

    public MaxBytesLengthPredicate(Logger logger, int maxPacketLength) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.maxPacketLength = maxPacketLength;
    }

    @Override
    public boolean test(byte[] bytes, M message) {
        // do not copy bytes because it's single threaded
        if (isLimit(bytes.length)) {
            // When packet size is greater than UDP packet size limit, it's better to discard packet than let the socket API fails.
            logger.info("discard packet. Caused:too large message. size:{}, {}", bytes.length, message);
            return false;
        }
        return true;
    }


    private boolean isLimit(int interBufferSize) {
        return interBufferSize > maxPacketLength;
    }
}
