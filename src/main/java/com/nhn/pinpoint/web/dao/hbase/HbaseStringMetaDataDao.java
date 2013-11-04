package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.bo.StringMetaDataBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.web.dao.StringMetaDataDao;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author emeroad
 */
@Repository
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("stringMetaDataMapper")
    private RowMapper<List<StringMetaDataBo>> stringMetaDataMapper;

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId) {
        StringMetaDataBo stringMetaData = new StringMetaDataBo(agentId, time, stringId);
        byte[] rowKey = getDistributedKey(stringMetaData.toRowKey());

        Get get = new Get(rowKey);
        get.addFamily(HBaseTables.STRING_METADATA_CF_STR);

        return hbaseOperations2.get(HBaseTables.STRING_METADATA, get, stringMetaDataMapper);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
