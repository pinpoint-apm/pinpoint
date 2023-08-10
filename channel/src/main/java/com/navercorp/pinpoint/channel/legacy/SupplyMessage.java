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
package com.navercorp.pinpoint.channel.legacy;

/**
 * @author youngjin.kim2
 */
public class SupplyMessage<S> {

    private Identifier demandId;
    private int sequence;
    private S content;
    private boolean terminated;

    public Identifier getDemandId() {
        return demandId;
    }

    public void setDemandId(Identifier demandId) {
        this.demandId = demandId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public S getContent() {
        return content;
    }

    public void setContent(S content) {
        this.content = content;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

}
