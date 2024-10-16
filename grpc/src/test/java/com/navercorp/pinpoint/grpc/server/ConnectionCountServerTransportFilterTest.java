package com.navercorp.pinpoint.grpc.server;

import io.grpc.Attributes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConnectionCountServerTransportFilterTest {

    @Test
    void transportReady() {
        ConnectionCountServerTransportFilter filter = new ConnectionCountServerTransportFilter();
        Attributes attributes = Attributes.newBuilder().build();
        filter.transportReady(attributes);

        Assertions.assertEquals(1, filter.getCurrentConnection());
    }

    @Test
    void transportReadyAndTerminated() {
        ConnectionCountServerTransportFilter filter = new ConnectionCountServerTransportFilter();
        Attributes attributes = Attributes.newBuilder().build();
        attributes = filter.transportReady(attributes);
        filter.transportTerminated(attributes);

        Assertions.assertEquals(0, filter.getCurrentConnection());
    }

    @Test
    void transportTerminated_duplicate() {
        ConnectionCountServerTransportFilter filter = new ConnectionCountServerTransportFilter();
        Attributes attributes = Attributes.newBuilder().build();
        attributes = filter.transportReady(attributes);
        filter.transportTerminated(attributes);
        filter.transportTerminated(attributes);
        filter.transportTerminated(attributes);

        Assertions.assertEquals(0, filter.getCurrentConnection());


    }
}