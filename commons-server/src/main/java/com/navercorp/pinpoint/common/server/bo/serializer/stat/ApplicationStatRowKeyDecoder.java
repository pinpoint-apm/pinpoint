/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import static com.navercorp.pinpoint.common.hbase.HbaseTableConstants.APPLICATION_NAME_MAX_LEN;
import static com.navercorp.pinpoint.common.server.bo.stat.join.StatType.TYPE_CODE_BYTE_LENGTH;


/**
 * @author minwoo.jung
 */
public class ApplicationStatRowKeyDecoder implements RowKeyDecoder<ApplicationStatRowKeyComponent> {

    @Override
    public ApplicationStatRowKeyComponent decodeRowKey(byte[] rowkey) {
        final String applicationId = BytesUtils.toStringAndRightTrim(rowkey, 0, APPLICATION_NAME_MAX_LEN);
        final StatType statType = StatType.fromTypeCode(rowkey[APPLICATION_NAME_MAX_LEN]);
        final long reversedBaseTimestamp = BytesUtils.bytesToLong(rowkey, APPLICATION_NAME_MAX_LEN + TYPE_CODE_BYTE_LENGTH);
        final long baseTimestamp = TimeUtils.recoveryTimeMillis(reversedBaseTimestamp);
        return new ApplicationStatRowKeyComponent(applicationId, statType, baseTimestamp);
    }
}