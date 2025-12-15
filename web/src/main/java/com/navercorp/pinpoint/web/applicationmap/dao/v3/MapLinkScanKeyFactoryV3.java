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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidPrefix;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanKeyFactory;
import com.navercorp.pinpoint.web.vo.Application;

public class MapLinkScanKeyFactoryV3 implements MapScanKeyFactory {
    private static final int saltKeySize = ByteSaltKey.NONE.size();

    public byte[] scanKey(int serviceUid, Application application, long timestamp) {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.skip(saltKeySize);
        UidPrefix.writePrefix(buffer, serviceUid, application.getName().getBytes(), application.getServiceTypeCode(), timestamp);
        return buffer.getBuffer();
    }
}
