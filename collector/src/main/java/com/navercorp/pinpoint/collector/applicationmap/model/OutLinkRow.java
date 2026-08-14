/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.model;

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;

import java.util.Objects;

/**
 * selfVertex -> outVertex/outHost
 */
public record OutLinkRow(Vertex selfVertex,
                         Vertex outVertex, String outHost,
                         int elapsed, boolean error) {
    public OutLinkRow {
        Objects.requireNonNull(selfVertex, "selfVertex");
        Objects.requireNonNull(outVertex, "outVertex");
    }

    @Override
    public String toString() {
        return "OutLink{" + selfVertex + " -> " + outVertex + '/' + outHost +
                " elapsed=" + elapsed + " error=" + error + '}';
    }
}