/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CreateNodeMessage {

    private final String nodePath;
    private final byte[] data;
    private final boolean creatingParentPathsIfNeeded;

    public CreateNodeMessage(String nodePath, byte[] data) {
        this(nodePath, data, false);
    }

    public CreateNodeMessage(String nodePath, byte[] data, boolean creatingParentPathsIfNeeded) {
        Assert.isTrue(StringUtils.hasLength(nodePath), "nodePath must not be empty");
        this.nodePath = nodePath;

        this.data = Objects.requireNonNull(data, "data");
        this.creatingParentPathsIfNeeded = creatingParentPathsIfNeeded;
    }

    public String getNodePath() {
        return nodePath;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isCreatingParentPathsIfNeeded() {
        return creatingParentPathsIfNeeded;
    }

    @Override
    public String toString() {
        return "CreateNodeMessage{" +
                "nodePath='" + nodePath + '\'' +
                ", dataSize=" + data.length +
                ", creatingParentPathsIfNeeded=" + creatingParentPathsIfNeeded +
                '}';
    }

}
