package com.navercorp.pinpoint.service.web.controller.vo;

import com.navercorp.pinpoint.common.PinpointConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceNameRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validName_passes() {
        List<String> names = List.of(
                "my-service",
                "my_service",
                "my.service",
                "MyService01",
                "a".repeat(PinpointConstants.SERVICE_NAME_MIN_LEN),
                "a".repeat(PinpointConstants.SERVICE_NAME_MAX_LEN)
        );

        for (String name : names) {
            assertThat(validate(name)).as(name).isEmpty();
        }
    }

    @Test
    void invalidCharacter_fails() {
        List<String> names = List.of(
                "my service",
                " my-service",
                "my-service ",
                "my/service",
                "my:service",
                "서비스"
        );

        for (String name : names) {
            assertThat(validate(name)).as(name).isNotEmpty();
        }
    }

    @Test
    void tooShortName_fails() {
        String name = "a".repeat(PinpointConstants.SERVICE_NAME_MIN_LEN - 1);

        assertThat(validate(name)).isNotEmpty();
    }

    @Test
    void tooLongName_fails() {
        String name = "a".repeat(PinpointConstants.SERVICE_NAME_MAX_LEN + 1);

        assertThat(validate(name)).isNotEmpty();
    }

    @Test
    void blankName_fails() {
        for (String name : List.of("", " ", "   ")) {
            assertThat(validate(name)).as("'" + name + "'").isNotEmpty();
        }
    }

    @Test
    void nullName_fails() {
        assertThat(validate(null)).isNotEmpty();
    }

    private Set<ConstraintViolation<ServiceNameRequest>> validate(String serviceName) {
        return validator.validate(new ServiceNameRequest(serviceName));
    }
}
