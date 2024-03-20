package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;

public class ApplicationNameRowKeyEncoder extends IdRowKeyEncoder {

    public ApplicationNameRowKeyEncoder() {
        super(HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
    }

}
