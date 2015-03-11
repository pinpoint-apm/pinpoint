package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.common.plugin.TypeProvider;
import com.navercorp.pinpoint.common.plugin.TypeSetupContext;

public class RedisTypeProvider implements TypeProvider, RedisConstants{

    @Override
    public void setUp(TypeSetupContext context) {
        context.addType(REDIS);
    }

}
