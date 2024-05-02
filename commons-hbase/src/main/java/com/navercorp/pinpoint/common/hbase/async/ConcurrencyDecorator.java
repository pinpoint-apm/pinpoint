package com.navercorp.pinpoint.common.hbase.async;

public class ConcurrencyDecorator implements HbasePutWriterDecorator {
    private final int permits;

    public ConcurrencyDecorator(int permits) {
        this.permits = permits;
    }

    @Override
    public HbasePutWriter decorator(HbasePutWriter hbasePutWriter) {
        if (permits == 0) {
            return hbasePutWriter;
        }

        final ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(permits);
        return new RateLimiterPutWriter(hbasePutWriter, helper);
    }
}
