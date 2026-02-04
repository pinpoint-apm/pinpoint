package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.util.ApplicationRowKeyUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class ApplicationMapper implements RowMapper<List<Application>> {

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
        int serviceUid = ApplicationRowKeyUtils.extractServiceUid(row);
        String applicationName = ApplicationRowKeyUtils.extractApplicationName(row);
        int serviceTypeCode = ApplicationRowKeyUtils.extractServiceTypeCode(row);
        return List.of(applicationFactory.createApplication(serviceUid, applicationName, serviceTypeCode));
    }
}
