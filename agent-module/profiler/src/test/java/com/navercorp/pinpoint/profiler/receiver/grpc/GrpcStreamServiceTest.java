package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GrpcStreamServiceTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    ActiveTraceRepository repo;

    @Test
    void register() {

        try (GrpcStreamService service = new GrpcStreamService("test", 5000, repo)) {
            ActiveThreadCountStreamSocket socket = mock(ActiveThreadCountStreamSocket.class);

            Assertions.assertFalse(service.isStarted());
            service.register(socket);

            Assertions.assertEquals(1, service.getStreamSocketList().length);
            Assertions.assertTrue(service.isStarted());

            service.unregister(socket);
            Assertions.assertFalse(service.isStarted());
        }
    }


    @Test
    void register_duplicate() {
        try (GrpcStreamService service = new GrpcStreamService("test", 5000, repo)) {
            ActiveThreadCountStreamSocket socket = mock(ActiveThreadCountStreamSocket.class);

            Assertions.assertTrue(service.register(socket));
            Assertions.assertFalse(service.register(socket));

            Assertions.assertTrue(service.isStarted());

            Assertions.assertFalse(service.unregister(socket));
            Assertions.assertTrue(service.unregister(socket));

            Assertions.assertFalse(service.isStarted());
        }
    }

}