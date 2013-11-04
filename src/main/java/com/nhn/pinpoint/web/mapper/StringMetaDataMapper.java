package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import com.nhn.pinpoint.common.bo.StringMetaDataBo;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
@Component
public class StringMetaDataMapper implements RowMapper<List<StringMetaDataBo>> {

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<StringMetaDataBo> mapRow(Result result, int rowNum) throws Exception {

        final byte[] rowKey = getOriginalKey(result.getRow());

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

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
