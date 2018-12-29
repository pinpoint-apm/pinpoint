package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.profiler.transaction.RegistryMapping.DEFAULT_REQUEST_MAPPING_INFO;

public class SamplingTransactionSampler implements Sampler {

    private ConcurrentHashMap<String, AtomicInteger> counter = new ConcurrentHashMap();
    private final int samplingRate;

    public SamplingTransactionSampler(int samplingRate) {
        if (samplingRate <= 0) {
            throw new IllegalArgumentException("Invalid samplingRate " + samplingRate);
        }
        this.samplingRate = samplingRate;
    }

    @Override
    public boolean isSampling() {
        return isSampling(DEFAULT_REQUEST_MAPPING_INFO.getTransactionType());
    }

    @Override
    public boolean isSampling(String transactionType) {
        synchronized (counter) {
            AtomicInteger transactionCounter = counter.get(transactionType);
            if (transactionCounter == null) {
                transactionCounter = new AtomicInteger(0);
                counter.put(transactionType, transactionCounter);
            }

            int samplingCount = MathUtils.fastAbs(transactionCounter.getAndIncrement());
            int isSampling = samplingCount % samplingRate;
            return isSampling == 0;
        }
    }
}
