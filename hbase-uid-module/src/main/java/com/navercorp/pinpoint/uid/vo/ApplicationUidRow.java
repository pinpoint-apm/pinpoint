package com.navercorp.pinpoint.uid.vo;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public record ApplicationUidRow(ServiceUid serviceUid, String applicationName, int serviceTypeCode,
                                ApplicationUid applicationUid) {
}
