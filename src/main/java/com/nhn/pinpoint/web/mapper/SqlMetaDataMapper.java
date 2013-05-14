package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.profiler.common.bo.SqlMetaDataBo;

/**
 *
 */
@Component
public class SqlMetaDataMapper implements RowMapper<List<SqlMetaDataBo>> {
    @Override
    public List<SqlMetaDataBo> mapRow(Result result, int rowNum) throws Exception {

        byte[] rowKey = result.getRow();

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
}
