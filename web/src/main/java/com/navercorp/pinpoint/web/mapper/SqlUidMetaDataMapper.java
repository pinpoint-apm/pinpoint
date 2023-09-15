package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetadataDecoder;
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

@Component
public class SqlUidMetaDataMapper implements RowMapper<List<SqlUidMetaDataBo>> {
    private final static String SQL_UID_METADATA_CF_SQL_QUALI_SQLSTATEMENT = Bytes.toString(HbaseColumnFamily.SQL_UID_METADATA_SQL.QUALIFIER_SQLSTATEMENT);

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final RowKeyDecoder<UidMetaDataRowKey> decoder = new UidMetadataDecoder();

    public SqlUidMetaDataMapper(@Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public List<SqlUidMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        final UidMetaDataRowKey key = decoder.decodeRowKey(rowKey);

        List<SqlUidMetaDataBo> sqlUidMetaDataList = new ArrayList<>();

        for (Cell cell : result.rawCells()) {
            String sql = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

            if (SQL_UID_METADATA_CF_SQL_QUALI_SQLSTATEMENT.equals(sql)) {
                sql = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }

            SqlUidMetaDataBo sqlUidMetaDataBo = new SqlUidMetaDataBo(key.getAgentId(), key.getAgentStartTime(), key.getUid(), sql);
            sqlUidMetaDataList.add(sqlUidMetaDataBo);
        }
        return sqlUidMetaDataList;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }

}
