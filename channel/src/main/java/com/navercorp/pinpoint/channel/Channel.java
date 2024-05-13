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
 * Channel is publishable channel, and subscribable channel at the same time.
 * If a pair of PubChannel, and SubChannel are bound in a single channel interface, the two channel
 * should be able to communicate with each other.
 * <br>
 * In most cases, A paired PubChannel, and SubChannel are located at the different side of the network, and
 * implemented with distributed systems like Redis, Kafka, etc.
 *
 * @see PubChannel
 * @see SubChannel
 */
public interface Channel extends PubChannel, SubChannel {
}
