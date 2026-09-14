package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmApplicationResolverTest {

    private static final AlarmApplication APPLICATION =
            new AlarmApplication("service", "app", "javascript");

    private static AlarmApplicationExistenceChecker checker(String ownedType, boolean exists) {
        return new AlarmApplicationExistenceChecker() {
            @Override
            public Set<String> supportedTypes() {
                return Set.of(ownedType);
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
                List.of(checker("java", false), checker("javascript", true)));

        assertDoesNotThrow(() -> resolver.verifyExists(APPLICATION));
    }

    @Test
    void missingApplicationIsNotFound() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("javascript", false)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> resolver.verifyExists(APPLICATION));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void unclaimedTypeIsABadRequest() {
        AlarmApplicationResolver resolver = new AlarmApplicationResolver(
                List.of(checker("java", true)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> resolver.verifyExists(APPLICATION));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    // Letting bean order decide would validate the target against whichever registry
    // happened to be injected first, accepting or rejecting it for the wrong reason.
    // Caught while wiring rather than on the first request that names the type.
    @Test
    void twoCheckersClaimingOneTypeIsRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new AlarmApplicationResolver(
                        List.of(checker("javascript", true), checker("javascript", false))));
        assertTrue(e.getMessage().contains("javascript"), e.getMessage());
    }
}
