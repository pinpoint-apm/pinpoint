package com.navercorp.pinpoint.uid.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttrRow;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUidAttrRowMapper implements RowMapper<ApplicationUidAttrRow> {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION_UID_ATTR;

    @Override
    public ApplicationUidAttrRow mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        ServiceUid serviceUid = UidBytesParseUtils.parseServiceUidFromRowKey(rowKey);
        ApplicationUid applicationUid = UidBytesParseUtils.parseApplicationUidFromRowKey(rowKey);

        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        long timeStamp = cell.getTimestamp();
        ApplicationUidAttribute applicationUidAttribute = UidBytesParseUtils.parseApplicationUidAttrFromValue(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        return new ApplicationUidAttrRow(serviceUid, applicationUid, timeStamp, applicationUidAttribute);
    }
}
