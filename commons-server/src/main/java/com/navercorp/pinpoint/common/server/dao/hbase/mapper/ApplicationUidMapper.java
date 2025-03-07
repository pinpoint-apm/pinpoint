package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUidMapper implements RowMapper<ApplicationUid> {

    private static final HbaseColumnFamily.ApplicationUid DESCRIPTOR = HbaseColumnFamily.APPLICATION_UID;

    @Override
    public ApplicationUid mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        long applicationUid = CellUtils.valueToLong(cell);
        return ApplicationUid.of(applicationUid);
    }
}
