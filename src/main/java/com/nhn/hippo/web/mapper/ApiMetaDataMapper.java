package com.nhn.hippo.web.mapper;

import com.profiler.common.bo.ApiMetaDataBo;
import com.profiler.common.bo.SqlMetaDataBo;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.RowKeyUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            ApiMetaDataBo apiMetaDataBo = RowKeyUtils.parseApiId(rowKey);
            byte[] qualifier = keyValue.getQualifier();
            Buffer buffer = new Buffer(qualifier);
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

