package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationIdentifierMapper implements RowMapper<ApplicationIdentifier> {

    private static final HbaseColumnFamily.ApplicationUid DESCRIPTOR = HbaseColumnFamily.APPLICATION_UID;

    @Override
    public ApplicationIdentifier mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        int serviceUid = ApplicationUidRowKeyUtils.getServiceUidFromRowKey(result.getRow());

        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        long applicationUid = CellUtils.valueToLong(cell);
        return new ApplicationIdentifier(serviceUid, applicationUid);
    }
}
