/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BasicTraceSampler implements TraceSampler {

    private final Sampler sampler;

    private final State newSuccessState;
    private final State newDisableState;

    private final State continueSuccessState;
    private final State continueDisableState;


    public BasicTraceSampler(final IdGenerator idGenerator, Sampler sampler) {
        Assert.requireNonNull(idGenerator, "idGenerator");
        this.sampler = Assert.requireNonNull(sampler, "sampler");

        this.newSuccessState = new State() {
            @Override
            public boolean isSampled() {
                return true;
            }

            @Override
            public long nextId() {
                return idGenerator.nextTransactionId();
            }
        };

        this.newDisableState = new State() {
            @Override
            public boolean isSampled() {
                return false;
            }

            @Override
            public long nextId() {
                return idGenerator.nextDisabledId();
            }
        };


        this.continueSuccessState = new State() {
            @Override
            public boolean isSampled() {
                return true;
            }

            @Override
            public long nextId() {
                return idGenerator.nextContinuedTransactionId();
            }
        };

        this.continueDisableState = new State() {
            @Override
            public boolean isSampled() {
                return false;
            }

            @Override
            public long nextId() {
                return idGenerator.nextContinuedDisabledId();
            }
        };

    }


    @Override
    public State isNewSampled() {
        if (sampler.isSampling()) {
            return newSuccessState;
        } else {
            return newDisableState;
        }
    }

    @Override
    public State isContinueSampled() {
        return continueSuccessState;
    }

    @Override
    public State getContinueDisableState() {
        return continueDisableState;
    }
}
