/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.thrift.dto.command.TCmdSamplingRate;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yjqg6666
 */
public class SamplingRateService implements ProfilerRequestCommandService<TBase<?, ?>, TBase<?, ?>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Sampler sampler;

    public SamplingRateService(Sampler sampler) {
        this.sampler = sampler;
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tbase) {
        logger.info("{} execute {}.", this, tbase);
        if (!(tbase instanceof TCmdSamplingRate)) {
            return tbase;
        }

        final TCmdSamplingRate tCmdSamplingRate = (TCmdSamplingRate) tbase;
        final double requestSamplingRate = tCmdSamplingRate.getSamplingRate();
        if (requestSamplingRate >= 0) {
            sampler.updateSamplingRate(requestSamplingRate);
        }
        final double currentSamplingRate = sampler.getSamplingRate();
        tCmdSamplingRate.setSamplingRate(currentSamplingRate);
        return tCmdSamplingRate;
    }

    @Override
    public short getCommandServiceCode() {
        return TCommandType.SAMPLING_RATE.getCode();
    }

}
