package com.navercorp.pinpoint.service.web.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.service.component.ReservedServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.service.web.controller.vo.ServiceNameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryControllerTest {

    @Mock
    private ServiceRegistryService serviceRegistryService;

    @Mock
    private ReservedServiceRegistry reservedServiceRegistry;

    private ServiceRegistryController controller;

    @BeforeEach
    void setUp() {
        controller = new ServiceRegistryController(serviceRegistryService, reservedServiceRegistry);
    }

    @Test
    void insertService_returnsSuccess() {
        ServiceNameRequest serviceNameRequest = new ServiceNameRequest("my-service");

        ServiceEntity entity = new ServiceEntity();
        entity.setUid(100);
        entity.setName("my-service");
        when(serviceRegistryService.insertService("my-service")).thenReturn(entity);

        Response response = controller.insertService(serviceNameRequest);

        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        verify(serviceRegistryService).insertService("my-service");
    }

    @Test
    void insertService_reservedName_throws400() {
        ServiceNameRequest serviceNameRequest = new ServiceNameRequest("DEFAULT");
        when(reservedServiceRegistry.contains("DEFAULT")).thenReturn(true);

        assertThatThrownBy(() -> controller.insertService(serviceNameRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
        verify(serviceRegistryService, never()).insertService("DEFAULT");
    }

    @Test
    void getServiceNames_returnsList() {
        when(serviceRegistryService.getServiceNames()).thenReturn(Arrays.asList("svc-a", "svc-b"));

        List<String> result = controller.getServiceNames();

        assertThat(result).containsExactly("svc-a", "svc-b");
    }

    @Test
    void getServiceNames_emptyList() {
        when(serviceRegistryService.getServiceNames()).thenReturn(Collections.emptyList());

        List<String> result = controller.getServiceNames();

        assertThat(result).isEmpty();
    }

    @Test
    void getService_found_returns200() {
        ServiceEntity entity = new ServiceEntity();
        entity.setUid(100);
        entity.setName("my-svc");
        when(serviceRegistryService.getService("my-svc")).thenReturn(entity);

        ResponseEntity<ServiceEntity> response = controller.getService("my-svc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("my-svc");
        assertThat(response.getBody().getUid()).isEqualTo(100);
    }

    @Test
    void getService_notFound_returns204() {
        when(serviceRegistryService.getService("missing")).thenReturn(null);

        ResponseEntity<ServiceEntity> response = controller.getService("missing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteService_returnsSuccess() {
        Response response = controller.deleteService("my-service");

        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        verify(serviceRegistryService).deleteService("my-service");
    }

    @Test
    void deleteService_reservedName_throws400() {
        when(reservedServiceRegistry.contains("DEFAULT")).thenReturn(true);

        assertThatThrownBy(() -> controller.deleteService("DEFAULT"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
        verify(serviceRegistryService, never()).deleteService("DEFAULT");
    }
}
