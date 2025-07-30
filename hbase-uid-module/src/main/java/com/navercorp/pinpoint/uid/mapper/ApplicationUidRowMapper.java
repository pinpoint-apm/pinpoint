package com.navercorp.pinpoint.uid.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUidRowMapper implements RowMapper<ApplicationUidAttribute> {

    @Override
    public ApplicationUidAttribute mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        return UidBytesParseUtils.parseApplicationUidAttrFromRowKey(rowKey);
    }
}
