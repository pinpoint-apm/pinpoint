package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;

import java.security.SecureRandom;
import java.util.Random;

public class RandomApplicationUidGenerator implements IdGenerator<ApplicationUid> {

    private final Random random = new SecureRandom();

    @Override
    public ApplicationUid generate() {
        long randomLong;
        do {
            randomLong = random.nextLong();
        } while (isReservedApplicationUid(randomLong));

        return ApplicationUid.of(randomLong);
    }

    private boolean isReservedApplicationUid(long uid) {
        return -ApplicationUid.RESERVED_NEGATIVE_UID_COUNT <= uid && uid <= ApplicationUid.RESERVED_POSITIVE_UID_COUNT;
    }
}
