package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import com.nhn.pinpoint.common.bo.StringMetaDataBo;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
public class StringMetaDataMapper implements RowMapper<List<StringMetaDataBo>> {
    @Override
    public List<StringMetaDataBo> mapRow(Result result, int rowNum) throws Exception {

        byte[] rowKey = result.getRow();

        List<StringMetaDataBo> stringMetaDataList = new ArrayList<StringMetaDataBo>();
        KeyValue[] keyList = result.raw();
        for (KeyValue keyValue : keyList) {
            StringMetaDataBo sqlMetaDataBo = new StringMetaDataBo();
            sqlMetaDataBo.readRowKey(rowKey);
            String stringValue = Bytes.toString(keyValue.getBuffer(), keyValue.getQualifierOffset(), keyValue.getQualifierLength());
            sqlMetaDataBo.setStringValue(stringValue);
            stringMetaDataList.add(sqlMetaDataBo);
        }
        return stringMetaDataList;
    }
}
