package com.navercorp.pinpoint.test.plugin.maven;

import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.eclipse.aether.RepositoryCache;
import org.eclipse.aether.RepositorySystemSession;
import org.tinylog.TaggedLogger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceRepositoryCache implements RepositoryCache  {
    private final TaggedLogger logger = TestLogger.getLogger();

    private final RepositoryCache delegate;
    private final AtomicInteger hit = new AtomicInteger();
    private final AtomicInteger miss = new AtomicInteger();

    public TraceRepositoryCache(RepositoryCache delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void put(RepositorySystemSession session, Object key, Object data) {
        if (logger.isInfoEnabled()) {
            logger.info("cache-put:{} {} {}", session, key, data);
        }
        delegate.put(session, key, data);
    }

    @Override
    public Object get(RepositorySystemSession session, Object key) {
        final Object result = delegate.get(session, key);
        if (result == null) {
            int count = miss.incrementAndGet();
            if (logger.isInfoEnabled()) {
                logger.info("cache-get-miss-{}:{} {}", count, session, key);
            }
        } else {
            int count = hit.incrementAndGet();
            if (logger.isInfoEnabled()) {
                logger.info("cache-get-hit-{}:{} {} result:{}", count, session, key, result);
            }
        }
        return result;
    }
}
