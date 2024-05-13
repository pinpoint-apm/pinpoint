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

/**
 * @author youngjin.kim2
 *
 * ChannelProvider provides PubChannel, and SubChannel by the key.
 * <br>
 * In distributed system, A pair of PubChannel, and SubChannel are connected each other if they have the same key.
 * In the other word, even if the two processes are located at the different side of the network, they can communicate
 * with each other if they have the same key.
 */
public interface ChannelProvider extends PubChannelProvider, SubChannelProvider {
    static ChannelProvider pair(PubChannelProvider pub, SubChannelProvider sub) {
        return new PairedChannelProvider(pub, sub);
    }

}
