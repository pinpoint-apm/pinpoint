/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author Roy Kim
 */
public class FileDescriptorBo extends AbstractStatDataPoint {


    private final long openFileDescriptorCount;

    public FileDescriptorBo(DataPoint point, long openFileDescriptorCount) {
        super(point);
        this.openFileDescriptorCount = openFileDescriptorCount;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.FILE_DESCRIPTOR;
    }

    public long getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    @Override
    public String toString() {
        return "FileDescriptorBo{" +
                "point=" + point +
                ", openFileDescriptorCount=" + openFileDescriptorCount +
                '}';
    }
}
