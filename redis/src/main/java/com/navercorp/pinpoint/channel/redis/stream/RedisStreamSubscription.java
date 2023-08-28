/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.channel.redis.stream;

import com.navercorp.pinpoint.channel.AbstractSubscription;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubscription extends AbstractSubscription {

    private final org.springframework.data.redis.stream.Subscription redisSubscription;
    private final String groupName;

    RedisStreamSubscription(
            RedisStreamSubChannel subChannel,
            org.springframework.data.redis.stream.Subscription redisSubscription,
            String groupName
    ) {
        super(subChannel);
        this.redisSubscription = redisSubscription;
        this.groupName = groupName;
    }

    org.springframework.data.redis.stream.Subscription getRedisSubscription() {
        return redisSubscription;
    }

    public String getGroupName() {
        return groupName;
    }

}
