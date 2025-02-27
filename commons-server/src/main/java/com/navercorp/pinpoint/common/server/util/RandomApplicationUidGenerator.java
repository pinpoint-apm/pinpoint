package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;

import java.security.SecureRandom;
import java.util.Random;

public class RandomApplicationUidGenerator implements IdGenerator<ApplicationUid> {

    private final Random random = new SecureRandom();

    @Override
    public ApplicationUid generate() {
        // ServiceUid range long [Long.MIN_VALUE, Long.MAX_VALUE], random.nextLong[Long.MIN_VALUE, Long.MAX_VALUE + 1)
        // remove reserved uid
        long idLong = random.nextLong(Long.MIN_VALUE + ApplicationUid.RESERVED_NEGATIVE_UID_COUNT, Long.MAX_VALUE - ApplicationUid.RESERVED_POSITIVE_UID_COUNT + 1);
        return ApplicationUid.of(idLong);
    }
}
