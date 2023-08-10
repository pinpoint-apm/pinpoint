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
package com.navercorp.pinpoint.channel;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class PairedChannelProvider implements ChannelProvider {

    private final PubChannelProvider pub;
    private final SubChannelProvider sub;

    public PairedChannelProvider(PubChannelProvider pub, SubChannelProvider sub) {
        this.pub = Objects.requireNonNull(pub, "pub");
        this.sub = Objects.requireNonNull(sub, "sub");
    }

    @Override
    public PubChannel getPubChannel(String key) {
        return this.pub.getPubChannel(key);
    }

    @Override
    public SubChannel getSubChannel(String key) {
        return this.sub.getSubChannel(key);
    }

}
