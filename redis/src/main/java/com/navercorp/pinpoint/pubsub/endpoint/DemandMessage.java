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
public class DemandMessage<D> {

    private final Identifier id;
    private final D content;

    private DemandMessage(Identifier id, D content) {
        this.id = id;
        this.content = content;
    }

    public Identifier getId() {
        return id;
    }

    public D getContent() {
        return content;
    }

    public static <D> DemandMessage<D> ok(Identifier id, D content) {
        return new DemandMessage<>(id, content);
    }

}
