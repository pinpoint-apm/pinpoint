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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class ChannelProviderTest {

    @Test
    public void memChannelTest() {
        testChannelProvider(new MemoryChannelProvider());
    }

    public static void testChannelProvider(ChannelProvider provider) {
        AtomicReference<String> target = new AtomicReference<>("original");

        SubChannel sub = provider.getSubChannel("key");
        sub.subscribe(el -> {
            target.set(new String(el));
            return true;
        });

        PubChannel pub = provider.getPubChannel("key");
        pub.publish("new bytes".getBytes());

        try {
            Thread.sleep(0);
        } catch (Exception ignored) {}

        assertThat(target.get()).isEqualTo("new bytes");
    }

}
