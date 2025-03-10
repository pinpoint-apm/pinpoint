package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.security.SecureRandom;
import java.util.Random;

public class RandomServiceUidGenerator implements IdGenerator<ServiceUid> {

    private final Random random = new SecureRandom();

    @Override
    public ServiceUid generate() {
        int randomInt;
        do {
            randomInt = random.nextInt();
        } while (isReservedServiceUid(randomInt));

        return ServiceUid.of(randomInt);
    }

    private boolean isReservedServiceUid(int uid) {
        return -ServiceUid.RESERVED_NEGATIVE_UID_COUNT <= uid && uid <= ServiceUid.RESERVED_POSITIVE_UID_COUNT;
    }
}
