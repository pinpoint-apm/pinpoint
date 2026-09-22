package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.util.ApplicationRowKeyUtils;
import com.navercorp.pinpoint.common.server.bo.Application;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApplicationMapper implements RowMapper<List<Application>> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ApplicationFactory applicationFactory;

    public ApplicationMapper(ApplicationFactory applicationFactory) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public List<Application> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        byte[] row = result.getRow();
        try {
            int serviceUid = ApplicationRowKeyUtils.extractServiceUid(row);
            String applicationName = ApplicationRowKeyUtils.extractApplicationName(row);
            int serviceTypeCode = ApplicationRowKeyUtils.extractServiceTypeCode(row);
            return List.of(applicationFactory.createApplication(serviceUid, applicationName, serviceTypeCode));
        } catch (Exception e) {
            logger.warn("Failed to parse application row key. rowKey={}, message={}", Bytes.toStringBinary(row), e.getMessage());
            return Collections.emptyList();
        }
    }
}
