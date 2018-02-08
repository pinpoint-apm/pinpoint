/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.BusinessLogCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.BusinessLogDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.BusinessLogDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;

/**
 * [XINGUANG]
 */
@Component("businessLogCodecV1")
public class BusinessLogCodecV1 implements BusinessLogCodec<BusinessLogV1Bo>{
	
	private static final byte VERSION = 1;

	private final BusinessLogDataPointCodec codec;
	
	@Autowired
    public BusinessLogCodecV1(BusinessLogDataPointCodec codec) {
        Assert.notNull(codec, "BusinessLogDataPointCodec must not be null");
        this.codec = codec;
    }

	@Override
	public byte getVersion() {
		return VERSION;
	}

	@Override
	public void encodeValues(Buffer valueBuffer, List<BusinessLogV1Bo> businessLogV1Bos) {
		if (CollectionUtils.isEmpty(businessLogV1Bos)) {
            throw new IllegalArgumentException("businessLogV1Bos must not be empty");
        }
        final int numValues = businessLogV1Bos.size();
        valueBuffer.putVInt(numValues);
        
        List<Long> startTimestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder timeStampAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder timeAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder threadNameAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder logLevelAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder classNameAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder transactionIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder spanIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder messageAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        for(BusinessLogV1Bo BusinessLogV1Bo : businessLogV1Bos) {
        	startTimestamps.add(BusinessLogV1Bo.getStartTimestamp());
        	timeStampAnalyzerBuilder.addValue(BusinessLogV1Bo.getTimestamp());
        	timeAnalyzerBuilder.addValue(BusinessLogV1Bo.getTime());
        	threadNameAnalyzerBuilder.addValue(BusinessLogV1Bo.getThreadName());
        	logLevelAnalyzerBuilder.addValue(BusinessLogV1Bo.getLogLevel());
        	classNameAnalyzerBuilder.addValue(BusinessLogV1Bo.getClassName());
        	transactionIdAnalyzerBuilder.addValue(BusinessLogV1Bo.getTransactionId());
        	spanIdAnalyzerBuilder.addValue(BusinessLogV1Bo.getSpanId());
        	messageAnalyzerBuilder.addValue(BusinessLogV1Bo.getMessage());
        }

        this.codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
        this.encodeDataPoint(
        		valueBuffer,
        		timeStampAnalyzerBuilder.build(),
        		timeAnalyzerBuilder.build(),
        		threadNameAnalyzerBuilder.build(),
        		logLevelAnalyzerBuilder.build(),
        		classNameAnalyzerBuilder.build(),
        		transactionIdAnalyzerBuilder.build(),
        		spanIdAnalyzerBuilder.build(),
        		messageAnalyzerBuilder.build());
	}
	
	private void encodeDataPoint(
			Buffer valueBuffer,
			StrategyAnalyzer<Long>   timeStampAnalyzerBuilder,
			StrategyAnalyzer<String> timeAnalyzerBuilder,
			StrategyAnalyzer<String> threadNameAnalyzerBuilder,
			StrategyAnalyzer<String> logLevelAnalyzerBuilder,
			StrategyAnalyzer<String> classNameAnalyzerBuilder,
			StrategyAnalyzer<String> transactionIdAnalyzerBuilder,
			StrategyAnalyzer<String> spanIdAnalyzerBuilder,
			StrategyAnalyzer<String> messageAnalyzerBuilder) {
		AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
		headerEncoder.addCode(timeStampAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(timeAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(threadNameAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(logLevelAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(classNameAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(transactionIdAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(spanIdAnalyzerBuilder.getBestStrategy().getCode());
		headerEncoder.addCode(messageAnalyzerBuilder.getBestStrategy().getCode());
		final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, timeStampAnalyzerBuilder.getBestStrategy(),timeStampAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, timeAnalyzerBuilder.getBestStrategy(), timeAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, threadNameAnalyzerBuilder.getBestStrategy(), threadNameAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, logLevelAnalyzerBuilder.getBestStrategy(), logLevelAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, classNameAnalyzerBuilder.getBestStrategy(), classNameAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, transactionIdAnalyzerBuilder.getBestStrategy(), transactionIdAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, spanIdAnalyzerBuilder.getBestStrategy(), spanIdAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, messageAnalyzerBuilder.getBestStrategy(), messageAnalyzerBuilder.getValues());
	}

	@Override
	public List<String> decodeValues(Buffer valueBuffer, BusinessLogDecodingContext decodingContext) {
		final String agentId = decodingContext.getAgentId();

        int numValues = valueBuffer.readVInt();
        List<Long> startTimestamps = this.codec.decodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, numValues);
        
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long>  timestampEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> timeEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> threadNameEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> logLevelEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> classNameEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> transactionIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> spanIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> messageEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Long>   timestamps = this.codec.decodeValues(valueBuffer,timestampEncodingStrategy,numValues);
        List<String> times = this.codec.decodeValues(valueBuffer, timeEncodingStrategy, numValues);
        List<String> threadNames = this.codec.decodeValues(valueBuffer, threadNameEncodingStrategy, numValues);
        List<String> logLevels = this.codec.decodeValues(valueBuffer, logLevelEncodingStrategy, numValues);
        List<String> classNames = this.codec.decodeValues(valueBuffer, classNameEncodingStrategy, numValues);
        List<String> transactionIds = this.codec.decodeValues(valueBuffer, transactionIdEncodingStrategy, numValues);
        List<String> spanIds = this.codec.decodeValues(valueBuffer, spanIdEncodingStrategy, numValues);
        List<String> messages = this.codec.decodeValues(valueBuffer, messageEncodingStrategy, numValues);
        
        List<BusinessLogV1Bo> businessLogV1Bos = new ArrayList<BusinessLogV1Bo>(numValues);
        for(int i = 0; i < numValues; ++i) {
        	BusinessLogV1Bo businessLogV1Bo = new BusinessLogV1Bo();
        	businessLogV1Bo.setAgentId(agentId);
        	businessLogV1Bo.setStartTimestamp(startTimestamps.get(i));
        	businessLogV1Bo.setTimestamp(timestamps.get(i));
        	businessLogV1Bo.setTime(times.get(i));
        	businessLogV1Bo.setThreadName(threadNames.get(i));
        	businessLogV1Bo.setLogLevel(logLevels.get(i));
        	businessLogV1Bo.setClassName(classNames.get(i));
        	businessLogV1Bo.setTransactionId(transactionIds.get(i));
        	businessLogV1Bo.setSpanId(spanIds.get(i));
        	businessLogV1Bo.setMessage(messages.get(i));
        	businessLogV1Bos.add(businessLogV1Bo);
        }
        StringBuilder sb = new StringBuilder();
        List<String> logList = new ArrayList<String>();
        for(BusinessLogV1Bo bussinessLogV1Bo : businessLogV1Bos){
        	
        	/*sb.append(bussinessLogV1Bo.getAgentId()).append(" ");
        	sb.append(bussinessLogV1Bo.getStartTimestamp()).append(" ");
        	sb.append(bussinessLogV1Bo.getTimestamp()).append(" ");*/
        	sb.append(bussinessLogV1Bo.getTime()).append(" ");
        	sb.append(bussinessLogV1Bo.getThreadName()).append(" ");
        	sb.append(bussinessLogV1Bo.getLogLevel()).append(" ");
        	sb.append(bussinessLogV1Bo.getClassName()).append(" ");
        	sb.append("[transactionId : ").append(bussinessLogV1Bo.getTransactionId()).append(" ");
        	sb.append("spanId : ").append(bussinessLogV1Bo.getSpanId()).append("] ");
        	sb.append(bussinessLogV1Bo.getMessage()).append(" ");
        	sb.append("<br>");
        	logList.add(sb.toString());
        	
        	int  sb_length = sb.length();
        	sb.delete(0,sb_length); 
		}
        return logList;
	}

}
