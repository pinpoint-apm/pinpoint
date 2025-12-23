package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ApplicationMapper implements RowMapper<Application> {

    private final ApplicationFactory applicationFactory;

    public ApplicationMapper(ApplicationFactory applicationFactory) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public Application mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();
        Buffer buffer = new FixedBuffer(rowKey);
        buffer.readInt(); //serviceUid
        String applicationName = buffer.readPadStringAndRightTrim(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3); //applicationName
        int serviceTypeCode = buffer.readInt(); //serviceTypeCode
        return applicationFactory.createApplication(applicationName, serviceTypeCode);
    }
}
