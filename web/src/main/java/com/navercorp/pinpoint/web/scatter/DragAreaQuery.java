/*
 * Copyright 2025 NAVER Corp.
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
 */

package com.navercorp.pinpoint.web.scatter;


import com.navercorp.pinpoint.web.scatter.vo.Dot;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class DragAreaQuery {

    private final DragArea dragArea;

    private final @Nullable String agentId;

    private final Dot.@Nullable Status dotStatus;

    public DragAreaQuery(DragArea dragArea, String agentId, Dot.Status dotStatus) {
        this.dragArea = Objects.requireNonNull(dragArea, "dragArea");
        this.agentId = agentId;
        this.dotStatus = dotStatus;
    }

    public DragAreaQuery(DragArea dragArea) {
        this(dragArea, null, null);
    }

    public DragArea getDragArea() {
        return dragArea;
    }

    public String getAgentId() {
        return agentId;
    }

    public Dot.Status getDotStatus() {
        return dotStatus;
    }

    @Override
    public String toString() {
        return "DragAreaQuery{" +
                "dragArea=" + dragArea +
                ", agentId='" + agentId + '\'' +
                ", dotStatus=" + dotStatus +
                '}';
    }
}
