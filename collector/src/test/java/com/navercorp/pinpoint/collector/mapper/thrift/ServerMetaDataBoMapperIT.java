package com.nhn.pinpoint.collector.mapper.thrift;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.bo.ServerMetaDataBo;
import com.nhn.pinpoint.common.bo.ServiceInfoBo;
import com.nhn.pinpoint.thrift.dto.TServerMetaData;
import com.nhn.pinpoint.thrift.dto.TServiceInfo;

/**
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
@Ignore
public class ServerMetaDataBoMapperIT {

    @Autowired
    private ServerMetaDataBoMapper mapper;
    
    @Test
    public void testValidMap() {
        // Given
        final TServerMetaData tServerMetaData = new TServerMetaData();
        tServerMetaData.setServerInfo("serverInfo");
        tServerMetaData.setVmArgs(Arrays.asList("arg1", "arg2"));
        
        final TServiceInfo tServiceInfo = new TServiceInfo();
        tServiceInfo.setServiceName("serviceName");
        tServiceInfo.setServiceLibs(Arrays.asList("lib1", "lib2"));
        List<TServiceInfo> tServiceInfos = Arrays.asList(tServiceInfo);
        tServerMetaData.setServiceInfos(tServiceInfos);
        // When
        ServerMetaDataBo serverMetaData = mapper.map(tServerMetaData);
        List<ServiceInfoBo> serviceInfos = serverMetaData.getServiceInfos();
        // Then
        assertEquals(tServerMetaData.getServerInfo(), serverMetaData.getServerInfo());
        assertEquals(tServerMetaData.getVmArgs(), serverMetaData.getVmArgs());
        assertEquals(tServiceInfos.size(), serviceInfos.size());
        for (int i = 0; i < tServiceInfos.size(); ++i) {
            assertEquals(tServiceInfos.get(i).getServiceName(), serviceInfos.get(i).getServiceName());
            assertEquals(tServiceInfos.get(i).getServiceLibs(), serviceInfos.get(i).getServiceLibs());
        }
    }
    
    @Test
    public void mapShouldNotThrowExceptionForNullValues() {
        // Given
        final TServerMetaData tServerMetaData = new TServerMetaData();
        tServerMetaData.setServerInfo(null);
        tServerMetaData.setVmArgs(null);
        tServerMetaData.setServiceInfos(null);
        // When
        ServerMetaDataBo serverMetaData = mapper.map(tServerMetaData);
        // Then
        assertEquals("", serverMetaData.getServerInfo());
        assertEquals(Collections.emptyList(), serverMetaData.getVmArgs());
        assertEquals(Collections.emptyList(), serverMetaData.getServiceInfos());
    }

}
