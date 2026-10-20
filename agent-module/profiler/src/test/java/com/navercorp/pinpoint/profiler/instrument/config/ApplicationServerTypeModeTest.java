package com.navercorp.pinpoint.profiler.instrument.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationServerTypeModeTest {

    @Test
    void parse_defaultsToV4() {
        assertThat(ApplicationServerTypeMode.parse(null)).isEqualTo(ApplicationServerTypeMode.V4);
        assertThat(ApplicationServerTypeMode.parse("")).isEqualTo(ApplicationServerTypeMode.V4);
        assertThat(ApplicationServerTypeMode.parse("  ")).isEqualTo(ApplicationServerTypeMode.V4);
    }

    @Test
    void parse_isCaseInsensitive() {
        assertThat(ApplicationServerTypeMode.parse("v4")).isEqualTo(ApplicationServerTypeMode.V4);
        assertThat(ApplicationServerTypeMode.parse("V1")).isEqualTo(ApplicationServerTypeMode.V1);
        assertThat(ApplicationServerTypeMode.parse(" v1 ")).isEqualTo(ApplicationServerTypeMode.V1);
    }

    @Test
    void parse_rejectsUnknown() {
        assertThatThrownBy(() -> ApplicationServerTypeMode.parse("simple"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
