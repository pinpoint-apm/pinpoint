package com.navercorp.pinpoint.web.component;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultApplicationFactoryTest {

    @Mock
    private ServiceTypeRegistryService mockRegistry;

    @Mock
    private ServiceModelResolver mockServiceModelResolver;

    private DefaultApplicationFactory defaultApplicationFactory;

    @BeforeEach
    void setUp() {
        lenient().when(mockRegistry.findServiceType(ServiceType.TEST.getCode())).thenReturn(ServiceType.TEST);
        lenient().when(mockServiceModelResolver.getService(Service.DEFAULT.getServiceUid())).thenReturn(Service.DEFAULT);

        defaultApplicationFactory = new DefaultApplicationFactory(mockRegistry, mockServiceModelResolver);
    }

    @Test
    void testCreateApplicationWithService() {
        // Given
        Service service = Service.DEFAULT;
        String applicationName = "TestApp";
        ServiceType serviceType = ServiceType.TEST;

        // When
        Application application = defaultApplicationFactory.createApplication(service, applicationName, serviceType);

        // Then
        assertNotNull(application);
        assertEquals(applicationName, application.getApplicationName());
        assertEquals(serviceType, application.getServiceType());
        assertEquals(service, application.getService());

        verifyNoInteractions(mockRegistry);
    }

    @Test
    void testCreateApplicationWithServiceType() {
        // Given
        Service service = Service.DEFAULT;
        String applicationName = "TestApp";
        int serviceType = ServiceType.TEST.getCode();

        // When
        Application application = defaultApplicationFactory.createApplication(service.getServiceUid(), applicationName, serviceType);

        // Then
        assertNotNull(application);
        assertEquals(applicationName, application.getApplicationName());
        assertEquals(serviceType, application.getServiceType().getCode());
        assertEquals(service, application.getService());
    }

    @Test
    void testCreateApplicationWithService_NullInputs() {
        Service service = Service.DEFAULT;
        ServiceType serviceType = ServiceType.TEST;

        // Null service
        assertThrows(NullPointerException.class,
                () -> defaultApplicationFactory.createApplication(null, "TestApp", serviceType));

        // Null application name
        assertThrows(NullPointerException.class,
                () -> defaultApplicationFactory.createApplication(service, null, serviceType));

        // Null service type
        assertThrows(NullPointerException.class,
                () -> defaultApplicationFactory.createApplication(service, "TestApp", null));
    }
}