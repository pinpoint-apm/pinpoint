package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.jspecify.annotations.Nullable;

@Deprecated
public interface ApplicationCleanupService {

    int cleanupInconsistentApplicationUid(@Nullable ServiceUid serviceUid);
}
