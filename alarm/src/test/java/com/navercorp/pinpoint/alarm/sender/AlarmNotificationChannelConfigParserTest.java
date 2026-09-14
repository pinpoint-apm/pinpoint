package com.navercorp.pinpoint.alarm.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmNotificationChannelConfigParserTest {

    private final AlarmNotificationChannelConfigParser parser =
            new AlarmNotificationChannelConfigParser(new ObjectMapper());

    @Test
    void parseIgnoresUnknownFields() {
        AlarmNotificationChannelConfig config = parser.parse(AlarmMethodType.WEBHOOK, """
                {"format":"SLACK","title":"title","template":"body","futureOption":true}
                """);

        assertEquals(new AlarmNotificationChannelConfig("SLACK", "title", "body"), config);
    }

    @ParameterizedTest
    @MethodSource("normalizedFormats")
    void parseNormalizesSupportedFormatCase(String input, String expected) {
        AlarmNotificationChannelConfig config = parser.parse(
                AlarmMethodType.WEBHOOK, "{\"format\":\"" + input + "\"}");

        assertEquals(expected, config.format());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void parseNullOrBlankReturnsEmptyConfig(String input) {
        assertEquals(AlarmNotificationChannelConfig.empty(), parser.parse(AlarmMethodType.EMAIL, input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMAIL", "SMS"})
    void parseRejectsWebhookFormatForNonWebhookChannel(String methodType) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(AlarmMethodType.valueOf(methodType), "{\"format\":\"SLACK\"}")
        );

        assertEquals(
                "notification channel config format is only supported for WEBHOOK channels",
                exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidConfigs")
    void parseRejectsInvalidConfig(String input, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(AlarmMethodType.WEBHOOK, input)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static Stream<Arguments> normalizedFormats() {
        return Stream.of(
                Arguments.of("default", "DEFAULT"),
                Arguments.of("sLaCk", "SLACK")
        );
    }

    private static Stream<Arguments> invalidConfigs() {
        return Stream.of(
                Arguments.of("{not-json",
                        "notification channel config must be a valid JSON object"),
                Arguments.of("{} {}",
                        "notification channel config must be a valid JSON object"),
                Arguments.of("null",
                        "notification channel config must be a JSON object"),
                Arguments.of("[]",
                        "notification channel config must be a JSON object"),
                Arguments.of("{\"format\":false}",
                        "notification channel config format must be a string"),
                Arguments.of("{\"title\":1}",
                        "notification channel config title must be a string"),
                Arguments.of("{\"template\":{}}",
                        "notification channel config template must be a string"),
                Arguments.of("{\"format\":\"TEAMS\"}",
                        "notification channel config format must be DEFAULT or SLACK")
        );
    }
}
