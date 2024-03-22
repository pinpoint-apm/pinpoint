package com.navercorp.pinpoint.collector.controller;

import com.navercorp.pinpoint.collector.service.ChannelzService;
import com.navercorp.pinpoint.collector.service.ChannelzService.ServerStatsWithId;
import com.navercorp.pinpoint.collector.service.ChannelzService.SocketStatsWithId;
import com.navercorp.pinpoint.collector.service.ChannelzSocketLookup;
import com.navercorp.pinpoint.collector.service.ChannelzSocketLookup.SocketEntry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/channelz")
public class ChannelzController {

    private final ChannelzService channelzService;
    private final ChannelzSocketLookup socketLookup;

    public ChannelzController(ChannelzService channelzService, ChannelzSocketLookup socketLookup) {
        this.channelzService = Objects.requireNonNull(channelzService, "channelzService");
        this.socketLookup = Objects.requireNonNull(socketLookup, "socketLookup");
    }

    @GetMapping(value = "/sockets/{logId}")
    public SocketStatsWithId findSocketStatsByLogId(@PathVariable long logId) {
        return this.channelzService.getSocketStats(logId);
    }

    @GetMapping(value = "/sockets")
    public List<SocketStatsWithId> findSocketStats(
            @RequestParam(required = false) String remoteAddress,
            @RequestParam(required = false) Integer localPort
    ) {
        List<Long> ids = this.socketLookup.find(remoteAddress, localPort).stream()
                .map(SocketEntry::getSocketId)
                .toList();
        return this.channelzService.getSocketStatsList(ids);
    }

    @GetMapping(value = "/servers")
    public List<ServerStatsWithId> getAllServerStats() {
        return this.channelzService.getAllServerStats();
    }

    @GetMapping(value = "/servers", produces = MediaType.TEXT_HTML_VALUE)
    public String getAllServerStatsInHtml() {
        return buildHtml(this.getAllServerStats());
    }

    @GetMapping(value = "/servers/{name}")
    public ServerStatsWithId getServerStat(@PathVariable("name") String name) {
        return this.channelzService.getServerStats(name);
    }

    @GetMapping(value = "/servers/{name}", produces = MediaType.TEXT_HTML_VALUE)
    public String getServerStatInHtml(@PathVariable("name") String name) {
        return buildHtml(this.getServerStat(name));
    }


    @GetMapping(value = "/sockets/{logId}", produces = MediaType.TEXT_HTML_VALUE)
    public String findSocketStatsByLogIdInHtml(@PathVariable long logId) {
        return buildHtml(this.findSocketStatsByLogId(logId));
    }

    @GetMapping(value = "/sockets", produces = MediaType.TEXT_HTML_VALUE)
    public String findSocketStatInHtml(
            @RequestParam(required = false) String remoteAddress,
            @RequestParam(required = false) Integer localPort
    ) throws Exception {
        return buildHtml(this.findSocketStats(remoteAddress, localPort));
    }

    private static <T> String buildHtml(List<T> stats) {
        if (stats == null || stats.isEmpty()) {
            return "Empty";
        }

        StringBuilder buffer = new StringBuilder();
        for (T stat : stats) {
            buffer.append(buildHtml(stat));
            buffer.append("<br>");
        }
        return buffer.toString();
    }

    private static <T> String buildHtml(T stats) {
        if (stats == null) {
            return "Null";
        }
        return new HTMLBuilder().build(stats);
    }

}
