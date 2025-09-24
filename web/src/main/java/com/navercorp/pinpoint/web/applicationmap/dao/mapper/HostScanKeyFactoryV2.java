/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.Application;

public class HostScanKeyFactoryV2 implements HostScanKeyFactory {
    @Override
    public byte[] scanKey(Application parentApplication, long timestamp) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadString(parentApplication.getName(), HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
        buffer.putShort((short) parentApplication.getServiceTypeCode());
        long reverseTimestamp = TimeUtils.recoveryTimeMillis(timestamp);
        buffer.putLong(reverseTimestamp);
        return buffer.getBuffer();
    }
}
