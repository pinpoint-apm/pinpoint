package com.navercorp.pinpoint.uid.vo;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public record ApplicationUidAttrRow(ServiceUid serviceUid, ApplicationUid applicationUid, long timeStamp,
                                    ApplicationUidAttribute applicationUidAttribute) {
}
