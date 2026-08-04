package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.security.SecureRandom;
import java.util.Random;

public class RandomServiceUidGenerator implements IdGenerator<ServiceUid> {

    private final Random random = new SecureRandom();

    @Override
    public ServiceUid generate() {
        int serviceUid;
        do {
            serviceUid = random.nextInt();
        } while (ServiceUid.isReservedUid(serviceUid));

        return ServiceUid.of(serviceUid);
    }
}
