package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.transaction.RequestMappingInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.navercorp.pinpoint.profiler.transaction.config.TransactionTypeConfiguration.CUSTOMER_TXTYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegistryMappingTest {

    @Mock
    DefaultProfilerConfig profilerConfig;

    @Test
    public void should_custom_first() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/name/{custom}");

        IMappingRegistry registryMaping = new RegistryMapping(profilerConfig);
        registryMaping.register(new RequestMappingInfo("/name/{mvc}", "GET", "POST"), 1);

        IRequestMappingInfo requestMappingInfo = registryMaping.match("/name/hi", "GET");

        IRequestMappingInfo expectedResult = new RequestMappingInfo("/name/{custom}", "POST", "GET");
        assertThat(requestMappingInfo, is(expectedResult));
    }

    @Test
    public void should_match_with_mvc_registry_when_no_match_in_custom_registry() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/name");

        IMappingRegistry registryMaping = new RegistryMapping(profilerConfig);
        registryMaping.register(new RequestMappingInfo("/name/{mvc}", "GET", "POST"), 1);

        IRequestMappingInfo requestMappingInfo = registryMaping.match("/name/hi", "POST");

        IRequestMappingInfo expectedResult = new RequestMappingInfo("/name/{mvc}", "POST", "GET");
        assertThat(requestMappingInfo, is(expectedResult));
    }

    @Test
    public void should_match_with_mvc_registry_when_cutom_registry_is_empty() {
        IMappingRegistry registryMaping = new RegistryMapping(profilerConfig);
        registryMaping.register(new RequestMappingInfo("/name/{mvc}", "GET", "POST"), 1);

        IRequestMappingInfo requestMappingInfo = registryMaping.match("/name/hi", "POST");

        IRequestMappingInfo expectedResult = new RequestMappingInfo("/name/{mvc}", "POST", "GET");
        assertThat(requestMappingInfo, is(expectedResult));
    }

}