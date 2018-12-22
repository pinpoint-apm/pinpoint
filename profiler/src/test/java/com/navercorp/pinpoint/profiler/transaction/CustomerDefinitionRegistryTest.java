package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.transaction.RequestMappingInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.navercorp.pinpoint.profiler.transaction.config.TransactionTypeConfiguration.CUSTOMER_TXTYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomerDefinitionRegistryTest {

    @Mock
    DefaultProfilerConfig profilerConfig;

    @Test
    public void should_get_the_request_mapping_when_the_uri_is_matched() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/post/{name}/hi");

        CustomerDefinitionRegistry customerDefinitionRegistry = new CustomerDefinitionRegistry(profilerConfig);

        IRequestMappingInfo expectedResult = new RequestMappingInfo("/post/{name}/hi", "POST", "GET");

        assertThat(customerDefinitionRegistry.match("/post/hello/hi", "POST"),
                is(expectedResult));

    }

    @Test
    public void should_get_null__when_the_method_or_uri_is_not_matched(){
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/post/{name}/hi");

        CustomerDefinitionRegistry customerDefinitionRegistry = new CustomerDefinitionRegistry(profilerConfig);

        assertNull(customerDefinitionRegistry.match("/post/hello/hi", "HEAD"));
        assertNull(customerDefinitionRegistry.match("/post/hello/himm", "GET"));
    }

    @Test
    public void should_get_one_of_request_mapping_when_there_are_multi_matched_rules() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/post/{name}/hi;GET,POST_/post/{name}/{age};");

        CustomerDefinitionRegistry customerDefinitionRegistry = new CustomerDefinitionRegistry(profilerConfig);

        assertNotNull(customerDefinitionRegistry.match("/post/hello/hi", "GET"));
    }

    @Test
    public void should_be_null_when_there_no_rules() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("");

        CustomerDefinitionRegistry customerDefinitionRegistry = new CustomerDefinitionRegistry(profilerConfig);

        assertNull(customerDefinitionRegistry.match("/post/hello/hi", "GET"));
    }
}