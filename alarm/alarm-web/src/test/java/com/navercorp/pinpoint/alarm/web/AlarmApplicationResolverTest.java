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
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmApplicationResolverTest {

    private static final AlarmApplication APPLICATION =
            new AlarmApplication("service", "app", "javascript");

    private static ApplicationIndexExistenceChecker applicationIndex(boolean exists) {
        ApplicationIndexExistenceChecker index = mock(ApplicationIndexExistenceChecker.class);
        when(index.exists(any())).thenReturn(exists);
        return index;
    }

    private static AlarmApplicationExistenceChecker checker(String ownedType, boolean exists) {
        return new AlarmApplicationExistenceChecker() {
            @Override
            public Set<String> supportedTypes() {
                return Set.of(ownedType);
            }

            @Override
            public String category() {
                return "OTHER";
            }

            @Override
            public boolean exists(AlarmApplication application) {
                return exists;
            }
        };
    }

    @Test
    void asksTheCheckerThatOwnsTheType() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("java", false), checker("javascript", true)),
                applicationIndex(false));

        assertDoesNotThrow(() -> resolver.verifyExists(APPLICATION));
    }

    @Test
    void missingApplicationIsNotFound() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("javascript", false)), applicationIndex(true));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> resolver.verifyExists(APPLICATION));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    // Letting bean order decide would validate the target against whichever registry
    // happened to be injected first, accepting or rejecting it for the wrong reason.
    // Caught while wiring rather than on the first request that names the type.
    @Test
    void twoCheckersClaimingOneTypeIsRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new AlarmApplicationResolver(
                        List.of(checker("javascript", true), checker("javascript", false)),
                        applicationIndex(false)));
        assertTrue(e.getMessage().contains("javascript"), e.getMessage());
    }

    @Test
    void aTypeTakesTheCategoryOfWhatAnswersForIt() {
        ApplicationIndexExistenceChecker index = applicationIndex(true);
        when(index.category()).thenReturn(AlarmDataSource.APM_CATEGORY);
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(List.of(checker("javascript", false)), index);

        assertEquals("OTHER", resolver.categoryOf("javascript"));
        assertEquals(AlarmDataSource.APM_CATEGORY, resolver.categoryOf("SPRING_BOOT"));
    }

    @Test
    void aTypeNoCheckerClaimsGoesToTheApplicationIndex() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("javascript", false)), applicationIndex(true));

        assertDoesNotThrow(() ->
                resolver.verifyExists(new AlarmApplication("service", "app", "SPRING_BOOT")));
    }

    @Test
    void aClaimedTypeStillGoesToItsClaimer() {
        // The claimer says no and the index says yes, so a 404 proves which answered.
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("javascript", false)), applicationIndex(true));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> resolver.verifyExists(APPLICATION));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void anUnknownTypeIsNotFoundRatherThanBadRequestOnceSomethingAnswersForIt() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(List.of(), applicationIndex(false));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> resolver.verifyExists(new AlarmApplication("service", "app", "NO_SUCH_TYPE")));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
