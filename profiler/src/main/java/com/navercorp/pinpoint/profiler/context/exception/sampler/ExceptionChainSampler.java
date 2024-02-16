/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.context.exception.sampler;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.atomic.AtomicLong;


/**
 * @author intr3p1d
 */
public class ExceptionChainSampler {

    public static final long INITIAL_EXCEPTION_ID = 1L;

    private final RateLimiter rateLimiter;

    private final AtomicLong exceptionChainId = new AtomicLong(INITIAL_EXCEPTION_ID);


    public final static SamplingState DISABLED = new SamplingState() {
        @Override
        public boolean isSampling() {
            return false;
        }

        @Override
        public long currentId() {
            return Long.MIN_VALUE;
        }
    };

    public ExceptionChainSampler(final double maxNewThroughput) {
        this.rateLimiter = RateLimiter.create(maxNewThroughput);
    }

    public SamplingState isNewSampled() {
        if (rateLimiter.tryAcquire()) {
            long errorId = nextErrorId();
            return newState(errorId);
        }
        return DISABLED;
    }

    private long nextErrorId() {
        return this.exceptionChainId.getAndIncrement();
    }

    private SamplingState newState(long id) {
        return new SamplingState() {
            @Override
            public boolean isSampling() {
                return true;
            }

            @Override
            public long currentId() {
                return id;
            }
        };
    }

    public interface SamplingState {
        boolean isSampling();

        long currentId();
    }
}
