/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.dao.hbase.encode;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIndexRowKeyEncoderV2Test {

    private ApplicationIndexRowKeyEncoderV2 encoder;

    @BeforeEach
    void beforeEach() {
        AbstractRowKeyDistributor rowKeyDistributor = applicationTraceIndexDistributor();
        this.encoder = new ApplicationIndexRowKeyEncoderV2(rowKeyDistributor);
    }

    private AbstractRowKeyDistributor applicationTraceIndexDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RowKeyDistributorByHashPrefix.OneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Test
    void newRowKey() {

        byte[] rowKey = encoder.newRowKey(new UUID(100, 100), 100, (byte) 10);

        int fuzzySize = PinpointConstants.APPLICATION_NAME_MAX_LEN + Bytes.SIZEOF_LONG + 1;
        assertThat(rowKey).hasSize(fuzzySize);
        Assertions.assertEquals(10, rowKey[rowKey.length - 1]);
    }
}