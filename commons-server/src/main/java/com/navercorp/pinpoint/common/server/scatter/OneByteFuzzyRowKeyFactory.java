package com.navercorp.pinpoint.common.server.scatter;


import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OneByteFuzzyRowKeyFactory implements FuzzyRowKeyFactory<Byte> {

    final long[] slot = allocateSlot();
    private static final byte MAX_SLOT = 32;
    private static final long MAX_SLOT_TIME = TimeUnit.MINUTES.toMillis(5);

    private long[] allocateSlot() {
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(100, 2);
        exponentialBackOff.setMaxInterval(TimeUnit.MINUTES.toMillis(MAX_SLOT_TIME * 2));
        BackOffExecution backOffExecution = exponentialBackOff.start();
        final List<Long> backOffTimeList = new ArrayList<>();
        for (int i = 0; i < MAX_SLOT; i++) {
            final long nextBackOff = backOffExecution.nextBackOff();
            backOffTimeList.add(nextBackOff);
            if (MAX_SLOT_TIME <= nextBackOff) {
                break;
            }
        }
        return toLongArray(backOffTimeList);
    }

    private long[] toLongArray(List<Long> list) {
        long[] buffer = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            long time = list.get(i);
            buffer[i] = time;
        }
        return buffer;
    }

    @Override
    public Byte getKey(long timeStamp) {
        final long[] slot = this.slot;
        for (int i = 0; i < slot.length; i++) {
            final long slotTime = slot[i];
            if (slotTime >= timeStamp) {
                return (byte) i;
            }
        }
        return (byte) slot.length;
    }

    @Override
    public List<Byte> getRangeKey(long y1, long y2) {
        LongPair pair = arrange(y1, y2);
        Byte high = getKey(pair.getFirst());
        Byte low = getKey(pair.getSecond());
        List<Byte> result = new ArrayList<>();
        for (int i = low; i <= high; i++) {
            result.add((byte) i);
        }
        return result;
    }

    private LongPair arrange(long y1, long y2) {
        if (y1 > y2) {
            return new LongPair(y1, y2);
        }
        return new LongPair(y2, y1);
    }

    @Override
    public String toString() {
        return "OneByteFuzzyKeyFactory{" +
                "slot=" + Arrays.toString(slot) +
                '}';
    }
}
