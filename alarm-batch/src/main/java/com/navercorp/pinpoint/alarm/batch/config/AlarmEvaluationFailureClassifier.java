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
package com.navercorp.pinpoint.alarm.batch.config;

import java.util.Objects;

/**
 * Splits evaluation failures into the ones a rule owner can act on and the ones they cannot.
 *
 * <p>A misconfigured rule is reported to its channels, because nobody else will notice it;
 * a backend outage is not, because every rule against that backend would report the same
 * thing at once.
 */
public class AlarmEvaluationFailureClassifier {

    public AlarmEvaluationFailureType classify(RuntimeException failure) {
        Objects.requireNonNull(failure, "failure");
        // The validators and the effective-rule resolver signal a rule that cannot be
        // evaluated as configured with IllegalArgumentException; anything else is the
        // backend failing underneath us.
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof IllegalArgumentException) {
                return AlarmEvaluationFailureType.RULE_CONFIGURATION;
            }
        }
        return AlarmEvaluationFailureType.INFRASTRUCTURE;
    }
}
