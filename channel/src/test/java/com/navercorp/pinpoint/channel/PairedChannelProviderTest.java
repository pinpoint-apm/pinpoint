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
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
public class PairedChannelProviderTest {

    @Test
    public void shouldDelegate() {
        PubChannelProvider pub = Mockito.mock(PubChannelProvider.class);
        SubChannelProvider sub = Mockito.mock(SubChannelProvider.class);
        ChannelProvider paired = new PairedChannelProvider(pub, sub);
        paired.getPubChannel("key");
        paired.getSubChannel("key");
        verify(pub, times(1)).getPubChannel(eq("key"));
        verify(sub, times(1)).getSubChannel(eq("key"));
    }

}
