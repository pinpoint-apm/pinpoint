package com.profiler.context;

import com.profiler.sender.DataSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class TimeLimitStorage implements Storage {
    private int flushCount = 10;

    private List<SubSpan> storage = new ArrayList<SubSpan>(flushCount);

    private boolean limit;
    private boolean dropSubSpan = true;
    private int limitTime = 1000;
    private DataSender dataSender;

    @Override
    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public DataSender getDataSender() {
        return this.dataSender;
    }

    @Override
    public void store(SubSpan subSpan) {
        addSubSpan(subSpan);
        // flush유무 확인
        if (!limit) {
            // 절대 시간만 체크한다. 1초 이내 라서 절대 데이터를 flush하지 않는다.
            limit = checkLimit(subSpan);
        } else {
            // 1초가 지났다면.
            // 데이터가 flushCount이상일 경우 먼저 flush한다.
            if (storage.size() >= flushCount) {
                SubSpanList subSpanList = new SubSpanList(storage);
                storage = new ArrayList<SubSpan>(flushCount);
                dataSender.send(subSpanList);
            }
        }
    }

    private void addSubSpan(SubSpan subSpan) {
        if (storage == null) {
            Logger logger = Logger.getLogger(this.getClass().getName());
            logger.warning("storage is null.");
            return;
        }
        storage.add(subSpan);
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
        if (dropSubSpan) {

            limit = checkLimit(span);
            if (!limit) {
                // 제한시간내 빨리 끝난 경우는 subspan을 버린다.
                this.storage = null;
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
        List<SubSpan> subSpanList = storage;
        if (subSpanList != null && subSpanList.size() != 0) {
            span.setSubSpanList(subSpanList);
        }
        this.storage = null;
        dataSender.send(span);
    }

}
