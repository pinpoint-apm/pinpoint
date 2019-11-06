/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sampler;

import com.google.common.util.concurrent.RateLimiter;

import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;


/**
 * @author jaehong.kim
 */
public class RateLimitTraceSampler implements TraceSampler {

    private final TraceSampler traceSampler;

    // @Nullable
    private final RateLimiter newRateLimiter;
    private final State newSkipState;

    // @Nullable
    private final RateLimiter continueRateLimiter;
    private final State continueSkipState;


    public RateLimitTraceSampler(final int newMaxNewThroughput, final int newMaxContinueThroughput, final IdGenerator idGenerator, TraceSampler traceSampler) {
        Assert.requireNonNull(idGenerator, "idGenerator");
        this.traceSampler = Assert.requireNonNull(traceSampler, "traceSampler");


        this.newRateLimiter = newRateLimiter(newMaxNewThroughput);
        this.newSkipState = new State() {
            @Override
            public boolean isSampled() {
                return false;
            }

            @Override
            public long nextId() {
                return idGenerator.nextSkippedId();
            }
        };

        this.continueRateLimiter = newRateLimiter(newMaxContinueThroughput);
        this.continueSkipState = new State() {
            @Override
            public boolean isSampled() {
                return false;
            }

            @Override
            public long nextId() {
                return idGenerator.nextContinuedSkippedId();
            }
        };
    }

    private RateLimiter newRateLimiter(int newMaxThroughput) {
        if (newMaxThroughput > 0) {
            return RateLimiter.create(newMaxThroughput);
        }
        return null;
    }

    @Override
    public State isNewSampled() {
        final State newState = traceSampler.isNewSampled();
        if (newState.isSampled()) {
            return getState(this.newRateLimiter, newState, newSkipState);
        }
        return newState;
    }


    @Override
    public State isContinueSampled() {
        final State continueState = traceSampler.isContinueSampled();
        if (continueState.isSampled()) {
            return getState(this.continueRateLimiter, continueState, continueSkipState);
        }
        return continueState;
    }


    private State getState(RateLimiter rateLimiter, State successState, State failState) {
        if (rateLimiter == null) {
            return successState;
        }
        final boolean acquire = rateLimiter.tryAcquire();
        if (acquire) {
            return successState;
        } else {
            return failState;
        }
    }

    @Override
    public State getContinueDisableState() {
        return traceSampler.getContinueDisableState();
    }

}