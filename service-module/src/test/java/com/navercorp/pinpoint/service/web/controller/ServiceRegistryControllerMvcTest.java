package com.navercorp.pinpoint.service.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.service.component.ReservedServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.service.web.controller.vo.ServiceNameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryControllerMvcTest {

    @Mock
    private ServiceRegistryService serviceRegistryService;

    @Mock
    private ReservedServiceRegistry reservedServiceRegistry;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ServiceRegistryController controller = new ServiceRegistryController(serviceRegistryService, reservedServiceRegistry);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void insertService_200() throws Exception {
        ServiceNameRequest serviceNameRequest = new ServiceNameRequest("my-service");

        ServiceEntity entity = new ServiceEntity();
        entity.setUid(100);
        entity.setName("my-service");
        when(serviceRegistryService.insertService("my-service")).thenReturn(entity);

        MvcResult result = mockMvc.perform(post("/api/v2/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceNameRequest)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("SUCCESS");
    }

    @Test
    void getServiceNames_200() throws Exception {
        when(serviceRegistryService.getServiceNames()).thenReturn(Arrays.asList("svc-a", "svc-b"));

        MvcResult result = mockMvc.perform(get("/api/v2/services"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("[\"svc-a\",\"svc-b\"]");
    }

    @Test
    void getService_found_200() throws Exception {
        ServiceEntity entity = new ServiceEntity();
        entity.setUid(100);
        entity.setName("my-svc");
        when(serviceRegistryService.getService("my-svc")).thenReturn(entity);

        MvcResult result = mockMvc.perform(get("/api/v2/services/service").param("serviceName", "my-svc"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"uid\":100");
        assertThat(body).contains("\"name\":\"my-svc\"");
    }

    @Test
    void getService_notFound_204() throws Exception {
        when(serviceRegistryService.getService("missing")).thenReturn(null);

        mockMvc.perform(get("/api/v2/services/service").param("serviceName", "missing"))
                .andExpect(status().isNoContent());
    }

    @Test
    void insertService_reservedName_throws400() throws Exception {
        when(reservedServiceRegistry.contains("DEFAULT")).thenReturn(true);

        ServiceNameRequest request = new ServiceNameRequest("DEFAULT");

        mockMvc.perform(post("/api/v2/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteService_200() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/v2/services/service").param("serviceName", "my-service"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("SUCCESS");
        verify(serviceRegistryService).deleteService("my-service");
    }

    @Test
    void deleteService_reservedName_400() throws Exception {
        when(reservedServiceRegistry.contains("DEFAULT")).thenReturn(true);

        mockMvc.perform(delete("/api/v2/services/service").param("serviceName", "DEFAULT"))
                .andExpect(status().isBadRequest());
    }

}
