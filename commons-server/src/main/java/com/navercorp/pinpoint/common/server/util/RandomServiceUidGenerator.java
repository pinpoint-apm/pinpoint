package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;

import java.security.SecureRandom;
import java.util.Random;

public class RandomServiceUidGenerator implements IdGenerator<ServiceUid> {

    private static final long unsignedIntMax = 0xffffffffL; //((long) Integer.MAX_VALUE << 1) + 1L;

    private final Random random = new SecureRandom();

    @Override
    public ServiceUid generate() {
        // int [0x00000000 ~ 0xffffffff], random.nextLong [0, 0xffffffffL + 1)
        //
        // reserve 0 for default service id
        // reserve n negative values for error service ids
        long uidLong = random.nextLong(1, (unsignedIntMax + 1) - ServiceUid.RESERVED_NEGATIVE_UID_COUNT);

        return ServiceUid.of((int) uidLong);
    }
}
