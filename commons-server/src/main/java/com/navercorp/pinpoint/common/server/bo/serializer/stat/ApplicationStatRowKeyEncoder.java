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

import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.springframework.stereotype.Component;

import static com.navercorp.pinpoint.common.hbase.HbaseTableConstatns.APPLICATION_NAME_MAX_LEN;

/**
 * @author minwoo.jung
 */
@Component
public class ApplicationStatRowKeyEncoder implements RowKeyEncoder<ApplicationStatRowKeyComponent> {

    @Override
    public byte[] encodeRowKey(ApplicationStatRowKeyComponent component) {
        if (component == null) {
            throw new NullPointerException("component");
        }
        byte[] bApplicationId = BytesUtils.toBytes(component.getApplicationId());
        byte[] bStatType = new byte[]{component.getStatType().getRawTypeCode()};
        byte[] rowKey = new byte[APPLICATION_NAME_MAX_LEN + bStatType.length + BytesUtils.LONG_BYTE_LENGTH];

        BytesUtils.writeBytes(rowKey, 0, bApplicationId);
        BytesUtils.writeBytes(rowKey, APPLICATION_NAME_MAX_LEN, bStatType);
        BytesUtils.writeLong(TimeUtils.reverseTimeMillis(component.getBaseTimestamp()), rowKey, APPLICATION_NAME_MAX_LEN + bStatType.length);

        return rowKey;
    }
}
