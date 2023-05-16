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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataDecoder;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class StringMetaDataMapper implements RowMapper<List<StringMetaDataBo>> {

    private final static String STRING_METADATA_CF_STR_QUALI_STRING = Bytes.toString(HbaseColumnFamily.STRING_METADATA_STR.QUALIFIER_STRING);

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final RowKeyDecoder<MetaDataRowKey> decoder = new MetadataDecoder();

    public StringMetaDataMapper(@Qualifier("metadataRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public List<StringMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());
        final MetaDataRowKey key = decoder.decodeRowKey(rowKey);

        List<StringMetaDataBo> stringMetaDataList = new ArrayList<>();

        for (Cell cell : result.rawCells()) {

            String stringValue = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            
            if (STRING_METADATA_CF_STR_QUALI_STRING.equals(stringValue)) {
                stringValue = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }
            
            StringMetaDataBo stringMetaDataBo = new StringMetaDataBo(key.getAgentId(), key.getAgentStartTime(), key.getId(), stringValue);

            stringMetaDataList.add(stringMetaDataBo);
        }
        return stringMetaDataList;
    }


    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
