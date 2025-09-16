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
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapV3ScanFactory extends MapScanFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public MapV3ScanFactory(RangeFactory rangeFactory) {
        super(rangeFactory);
    }

    protected byte[] rowKey(Application application, long timestamp) {
        return UidLinkRowKey.makeRowKey(ByteSaltKey.NONE.size(), ServiceUid.DEFAULT_SERVICE_UID_CODE, application.getName(),
                application.getServiceTypeCode(), timestamp);
    }
}
