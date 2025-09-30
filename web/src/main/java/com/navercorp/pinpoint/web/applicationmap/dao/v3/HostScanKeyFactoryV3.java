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

package com.navercorp.pinpoint.web.applicationmap.dao.v3;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostScanKeyFactory;
import com.navercorp.pinpoint.web.vo.Application;

public class HostScanKeyFactoryV3 implements HostScanKeyFactory {

    private final int applicationNameMaxLength = PinpointConstants.APPLICATION_NAME_MAX_LEN_V3;

    @Override
    public byte[] scanKey(Application parentApplication, long timestamp) {
//        Buffer buffer = new AutomaticBuffer();
//        buffer.putPadString(parentApplication.getName(), applicationNameMaxLength);
//        buffer.putInt(parentApplication.getServiceTypeCode());
//        buffer.putInt(ServiceUid.DEFAULT_SERVICE_UID_CODE);
//        long reverseTimestamp = LongInverter.invert(timestamp);
//        buffer.putLong(reverseTimestamp);
//        return buffer.getBuffer();


        return UidLinkRowKey.makeRowKey(0, ServiceUid.DEFAULT_SERVICE_UID_CODE,
                parentApplication.getName(), parentApplication.getServiceTypeCode(),
                timestamp);
    }
}
