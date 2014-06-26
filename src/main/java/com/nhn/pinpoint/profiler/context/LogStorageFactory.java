package com.nhn.pinpoint.profiler.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class LogStorageFactory implements StorageFactory {

    private final static Storage DEFAULT_STORAGE = new LogStorage();

    @Override
    public Storage createStorage() {
        // 상태 없음 그냥 재활용하면 됨.
        return DEFAULT_STORAGE;
    }

    public static class LogStorage implements Storage {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        @Override
        public void store(SpanEvent spanEvent) {
            logger.debug("log spanEvent:{}", spanEvent);
        }

        @Override
        public void store(Span span) {
            logger.debug("log span:{}", span);
        }
    }
}
