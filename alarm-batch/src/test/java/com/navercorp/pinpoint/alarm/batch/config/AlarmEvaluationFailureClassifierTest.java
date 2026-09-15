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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlarmEvaluationFailureClassifierTest {

    private final AlarmEvaluationFailureClassifier classifier = new AlarmEvaluationFailureClassifier();

    // The validators throw IllegalArgumentException, but by the time the failure reaches the
    // tasklet it is usually wrapped, so only the whole cause chain identifies it.
    @Test
    void aWrappedConfigurationFailureIsStillAConfigurationFailure() {
        RuntimeException failure = new IllegalStateException(
                "outer", new IllegalArgumentException("unsupported metric"));

        assertEquals(AlarmEvaluationFailureType.RULE_CONFIGURATION, classifier.classify(failure));
    }

    // Reported to nobody: every rule against the same backend would say this at once.
    @Test
    void anythingElseIsInfrastructure() {
        assertEquals(AlarmEvaluationFailureType.INFRASTRUCTURE,
                classifier.classify(new IllegalStateException("metric query failed")));
    }

    /**
     * A rule whose data source no installed module owns is the rule owner's to fix, so it has
     * to reach them. Both places that detect it raise IllegalArgumentException for that reason
     * -- the registry when it cannot resolve a persisted code, and the job when no module
     * contributed a metric query service for one -- and reporting either as infrastructure
     * would record the failure without telling anyone.
     */
    @Test
    void aDataSourceNoModuleOwnsIsTheRuleOwnersToFix() {
        assertEquals(AlarmEvaluationFailureType.RULE_CONFIGURATION,
                classifier.classify(new IllegalArgumentException(
                        "No metric query service for dataSource=NOT_INSTALLED")));
        assertEquals(AlarmEvaluationFailureType.RULE_CONFIGURATION,
                classifier.classify(new IllegalArgumentException(
                        "Unknown alarm data source: NOT_INSTALLED")));
    }
}
