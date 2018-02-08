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

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;

/**
 * [XINGUANG]
 */
@Component
public class BusinessLogRowKeyEncoder implements RowKeyEncoder<BusinessLogRowKeyComponent>{

	@Override
    public byte[] encodeRowKey(BusinessLogRowKeyComponent component) {
		if (component == null) {
			throw new NullPointerException("component must not be null");
		}
		byte[] bAgentId = BytesUtils.toBytes(component.getAgentId());
		byte[] bStatType = new byte[]{component.getBusinessLogType().getRawTypeCode()};
		byte[] bTransactionIdAndSpanId = BytesUtils.toBytes(component.getTransactionIdANDSpanId());
		byte[] rowKey = new byte[AGENT_NAME_MAX_LEN + bStatType.length + PinpointConstants.TRANSACTIONID_AND_SPANID_LEN];
		
		BytesUtils.writeBytes(rowKey, 0, bAgentId);
        BytesUtils.writeBytes(rowKey, AGENT_NAME_MAX_LEN, bStatType);
        BytesUtils.writeBytes(rowKey, AGENT_NAME_MAX_LEN + bStatType.length, bTransactionIdAndSpanId);
		return rowKey;
    }

}
