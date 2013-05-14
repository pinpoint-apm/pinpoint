package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.List;

import com.profiler.common.buffer.FixedBuffer;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.profiler.common.bo.ApiMetaDataBo;
import com.profiler.common.buffer.Buffer;

/**
 *
 */
@Component
public class ApiMetaDataMapper implements RowMapper<List<ApiMetaDataBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<ApiMetaDataBo> mapRow(Result result, int rowNum) throws Exception {

        byte[] rowKey = result.getRow();

        List<ApiMetaDataBo> apiMetaDataList = new ArrayList<ApiMetaDataBo>();
        KeyValue[] keyList = result.raw();
        for (KeyValue keyValue : keyList) {
            ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo();
            apiMetaDataBo.readRowKey(rowKey);
            byte[] qualifier = keyValue.getQualifier();
            Buffer buffer = new FixedBuffer(qualifier);
            String apiInfo = buffer.readPrefixedString();
            int lineNumber = buffer.readInt();
            apiMetaDataBo.setApiInfo(apiInfo);
            apiMetaDataBo.setLineNumber(lineNumber);
            apiMetaDataList.add(apiMetaDataBo);
            if (logger.isDebugEnabled()) {
                logger.debug("read apiAnnotation:{}", apiMetaDataBo);
            }
        }
        return apiMetaDataList;
    }
}

