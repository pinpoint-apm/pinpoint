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
package com.navercorp.pinpoint.pubsub.endpoint;

/**
 * @author youngjin.kim2
 */
public class SupplyMessage<S> {

    private final Identifier demandId;
    private final int sequence;
    private final S content;
    private final boolean terminated;

    private SupplyMessage(Identifier demandId, int sequence, S content, boolean terminated) {
        this.demandId = demandId;
        this.sequence = sequence;
        this.content = content;
        this.terminated = terminated;
    }

    public Identifier getDemandId() {
        return demandId;
    }

    public int getSequence() {
        return sequence;
    }

    public S getContent() {
        return content;
    }

    public boolean isTerminated() {
        return terminated;
    }

    static <S> SupplyMessage<S> ok(Identifier demandId, int sequence, S content) {
        return new SupplyMessage<>(demandId, sequence, content, false);
    }

    static <S> SupplyMessage<S> terminated(Identifier demandId, int sequence) {
        return new SupplyMessage<>(demandId, sequence, null, true);
    }

}
