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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.common.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class ApiMetaDataMapper implements RowMapper<List<ApiMetaDataBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;
    
    private final static String API_METADATA_CF_API_QUALI_SIGNATURE  = Bytes.toString(HBaseTables.API_METADATA_CF_API_QUALI_SIGNATURE); 

    @Override
    public List<ApiMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        List<ApiMetaDataBo> apiMetaDataList = new ArrayList<>();
        for (Cell cell : result.rawCells()) {
            ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo();
            apiMetaDataBo.readRowKey(rowKey);

            byte[] qualifier = CellUtil.cloneQualifier(cell);
            byte[] value = null;
            
            if (API_METADATA_CF_API_QUALI_SIGNATURE.equals(Bytes.toString(qualifier))) {
                value = CellUtil.cloneValue(cell);
            } else {
                value = qualifier;
            }
            
            Buffer buffer = new FixedBuffer(value);
            String apiInfo = buffer.readPrefixedString();
            int lineNumber = buffer.readInt();
            int type = 0;
            if (buffer.limit() > 0) {
                type = buffer.readInt();
            }
            apiMetaDataBo.setApiInfo(apiInfo);
            apiMetaDataBo.setLineNumber(lineNumber);
            apiMetaDataBo.setType(type);
            apiMetaDataList.add(apiMetaDataBo);
            if (logger.isDebugEnabled()) {
                logger.debug("read apiAnnotation:{}", apiMetaDataBo);
            }
        }
        return apiMetaDataList;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }


}

