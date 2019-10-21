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
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v2.JvmGcDetailedCodecV2;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component("jvmGcDetailedCodecV1")
public class JvmGcDetailedCodecV1 extends AgentStatCodecV1<JvmGcDetailedBo> {

    @Autowired
    public JvmGcDetailedCodecV1(AgentStatDataPointCodec codec) {
        super(new JvmGcDetailedCodecFactory(codec));
    }


    private static class JvmGcDetailedCodecFactory implements CodecFactory<JvmGcDetailedBo> {

        private final AgentStatDataPointCodec codec;

        private JvmGcDetailedCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<JvmGcDetailedBo> createCodecEncoder() {
            return new JvmGcDetailedCodecV2.JvmGcDetailedCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<JvmGcDetailedBo> createCodecDecoder() {
            return new JvmGcDetailedCodecV2.JvmGcDetailedCodecDecoder(codec);
        }
    }

}
