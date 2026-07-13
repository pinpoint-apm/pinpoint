package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryServiceImplTest {

    @Mock
    private ServiceRegistryDao serviceRegistryDao;

    @Mock
    private IdGenerator<ServiceUid> serviceUidGenerator;

    private ServiceRegistryServiceImpl serviceRegistryService;

    @BeforeEach
    void setUp() {
        serviceRegistryService = new ServiceRegistryServiceImpl(serviceRegistryDao, serviceUidGenerator);
    }

    @Test
    void insertService_success() {
        ServiceUid serviceUid = ServiceUid.of(12345);
        when(serviceUidGenerator.generate()).thenReturn(serviceUid);

        ServiceEntity result = serviceRegistryService.insertService("my-service");

        verify(serviceRegistryDao).insertService(12345, "my-service");
        assertThat(result.getUid()).isEqualTo(12345);
        assertThat(result.getName()).isEqualTo("my-service");
    }

    @Test
    void insertService_nullName() {
        assertThatThrownBy(() -> serviceRegistryService.insertService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void insertService_duplicateName_notRetryable() {
        ServiceUid serviceUid = ServiceUid.of(12345);
        when(serviceUidGenerator.generate()).thenReturn(serviceUid);
        when(serviceRegistryDao.insertService(12345, "my-service")).thenThrow(new DuplicateKeyException("duplicate"));
        ServiceEntity existing = new ServiceEntity();
        existing.setUid(99999);
        existing.setName("my-service");
        when(serviceRegistryDao.selectService("my-service")).thenReturn(existing);

        assertThatThrownBy(() -> serviceRegistryService.insertService("my-service"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void insertService_uidCollision_retryable() {
        ServiceUid serviceUid = ServiceUid.of(12345);
        when(serviceUidGenerator.generate()).thenReturn(serviceUid);
        when(serviceRegistryDao.insertService(12345, "my-service")).thenThrow(new DuplicateKeyException("duplicate"));
        when(serviceRegistryDao.selectService("my-service")).thenReturn(null);

        assertThatThrownBy(() -> serviceRegistryService.insertService("my-service"))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void getServiceNames_returnsList() {
        List<String> names = Arrays.asList("svc-a", "svc-b");
        when(serviceRegistryDao.selectServiceNames()).thenReturn(names);

        List<String> result = serviceRegistryService.getServiceNames();

        assertThat(result).containsExactly("svc-a", "svc-b");
        verify(serviceRegistryDao).selectServiceNames();
    }

    @Test
    void getServiceNames_emptyList() {
        when(serviceRegistryDao.selectServiceNames()).thenReturn(Collections.emptyList());

        List<String> result = serviceRegistryService.getServiceNames();

        assertThat(result).isEmpty();
    }

    @Test
    void getService_found() {
        ServiceEntity expected = new ServiceEntity();
        expected.setUid(100);
        expected.setName("test-svc");
        when(serviceRegistryDao.selectService("test-svc")).thenReturn(expected);

        ServiceEntity result = serviceRegistryService.getService("test-svc");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test-svc");
    }

    @Test
    void getService_notFound() {
        when(serviceRegistryDao.selectService("no-svc")).thenReturn(null);

        ServiceEntity result = serviceRegistryService.getService("no-svc");

        assertThat(result).isNull();
    }

    @Test
    void getService_nullName() {
        assertThatThrownBy(() -> serviceRegistryService.getService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteService_success() {
        ServiceEntity existing = new ServiceEntity();
        existing.setUid(300);
        existing.setName("to-delete");
        when(serviceRegistryDao.selectService("to-delete")).thenReturn(existing);

        serviceRegistryService.deleteService("to-delete");

        verify(serviceRegistryDao).deleteService(300);
    }

    @Test
    void deleteService_notFound_silent() {
        when(serviceRegistryDao.selectService("missing")).thenReturn(null);

        serviceRegistryService.deleteService("missing");

        verify(serviceRegistryDao, never()).deleteService(anyInt());
    }

    @Test
    void deleteService_nullName() {
        assertThatThrownBy(() -> serviceRegistryService.deleteService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
