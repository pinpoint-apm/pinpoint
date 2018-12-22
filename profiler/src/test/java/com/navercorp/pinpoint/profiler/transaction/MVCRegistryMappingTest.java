package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.transaction.RequestMappingInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MVCRegistryMappingTest {

    @Test
    public void should_ignore_the_request_mapping_with_invalid_level() {
        MVCRegistryMapping mvcRegistryMapping = new MVCRegistryMapping();

        mvcRegistryMapping.register(new RequestMappingInfo("/ignore", "POST", "GET"), 0);
        mvcRegistryMapping.register(new RequestMappingInfo("/ignore", "POST"), 3);

        assertNull(mvcRegistryMapping.match("/ignore", "GET"));
    }

    @Test
    public void should_match_as_level_order() {
        MVCRegistryMapping mvcRegistryMapping = new MVCRegistryMapping();

        mvcRegistryMapping.register(new RequestMappingInfo("/ignore/{level1}", "POST", "GET"), 1);
        mvcRegistryMapping.register(new RequestMappingInfo("/ignore/{level2}", "POST", "GET"), 2);
        mvcRegistryMapping.register(new RequestMappingInfo("/ignore/{level2}/{level2}", "POST", "GET"), 2);

        IRequestMappingInfo expectedLevel1Result = new RequestMappingInfo("/ignore/{level1}", "GET", "POST");
        IRequestMappingInfo expectedLevel2Result = new RequestMappingInfo("/ignore/{level2}/{level2}", "GET", "POST");

        assertThat(mvcRegistryMapping.match("/ignore/match", "GET"), is(expectedLevel1Result));
        assertThat(mvcRegistryMapping.match("/ignore/match/ma", "GET"), is(expectedLevel2Result));
    }
}