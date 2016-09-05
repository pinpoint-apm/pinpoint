package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * usage for development env
 * @author Woonduk Kang(emeroad)
 */
public class DualWriteHbaseTraceDao implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TraceDao master;
    private final TraceDao slave;

    public DualWriteHbaseTraceDao(TraceDao master, TraceDao slave) {
        if (master == null) {
            throw new NullPointerException("master must not be null");
        }
        if (slave == null) {
            throw new NullPointerException("slave must not be null");
        }
        this.master = master;
        this.slave = slave;
    }

    @Override
    public void insert(SpanBo span) {
        Throwable masterException = null;
        try {
            master.insert(span);
        } catch (Throwable e) {
            masterException = e;
        }
        try {
            slave.insert(span);
        } catch (Throwable e) {
            logger.warn("slave insert(TSpan) Error:{}", e.getMessage(), e);
        }
        rethrowRuntimeException(masterException);
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        Throwable masterException = null;
        try {
            master.insertSpanChunk(spanChunkBo);
        } catch (Throwable e) {
            masterException = e;
        }
        try {
            slave.insertSpanChunk(spanChunkBo);
        } catch (Throwable e) {
            logger.warn("slave insertSpanChunk(TSpanChunk) Error:{}", e.getMessage(), e);
        }
        rethrowRuntimeException(masterException);
    }

    private void rethrowRuntimeException(Throwable exception) {
        if (exception != null) {
            this.<RuntimeException>rethrowException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Exception> void rethrowException(final Throwable exception) throws T {
        throw (T) exception;
    }
}
