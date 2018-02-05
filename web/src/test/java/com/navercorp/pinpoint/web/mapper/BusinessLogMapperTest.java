package com.navercorp.pinpoint.web.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.verify;

/**
 * Created by suny on 2018/2/3.
 */
public class BusinessLogMapperTest {
    @InjectMocks
    BusinessLogMapper businessLogMapper;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mapRowNull() throws Exception {
        Assert.assertNull(businessLogMapper.mapRow(new Result(),0));
    }

}