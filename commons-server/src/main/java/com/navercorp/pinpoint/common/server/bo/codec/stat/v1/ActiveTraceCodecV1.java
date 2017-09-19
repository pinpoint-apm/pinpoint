/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v2.ActiveTraceCodecV2;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component("activeTraceCodecV1")
public class ActiveTraceCodecV1 extends AbstractAgentStatCodecV1<ActiveTraceBo> {

    @Autowired
    public ActiveTraceCodecV1(AgentStatDataPointCodec codec) {
        super(codec);
    }

    @Override
    protected CodecEncoder createCodecEncoder() {
        return new ActiveTraceCodecV2.ActiveTraceCodecEncoder(codec);
    }

    @Override
    protected CodecDecoder createCodecDecoder() {
        return new ActiveTraceCodecV2.ActiveTraceCodecDecoder(codec);
    }

}
