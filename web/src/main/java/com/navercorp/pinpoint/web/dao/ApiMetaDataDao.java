/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

/**
 * @author emeroad
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(ServiceUid serviceUid, String agentId, long time, int apiId);

    /**
     * Batch variant of {@link #getApiMetaData(ServiceUid, String, long, int)}.
     * The returned list is index-aligned with {@code keys}; a key with no matching
     * row yields an empty list at the same index.
     */
    List<List<ApiMetaDataBo>> getApiMetaData(List<ApiMetaDataKey> keys);

    record ApiMetaDataKey(ServiceUid serviceUid, String agentId, long agentStartTime, int apiId) {
        public ApiMetaDataKey(String agentId, long agentStartTime, int apiId) {
            this(ServiceUid.DEFAULT, agentId, agentStartTime, apiId);
        }
    }
}
