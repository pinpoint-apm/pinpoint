package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import jakarta.annotation.Nullable;

public interface ApplicationCleanupService {

    int cleanupInconsistentApplicationUid(@Nullable ServiceUid serviceUid);
}
