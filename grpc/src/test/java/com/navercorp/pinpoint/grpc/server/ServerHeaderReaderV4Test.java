package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServerHeaderReaderV4Test {

    private static final String AGENT_ID = "agentId";
    private static final String AGENT_NAME = "agentName";
    private static final String APPLICATION_NAME = "applicationName";
    private static final String SERVICE_NAME = "serviceName";

    private final HeaderReader<Header> reader = new ServerHeaderReaderV4("test", headers -> Collections.emptyMap());

    @Test
    void extract() {
        Header header = reader.extract(newMetadata(SERVICE_NAME));

        assertThat(header.getServiceName()).isEqualTo(SERVICE_NAME);
    }

    @Test
    void extract_fail_no_serviceName() {
        assertThatThrownBy(() -> reader.extract(newMetadata(null)))
                .isInstanceOf(InvalidGrpcHeaderException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void extract_empty_serviceName() {
        Header header = reader.extract(newMetadata(""));

        assertThat(header.getServiceName()).isEmpty();
    }

    @Test
    void extract_serviceName_minLength() {
        assertThat(reader.extract(newMetadata("abc")).getServiceName()).isEqualTo("abc");

        assertThatThrownBy(() -> reader.extract(newMetadata("ab")))
                .isInstanceOf(InvalidGrpcHeaderException.class)
                .hasMessageContaining("FAIL_LENGTH");
    }

    @Test
    void extract_serviceName_maxLength() {
        StringBuilder max = new StringBuilder();
        for (int i = 0; i < PinpointConstants.SERVICE_NAME_MAX_LEN; i++) {
            max.append('a');
        }
        assertThat(reader.extract(newMetadata(max.toString())).getServiceName()).hasSize(PinpointConstants.SERVICE_NAME_MAX_LEN);

        assertThatThrownBy(() -> reader.extract(newMetadata(max.append('a').toString())))
                .isInstanceOf(InvalidGrpcHeaderException.class)
                .hasMessageContaining("FAIL_LENGTH");
    }

    @Test
    void extract_fail_serviceName_pattern() {
        assertThatThrownBy(() -> reader.extract(newMetadata("service name")))
                .isInstanceOf(InvalidGrpcHeaderException.class)
                .hasMessageContaining("FAIL_PATTERN");
    }

    private Metadata newMetadata(String serviceName) {
        Metadata metadata = new Metadata();
        metadata.put(Header.AGENT_ID_KEY, AGENT_ID);
        metadata.put(Header.AGENT_NAME_KEY, AGENT_NAME);
        metadata.put(Header.APPLICATION_NAME_KEY, APPLICATION_NAME);
        if (serviceName != null) {
            metadata.put(Header.SERVICE_NAME_KEY, serviceName);
        }
        metadata.put(Header.AGENT_START_TIME_KEY, Long.toString(System.currentTimeMillis()));
        return metadata;
    }
}
