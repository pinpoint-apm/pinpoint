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

import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAgentIdLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;

public class UidAgentIdLinkRowKeyDecoder implements RowKeyDecoder<UidAgentIdLinkRowKey> {
    private final int saltKeySize;

    public UidAgentIdLinkRowKeyDecoder(int saltKeySize) {
        this.saltKeySize = saltKeySize;
    }

    @Override
    public UidAgentIdLinkRowKey decodeRowKey(byte[] rowKey) {
        return UidAgentIdLinkRowKey.read(saltKeySize, rowKey);
    }
}
