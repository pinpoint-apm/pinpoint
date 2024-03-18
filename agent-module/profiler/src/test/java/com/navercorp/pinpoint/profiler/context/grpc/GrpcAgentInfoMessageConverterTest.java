package com.navercorp.pinpoint.profiler.context.grpc;


import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PJvmInfo;
import com.navercorp.pinpoint.grpc.trace.PServerMetaData;
import com.navercorp.pinpoint.grpc.trace.PServiceInfo;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaData;
import com.navercorp.pinpoint.profiler.context.DefaultServiceInfo;
import com.navercorp.pinpoint.profiler.context.TestAgentInformation;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentInfoMapperImpl;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.JvmGcTypeMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.JvmGcTypeMapperImpl;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomIntegerStringMap;
import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomStringList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class GrpcAgentInfoMessageConverterTest {

    private final JvmGcTypeMapper jvmGcTypeMapper = new JvmGcTypeMapperImpl();
    private final AgentInfoMapper mapper = new AgentInfoMapperImpl(jvmGcTypeMapper);
    private final GrpcAgentInfoMessageConverter converter = new GrpcAgentInfoMessageConverter(mapper);

    @Test
    void testMapAgentInfo() {
        AgentInfo agentInfo = new AgentInfo(
                new TestAgentInformation(),
                new DefaultServerMetaData(
                        "serverInfo",
                        randomStringList(),
                        randomIntegerStringMap(),
                        randomServiceInfoList()
                ),
                new JvmInformation("1.0", JvmGcType.G1)
        );
        PAgentInfo pAgentInfo = (PAgentInfo) converter.toMessage(agentInfo);


        AgentInformation agentInformation = agentInfo.getAgentInformation();

        assertEquals(agentInformation.getHostIp(), pAgentInfo.getIp());
        assertEquals(agentInformation.getMachineName(), pAgentInfo.getHostname());
        assertEquals("", pAgentInfo.getPorts());
        assertEquals(agentInformation.isContainer(), pAgentInfo.getContainer());
        assertEquals(agentInformation.getPid(), pAgentInfo.getPid());
        assertEquals(agentInformation.getServerType().getCode(), pAgentInfo.getServiceType());
        assertEquals(agentInformation.getJvmVersion(), pAgentInfo.getVmVersion());
        assertEquals(Version.VERSION, pAgentInfo.getAgentVersion());


        ServerMetaData serverMetaData = agentInfo.getServerMetaData();
        PServerMetaData pServerMetaData = pAgentInfo.getServerMetaData();

        assertEquals(serverMetaData.getServerInfo(), pServerMetaData.getServerInfo());
        assertEquals(serverMetaData.getVmArgs(), pServerMetaData.getVmArgList());

        List<ServiceInfo> serviceInfoList = serverMetaData.getServiceInfos();
        List<PServiceInfo> pServiceInfoList = pServerMetaData.getServiceInfoList();

        assertEquals(serviceInfoList.size(), pServiceInfoList.size());
        for (int i = 0; i < serviceInfoList.size(); i++) {
            assertEquals(serviceInfoList.get(i).getServiceName(), pServiceInfoList.get(i).getServiceName());
            assertEquals(serviceInfoList.get(i).getServiceLibs(), pServiceInfoList.get(i).getServiceLibList());
        }

        JvmInformation jvmInformation = agentInfo.getJvmInfo();
        PJvmInfo pJvmInfo = pAgentInfo.getJvmInfo();

        assertEquals(jvmInformation.getJvmVersion(), pJvmInfo.getVmVersion());
        assertEquals("JVM_GC_TYPE_" + jvmInformation.getJvmGcType().toString(), pJvmInfo.getGcType().toString());
    }


    List<ServiceInfo> randomServiceInfoList() {
        List<ServiceInfo> infos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            infos.add(randomServiceInfo());
        }
        return infos;
    }

    ServiceInfo randomServiceInfo(){
        return new DefaultServiceInfo(
                randomString(),
                randomStringList()
        );
    }
}