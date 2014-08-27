package com.nhn.pinpoint.profiler.context.storage;

import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class BufferedStorage implements Storage {
    private static final Logger logger = LoggerFactory.getLogger(BufferedStorage.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private static final int DEFAULT_BUFFER_SIZE = 20;

    private final int bufferSize;

    private List<SpanEvent> storage ;
    private final DataSender dataSender;
    private final SpanChunkFactory spanChunkFactory;

    public BufferedStorage(DataSender dataSender, SpanChunkFactory spanChunkFactory) {
        this(dataSender, spanChunkFactory, DEFAULT_BUFFER_SIZE);
    }

    public BufferedStorage(DataSender dataSender, SpanChunkFactory spanChunkFactory, int bufferSize) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (spanChunkFactory == null) {
            throw new NullPointerException("spanChunkFactory must not be null");
        }
        this.dataSender = dataSender;
        this.spanChunkFactory = spanChunkFactory;
        this.bufferSize = bufferSize;
        this.storage = new ArrayList<SpanEvent>(bufferSize);
    }


    @Override
    public void store(SpanEvent spanEvent) {

        List<SpanEvent> flushData = null;
        synchronized (this) {
            addSpanEvent(spanEvent);
            if (storage.size() >= bufferSize) {
                // data copy
                flushData = storage;
                storage = new ArrayList<SpanEvent>(bufferSize);
            }
        }
        if (flushData != null) {
            final SpanChunk spanChunk = spanChunkFactory.create(flushData);
            if (isDebug) {
                logger.debug("flush SpanChunk {}", spanChunk);
            }
            dataSender.send(spanChunk);
        }
    }

    private void addSpanEvent(SpanEvent spanEvent) {
        final List<SpanEvent> storage = this.storage;
        if (storage == null) {
            if (logger.isErrorEnabled()) {
                logger.error("storage is null. discard spanEvent:{}", spanEvent);
            }
            // 이미 span이 와서 flush된 상황임.
            // 비동기를 이쪽에 포함시키면 이렇게 될수 있으나. 현재 구조를 변경할 계획임.
            return;
        }
        storage.add(spanEvent);
    }


    @Override
    public void store(Span span) {
        flushAll(span);
    }

    private void flushAll(Span span) {
        List<SpanEvent> spanEventList;
        synchronized (this) {
            spanEventList = storage;
            this.storage = null;
        }
        if (spanEventList != null && !spanEventList.isEmpty()) {
            span.setSpanEventList((List) spanEventList);
        }
        if (isDebug) {
            logger.debug("flush span {}", span);
        }
        dataSender.send(span);
    }

    @Override
    public String toString() {
        return "BufferedStorage{" +
                "bufferSize=" + bufferSize +
                ", dataSender=" + dataSender +
                '}';
    }
}
