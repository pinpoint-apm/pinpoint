/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Component("fileDescriptorCodecV2")
public class FileDescriptorCodecV2 extends AgentStatCodecV2<FileDescriptorBo> {

    @Autowired
    public FileDescriptorCodecV2(AgentStatDataPointCodec codec) {
        super(new FileDescriptorCodecFactory(codec));
    }


    private static class FileDescriptorCodecFactory implements CodecFactory<FileDescriptorBo> {

        private final AgentStatDataPointCodec codec;

        private FileDescriptorCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<FileDescriptorBo> createCodecEncoder() {
            return new FileDescriptorCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<FileDescriptorBo> createCodecDecoder() {
            return new FileDescriptorCodecDecoder(codec);
        }
    }

    public static class FileDescriptorCodecEncoder implements CodecEncoder<FileDescriptorBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder openFileDescriptorCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public FileDescriptorCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(FileDescriptorBo fileDescriptorBo) {
            openFileDescriptorCountAnalyzerBuilder.addValue(fileDescriptorBo.getOpenFileDescriptorCount());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> openFileDescriptorCountStrategyAnalyzer = openFileDescriptorCountAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(openFileDescriptorCountStrategyAnalyzer.getBestStrategy().getCode());
            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, openFileDescriptorCountStrategyAnalyzer.getBestStrategy(), openFileDescriptorCountStrategyAnalyzer.getValues());

        }

    }

    public static class FileDescriptorCodecDecoder implements CodecDecoder<FileDescriptorBo> {

        private final AgentStatDataPointCodec codec;

        private List<Long> openFileDescriptorCounts;

        public FileDescriptorCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> openFileDescriptorCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.openFileDescriptorCounts = this.codec.decodeValues(valueBuffer, openFileDescriptorCountEncodingStrategy, valueSize);
        }

        @Override
        public FileDescriptorBo getValue(int index) {
            FileDescriptorBo fileDescriptorBo = new FileDescriptorBo();
            fileDescriptorBo.setOpenFileDescriptorCount(openFileDescriptorCounts.get(index));
            return fileDescriptorBo;
        }

    }

}
