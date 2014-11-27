package com.nhn.pinpoint.profiler.modifier.tomcat;

import static org.junit.Assert.*;

import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.util.ServerInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nhn.pinpoint.bootstrap.context.ServerMetaData;
import com.nhn.pinpoint.test.junit4.BasePinpointTest;

/**
 * @author hyungil.jeong
 */
public class StandardServiceModifierTest extends BasePinpointTest {

    private StandardService service;
    
    @Mock
    private StandardEngine engine;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.service = new StandardService();
        this.service.setContainer(this.engine);
    }

    @Test
    public void startShouldCollectServerInfo() throws Exception {
        // Given
        String expectedServerInfo = ServerInfo.getServerInfo();
        // When
        service.start();
        service.stop();
        // Then
        ServerMetaData serverMetaData = getServerMetaData();
        assertEquals(serverMetaData.getServerInfo(), expectedServerInfo);
    }

}
