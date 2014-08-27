package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;

/**
 * @author emeroad
 */
@Component
public class SqlMetaDataMapper implements RowMapper<List<SqlMetaDataBo>> {

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<SqlMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        List<SqlMetaDataBo> sqlMetaDataList = new ArrayList<SqlMetaDataBo>();
        KeyValue[] keyList = result.raw();
        for (KeyValue keyValue : keyList) {
            SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo();
            sqlMetaDataBo.readRowKey(rowKey);
            String sql = Bytes.toString(keyValue.getBuffer(), keyValue.getQualifierOffset(), keyValue.getQualifierLength());
            sqlMetaDataBo.setSql(sql);
            sqlMetaDataList.add(sqlMetaDataBo);
        }
        return sqlMetaDataList;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
