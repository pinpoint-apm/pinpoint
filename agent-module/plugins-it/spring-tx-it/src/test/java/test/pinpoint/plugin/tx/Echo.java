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

package test.pinpoint.plugin.tx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * profiler.entrypoint leaf for the seam-wrap async-link assertions - an INTERNAL_METHOD event
 * when the trace propagated, a STAND_ALONE root when it did not (see reactor-it's Echo).
 */
public class Echo {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public String get(String message) {
        logger.info("echo:{}", message);
        return message;
    }
}
