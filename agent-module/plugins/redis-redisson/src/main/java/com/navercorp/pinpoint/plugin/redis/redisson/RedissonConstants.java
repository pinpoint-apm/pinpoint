/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.redisson;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

/**
 * @author jaehong.kim
 */
public class RedissonConstants {
    public static final ServiceType REDISSON = ServiceTypeProvider.getByName("REDIS_REDISSON");
    public static final ServiceType REDISSON_INTERNAL = ServiceTypeProvider.getByName("REDIS_REDISSON_INTERNAL");
    public static final ServiceType REDISSON_REACTIVE = ServiceTypeProvider.getByName("REDIS_REDISSON_REACTIVE");
    public static final String REDISSON_SCOPE = "redisRedissonScope";

}
