/*
 * Copyright 2026 NAVER Corp.
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
package test.pinpoint.plugin.r2dbc;

/**
 * Instrumented via {@code profiler.entrypoint} — the recorded INTERNAL_METHOD event marks where a
 * downstream callback actually ran, so the IT can assert the async link to the seam event.
 */
public class Echo {

    public <T> T get(T value) {
        return value;
    }
}
