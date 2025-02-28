package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;

import java.security.SecureRandom;

public class RandomApplicationIdGenerator implements IdGenerator<ApplicationUid> {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ApplicationUid generate() {
        long idLong = secureRandom.nextLong();
        return new ApplicationUid(idLong);
    }
}
