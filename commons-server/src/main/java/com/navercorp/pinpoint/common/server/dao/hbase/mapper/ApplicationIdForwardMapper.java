/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Component
public class ApplicationIdForwardMapper implements RowMapper<UUID> {

    private static final HbaseColumnFamily.ApplicationId DESCRIPTOR = HbaseColumnFamily.APPLICATION_ID_FORWARD;

    @Override
    public UUID mapRow(Result result, int rowNum) throws Exception {
        byte[] family = DESCRIPTOR.getName();
        byte[] qualifier = DESCRIPTOR.getName();
        Cell cell = result.getColumnLatestCell(family, qualifier);
        if (cell == null) {
            return null;
        }

        if (cell.getValueLength() < 16) {
            throw new IllegalArgumentException("Invalid bytes length: " + cell.getValueLength());
        }

        return BytesUtils.bytesToUUID(cell.getValueArray(), cell.getValueOffset());
    }

}
