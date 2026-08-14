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
package com.navercorp.pinpoint.plugin.reactorsupport;

import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

/** Test double for the scope-depth contract: active while entered more often than left. */
final class DepthScope implements TraceScope {
    private int depth;

    @Override
    public String getName() {
        return ScopeUtils.ASYNC_TRACE_SCOPE;
    }

    @Override
    public boolean tryEnter() {
        depth++;
        return true;
    }

    @Override
    public boolean canLeave() {
        return depth > 0;
    }

    @Override
    public void leave() {
        depth--;
    }

    @Override
    public boolean isActive() {
        return depth > 0;
    }
}
