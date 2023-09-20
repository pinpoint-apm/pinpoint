/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.common.hbase.config;

public class HbaseMultiplexerProperties {

    private boolean enable = false;
    private int inQueueSize = 10000;

    public HbaseMultiplexerProperties() {
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getInQueueSize() {
        return inQueueSize;
    }

    public void setInQueueSize(int inQueueSize) {
        this.inQueueSize = inQueueSize;
    }

    @Override
    public String toString() {
        return "HbaseAsyncProperties{" +
                "enable=" + enable +
                ", inQueueSize=" + inQueueSize +
                '}';
    }
}
