package com.navercorp.pinpoint.service.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceEntityTest {

    @Test
    void getterSetter() {
        ServiceEntity entity = new ServiceEntity();
        entity.setUid(42);
        entity.setName("test-service");

        assertThat(entity.getUid()).isEqualTo(42);
        assertThat(entity.getName()).isEqualTo("test-service");
    }

    @Test
    void defaultValues() {
        ServiceEntity entity = new ServiceEntity();

        assertThat(entity.getUid()).isEqualTo(0);
        assertThat(entity.getName()).isNull();
    }
}
