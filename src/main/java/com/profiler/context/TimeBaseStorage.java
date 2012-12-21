package com.profiler.context;

import com.profiler.sender.DataSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class TimeBaseStorage implements Storage {

    private static final int RESERVE_BUFFER_SIZE = 2;

    private boolean discard = true;

    private boolean limit;
    private long limitTime = 1000;
    private int bufferSize = 20;

    private List<SubSpan> storage = new ArrayList<SubSpan>(bufferSize + RESERVE_BUFFER_SIZE);
    private DataSender dataSender;

    public TimeBaseStorage() {
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    @Override
    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setLimitTime(long limitTime) {
        this.limitTime = limitTime;
    }

    @Override
    public DataSender getDataSender() {
        return this.dataSender;
    }

    @Override
    public void store(SubSpan subSpan) {
        // flush유무 확인
        if (!limit) {
            // 절대 시간만 체크한다. 1초 이내 라서 절대 데이터를 flush하지 않는다.
            synchronized (this) {
                addSubSpan(subSpan);
            }
            limit = checkLimit(subSpan);
        } else {
            // 1초가 지났다면.
            // 데이터가 flushCount이상일 경우 먼저 flush한다.
            List<SubSpan> flushData = null;
            synchronized (this) {
                if (!addSubSpan(subSpan)) {
                    dataSender.send(subSpan);
                    return;
                }
                if (storage.size() >= bufferSize) {
                    flushData = storage;
                    storage = new ArrayList<SubSpan>(bufferSize + RESERVE_BUFFER_SIZE);
                }
            }
            if (flushData != null) {
                dataSender.send(new SubSpanList(flushData));
            }
        }
    }

    private boolean addSubSpan(SubSpan subSpan) {
        if (storage == null) {
            Logger logger = Logger.getLogger(this.getClass().getName());
            logger.fine("storage is null. direct send");
            // 이미 span이 와서 flush된 상황임.
            return false;
        }
        storage.add(subSpan);
        return true;
    }

    private boolean checkLimit(SubSpan subSpan) {
        return checkLimit(subSpan.getParentSpan());
    }

    private boolean checkLimit(Span span) {
        long startTime = span.getStartTime();
        long current = System.currentTimeMillis();
        return (current > (startTime + limitTime));
    }

    @Override
    public void store(Span span) {
        // Span이 들어오는것은 마지막 flush타이밍이다.
        // 비동기일 경우는 애매함. 비동기는 개별 flush해야 되나?
        if (discard) {
            limit = checkLimit(span);
            if (!limit) {
                // 제한시간내 빨리 끝난 경우는 subspan을 버린다.
                synchronized (this) {
                    this.storage = null;
                }
                dataSender.send(span);

            } else {
                // 제한 시간이 지났을 경우 모두 flush
                flushAll(span);
            }
        } else {
            flushAll(span);
        }
    }

    private void flushAll(Span span) {
        List<SubSpan> subSpanList;
        synchronized (this) {
            subSpanList = storage;
            this.storage = null;
        }
        if (subSpanList != null && subSpanList.size() != 0) {
            span.setSubSpanList(subSpanList);
        }
        dataSender.send(span);
    }

}
