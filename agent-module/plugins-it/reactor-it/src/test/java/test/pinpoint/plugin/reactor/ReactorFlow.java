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

package test.pinpoint.plugin.reactor;

/**
 * Root entry point for the reactor propagation IT.
 * <p>
 * Registered through {@code profiler.entrypoint} so that a trace is already active when the
 * reactive chain is assembled and subscribed.
 * <p>
 * This matters for the assertions: {@code Echo.get} is also an entrypoint, so it becomes an
 * {@code INTERNAL_METHOD} span event when a trace is active, but starts its own
 * {@code STAND_ALONE} root trace when none is. Running the whole flow inside this method
 * therefore makes propagation success and failure distinguishable - without it the chain runs
 * with no active trace and every {@code Echo.get} looks the same either way.
 */
public class ReactorFlow {

    public void execute(Runnable flow) {
        flow.run();
    }
}
