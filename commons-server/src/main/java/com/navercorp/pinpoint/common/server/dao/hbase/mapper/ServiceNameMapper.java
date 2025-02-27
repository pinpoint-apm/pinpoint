package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ServiceNameMapper implements RowMapper<String> {

    private static final HbaseColumnFamily.ServiceName NAME = HbaseColumnFamily.SERVICE_NAME;

    @Override
    public String mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        Cell cell = result.getColumnLatestCell(NAME.getName(), NAME.getName());
        return CellUtils.valueToString(cell);
    }
}
