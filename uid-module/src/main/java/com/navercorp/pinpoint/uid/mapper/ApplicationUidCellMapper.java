package com.navercorp.pinpoint.uid.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUidCellMapper implements RowMapper<HbaseCellData> {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION_UID;

    @Override
    public HbaseCellData mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        ApplicationUid applicationUid = ApplicationUid.of(CellUtils.valueToLong(cell));
        return new HbaseCellData(rowKey, cell.getTimestamp(), applicationUid);
    }
}
