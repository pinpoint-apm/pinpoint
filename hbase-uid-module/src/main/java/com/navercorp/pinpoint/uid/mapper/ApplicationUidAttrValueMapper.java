package com.navercorp.pinpoint.uid.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUidAttrValueMapper implements RowMapper<ApplicationUidAttribute> {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION_UID_ATTR;

    @Override
    public ApplicationUidAttribute mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());

        return UidBytesParseUtils.parseApplicationUidAttrFromValue(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }
}
