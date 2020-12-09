package com.navercorp.pinpoint.collector.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.channelz.ChannelzUtils;
import io.grpc.InternalChannelz;
import io.grpc.InternalInstrumented;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping("/channelz")
public class ChannelzController {

    private final ChannelzRegistry channelzRegistry;
    private final InternalChannelz channelz = InternalChannelz.instance();
    private final ObjectMapper mapper;

    @Autowired
    public ChannelzController(ChannelzRegistry channelzRegistry, ObjectMapper objectMapper) {
        this.channelzRegistry = Assert.requireNonNull(channelzRegistry, "channelzRegistry");
        this.mapper = Assert.requireNonNull(objectMapper, "objectMapper");
    }

    @RequestMapping("/getSocket")
    @ResponseBody
    public String getSocket(long logId) throws JsonProcessingException {
        InternalChannelz.SocketStats stats = getSocket0(logId);

        return mapper.writeValueAsString(stats);
    }

    @RequestMapping("/html/getSocket")
    @ResponseBody
    public String getSocketToHtml(long logId) {
        InternalChannelz.SocketStats stats = getSocket0(logId);

        return new HTMLBuilder().build(stats);
    }

    private InternalChannelz.SocketStats getSocket0(long logId) {
        InternalInstrumented<InternalChannelz.SocketStats> socket = channelz.getSocket(logId);
        if (socket == null) {
            return null;
        }
        return ChannelzUtils.getResult("Socket", socket);
    }

    @RequestMapping("/findSocket")
    @ResponseBody
    public String findSocket(String remoteAddress, int localPort) throws JsonProcessingException {
        String target = remoteAddress + ":" + localPort;

        InternalChannelz.SocketStats stats = findSocket(target);
        if (stats == null) {
            return notFound("remoteAddress:" + remoteAddress +" localPort:" + localPort);
        }

        return mapper.writeValueAsString(stats);
    }

    @RequestMapping("/html/findSocket")
    @ResponseBody
    public String findSocketStatToHtml(String remoteAddress, int localPort) {
        String target = remoteAddress + ":" + localPort;

        InternalChannelz.SocketStats stats = findSocket(target);
        if (stats == null) {
            return notFound("remoteAddress:" + remoteAddress +" localPort:" + localPort);
        }

        return new HTMLBuilder().build(stats);
    }

    private InternalChannelz.SocketStats findSocket(String targetAddress) {
        Long logId = channelzRegistry.getSocketLogId(targetAddress);
        InternalInstrumented<InternalChannelz.SocketStats> socket = channelz.getSocket(logId);
        if (socket == null) {
            return null;
        }
        return ChannelzUtils.getResult("Socket", socket);
    }

    @RequestMapping("/html/getServer")
    @ResponseBody
    public String getServerStatToHtml(String serverName) {
        List<InternalChannelz.ServerStats> stats = getServer(serverName);
        if (stats == null) {
            return notFound("serverName=" + serverName);
        }
        StringBuilder buffer = new StringBuilder();
        for (InternalChannelz.ServerStats stat : stats) {
            String html = new HTMLBuilder().build(stat);
            buffer.append(html);

            buffer.append("<br>");
        }
        return buffer.toString();
    }

    @RequestMapping("/html/getSpanReceiver")
    @ResponseBody
    public String getSpanReceiverl() {
        return getServerStatToHtml("grpcSpanReceiver");
    }


    private List<InternalChannelz.ServerStats> getServer(String serverName) {
        Long logId = channelzRegistry.getServerLogId(serverName);

        InternalChannelz.ServerList serverList = channelz.getServers(logId, 10000);

        List<InternalChannelz.ServerStats> serverStats = ChannelzUtils.getResults("ServerStats", serverList.servers);
        return serverStats;
    }



    private String notFound(String target) {
        return target + " not Found";
    }


}
