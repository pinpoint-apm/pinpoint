package com.navercorp.pinpoint.profiler.transaction.config;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.transaction.RequestMappingInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.profiler.transaction.config.TransactionTypeConfiguration.CUSTOMER_TXTYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionTypeConfigurationTest {

    @Mock
    DefaultProfilerConfig profilerConfig;


    @Test
    public void should_parse_the_txtype_correctly() {
       when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("GET,POST_/hi/{name}");

       TransactionTypeConfiguration transactionTypeConfiguration = new TransactionTypeConfiguration(profilerConfig);
       List<IRequestMappingInfo> requestMappingInfos =  transactionTypeConfiguration.rules();

       assertThat(requestMappingInfos, is(createRequestMappingInfos(new RequestMappingInfo("/hi/{name}", "GET", "POST"))));
    }

    @Test
    public void should_ignore_the_invalid_txtype() {
        when(profilerConfig.readString(CUSTOMER_TXTYPE, "")).thenReturn("/hi/{name}; GET,POST_/hi/{name}");

        TransactionTypeConfiguration transactionTypeConfiguration = new TransactionTypeConfiguration(profilerConfig);
        List<IRequestMappingInfo> requestMappingInfos =  transactionTypeConfiguration.rules();

        assertThat(requestMappingInfos, is(createRequestMappingInfos(new RequestMappingInfo("/hi/{name}", "GET", "POST"))));
    }

    private List<IRequestMappingInfo> createRequestMappingInfos(IRequestMappingInfo ... requestMappingInfos){
        return Arrays.asList(requestMappingInfos);
    }
}