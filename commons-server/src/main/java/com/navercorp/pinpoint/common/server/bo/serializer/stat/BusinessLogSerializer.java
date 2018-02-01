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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.server.bo.codec.stat.BusinessLogEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogDataPoint;

/**
 * [XINGUANG]
 */
public class BusinessLogSerializer<T extends BusinessLogDataPoint> implements HbaseSerializer<List<T>, Put>  {
	
	private final BusinessLogEncoder<T> encoder;
	
	protected BusinessLogSerializer(BusinessLogEncoder<T> encoder) {
		this.encoder = encoder;
	}

	@Override
	public void serialize(List<T> BusinessLogV1Bo, Put put, SerializationContext context) {
		if (CollectionUtils.isEmpty(BusinessLogV1Bo)) {
            throw new IllegalArgumentException("BusinessLogBos should not be empty");
        }
        long currentTime = System.currentTimeMillis();
		ByteBuffer qualifierBuffer = this.encoder.getQualifierBuffer(currentTime);
        ByteBuffer valueBuffer = this.encoder.encodeValue(BusinessLogV1Bo);
        put.addColumn(HBaseTables.BUSINESS_MESSAGEINFO, qualifierBuffer, HConstants.LATEST_TIMESTAMP, valueBuffer);
	}

}
