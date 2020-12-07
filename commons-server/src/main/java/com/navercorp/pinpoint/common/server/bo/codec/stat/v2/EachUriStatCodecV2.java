/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * @author Taejin Koo
 */
public class EachUriStatCodecV2 implements AgentStatCodec<EachUriStatBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    public EachUriStatCodecV2(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<EachUriStatBo> eachUriStatBoList) {
        EachUriStatBoCodecEncoder eachUriStatBoCodecEncoder = new EachUriStatBoCodecEncoder(codec);
        for (EachUriStatBo eachUriStatBo : eachUriStatBoList) {
            eachUriStatBoCodecEncoder.addValue(eachUriStatBo);
        }

        eachUriStatBoCodecEncoder.encode(valueBuffer);
    }

    @Override
    public List<EachUriStatBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final int numValues = valueBuffer.readVInt();

        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);

        EachUriStatBoCodecDecoder eachUriStatBoCodecDecoder = new EachUriStatBoCodecDecoder(codec);
        eachUriStatBoCodecDecoder.decode(valueBuffer, headerDecoder, numValues);

        List<EachUriStatBo> eachUriStatBoList = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            EachUriStatBo eachUriStatBo = eachUriStatBoCodecDecoder.getValue(i);
            eachUriStatBoList.add(eachUriStatBo);
        }

        return eachUriStatBoList;
    }

    private static class EachUriStatBoCodecEncoder implements AgentStatCodec.CodecEncoder<EachUriStatBo> {

        private final AgentStatDataPointCodec codec;

        private final StringEncodingStrategy.Analyzer.Builder uriAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder histogramCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();

        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder countAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder avgAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder maxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder under100BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range100to300BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range300to500BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range500to1000BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range1000to3000BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range3000to5000BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder range5000to8000BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder over8000BucketCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();

        public EachUriStatBoCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(EachUriStatBo eachUriStatBo) {
            uriAnalyzerBuilder.addValue(eachUriStatBo.getUri());
            int histogramCount = getHistogramCount(eachUriStatBo);
            histogramCountAnalyzerBuilder.addValue(histogramCount);

            UriStatHistogram totalHistogram = eachUriStatBo.getTotalHistogram();
            addValue(totalHistogram);

            UriStatHistogram failedHistogram = eachUriStatBo.getFailedHistogram();
            addValue(failedHistogram);
        }

        private void addValue(UriStatHistogram uriStatHistogram) {
            if (uriStatHistogram == null || uriStatHistogram.getCount() == 0) {
                return;
            }
            countAnalyzerBuilder.addValue(uriStatHistogram.getCount());
            avgAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(uriStatHistogram.getAvg()));
            maxAnalyzerBuilder.addValue(uriStatHistogram.getMax());

            int[] timestampHistogram = uriStatHistogram.getTimestampHistogram();
            under100BucketCountAnalyzerBuilder.addValue(timestampHistogram[0]);
            range100to300BucketCountAnalyzerBuilder.addValue(timestampHistogram[1]);
            range300to500BucketCountAnalyzerBuilder.addValue(timestampHistogram[2]);
            range500to1000BucketCountAnalyzerBuilder.addValue(timestampHistogram[3]);
            range1000to3000BucketCountAnalyzerBuilder.addValue(timestampHistogram[4]);
            range3000to5000BucketCountAnalyzerBuilder.addValue(timestampHistogram[5]);
            range5000to8000BucketCountAnalyzerBuilder.addValue(timestampHistogram[6]);
            over8000BucketCountAnalyzerBuilder.addValue(timestampHistogram[7]);
        }

        private int getHistogramCount(EachUriStatBo eachUriStatBo) {
            UriStatHistogram failedHistogram = eachUriStatBo.getFailedHistogram();
            if (failedHistogram == null || failedHistogram.getCount() == 0) {
                return 1;
            }
            return 2;
        }

        @Override
        public void encode(Buffer valueBuffer) {
            final List<StrategyAnalyzer> strategyAnalyzerList = getAnalyzerList();

            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            for (StrategyAnalyzer strategyAnalyzer : strategyAnalyzerList) {
                headerEncoder.addCode(strategyAnalyzer.getBestStrategy().getCode());
            }
            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);

            for (StrategyAnalyzer strategyAnalyzer : strategyAnalyzerList) {
                this.codec.encodeValues(valueBuffer, strategyAnalyzer.getBestStrategy(), strategyAnalyzer.getValues());
            }
        }

        private List<StrategyAnalyzer> getAnalyzerList() {
            List<StrategyAnalyzer> strategyAnalyzerList = new ArrayList<>();
            strategyAnalyzerList.add(uriAnalyzerBuilder.build());
            strategyAnalyzerList.add(histogramCountAnalyzerBuilder.build());

            strategyAnalyzerList.add(countAnalyzerBuilder.build());
            strategyAnalyzerList.add(avgAnalyzerBuilder.build());
            strategyAnalyzerList.add(maxAnalyzerBuilder.build());

            strategyAnalyzerList.add(under100BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range100to300BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range300to500BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range500to1000BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range1000to3000BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range3000to5000BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(range5000to8000BucketCountAnalyzerBuilder.build());
            strategyAnalyzerList.add(over8000BucketCountAnalyzerBuilder.build());

            return strategyAnalyzerList;
        }
    }

    private static class EachUriStatBoCodecDecoder implements AgentStatCodec.CodecDecoder<EachUriStatBo> {

        private final AgentStatDataPointCodec codec;

        private List<String> uriList;
        private List<Integer> histogramCountList;
        private Queue<UriStatHistogram> uriStatHistogramQueue;

        public EachUriStatBoCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<String> uriEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> histogramCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());

            this.uriList = this.codec.decodeValues(valueBuffer, uriEncodingStrategy, valueSize);
            this.histogramCountList = this.codec.decodeValues(valueBuffer, histogramCountEncodingStrategy, valueSize);

            int totalHistogramCount = 0;
            for (Integer histogramCount : histogramCountList) {
                totalHistogramCount += histogramCount;
            }

            this.uriStatHistogramQueue = decodeUriStatHistogramList(valueBuffer, headerDecoder, totalHistogramCount);
        }

        private Queue<UriStatHistogram> decodeUriStatHistogramList(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Integer> countAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> avgAnalyzerEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> maxAnalyzerEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());

            EncodingStrategy<Integer> under100BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range100to300BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range300to500BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range500to1000BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range1000to3000BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range3000to5000BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> range5000to8000BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Integer> over8000BucketCountAnalyzerEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());

            List<Integer> countList = this.codec.decodeValues(valueBuffer, countAnalyzerEncodingStrategy, valueSize);
            List<Long> avgList = this.codec.decodeValues(valueBuffer, avgAnalyzerEncodingStrategy, valueSize);
            List<Long> maxList = this.codec.decodeValues(valueBuffer, maxAnalyzerEncodingStrategy, valueSize);

            List<Integer> under100BucketCountList = this.codec.decodeValues(valueBuffer, under100BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range100to300BucketCountList = this.codec.decodeValues(valueBuffer, range100to300BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range300to500BucketCountList = this.codec.decodeValues(valueBuffer, range300to500BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range500to1000BucketCountList = this.codec.decodeValues(valueBuffer, range500to1000BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range1000to3000BucketCountList = this.codec.decodeValues(valueBuffer, range1000to3000BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range3000to5000BucketCountList = this.codec.decodeValues(valueBuffer, range3000to5000BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> range5000to8000BucketCountList = this.codec.decodeValues(valueBuffer, range5000to8000BucketCountAnalyzerEncodingStrategy, valueSize);
            List<Integer> over8000BucketCountList = this.codec.decodeValues(valueBuffer, over8000BucketCountAnalyzerEncodingStrategy, valueSize);

            Queue<UriStatHistogram> uriStatHistogramQueue = new LinkedList<>();
            for (int i = 0; i < valueSize; i++) {
                UriStatHistogram uriStatHistogram = new UriStatHistogram();
                final Integer count = countList.get(i);
                uriStatHistogram.setCount(count);

                final Long avg = avgList.get(i);
                uriStatHistogram.setAvg(AgentStatUtils.convertLongToDouble(avg));

                final Long max = maxList.get(i);
                uriStatHistogram.setMax(max);

                final int[] timestampHistograms = new int[]{under100BucketCountList.get(i), range100to300BucketCountList.get(i), range300to500BucketCountList.get(i), range500to1000BucketCountList.get(i),
                        range1000to3000BucketCountList.get(i), range3000to5000BucketCountList.get(i), range5000to8000BucketCountList.get(i), over8000BucketCountList.get(i)};
                uriStatHistogram.setTimestampHistogram(timestampHistograms);

                uriStatHistogramQueue.add(uriStatHistogram);
            }
            return uriStatHistogramQueue;
        }

        @Override
        public EachUriStatBo getValue(int index) {
            String uri = uriList.get(index);
            Integer histogramCount = histogramCountList.get(index);

            EachUriStatBo eachUriStatBo = new EachUriStatBo();
            eachUriStatBo.setUri(uri);

            UriStatHistogram totalHistogramCount = uriStatHistogramQueue.poll();
            eachUriStatBo.setTotalHistogram(totalHistogramCount);

            if (histogramCount == 2) {
                UriStatHistogram failedHistogramCount = uriStatHistogramQueue.poll();
                eachUriStatBo.setFailedHistogram(failedHistogramCount);
            }

            return eachUriStatBo;
        }

    }

}
