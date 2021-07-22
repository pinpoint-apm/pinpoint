package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;

public interface ApiParser {
    Api parse(ApiMetaDataBo apiMetadata);
}
