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

import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanKeyFactory;
import com.navercorp.pinpoint.web.vo.Application;

public class MapScanKeyFactoryV3 implements MapScanKeyFactory {
    public byte[] scanKey(int serviceUid, Application application, long timestamp) {
        return UidLinkRowKey.makeRowKey(ByteSaltKey.NONE.size(), serviceUid,
                application.getName(),
                application.getServiceTypeCode(), timestamp);
    }
}
