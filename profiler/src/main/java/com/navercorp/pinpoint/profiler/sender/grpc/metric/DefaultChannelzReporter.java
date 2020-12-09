package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.channelz.ChannelzUtils;
import io.grpc.InternalChannelz;
import io.grpc.InternalInstrumented;
import io.grpc.InternalWithLogId;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultChannelzReporter implements ChannelzReporter {
    private final Logger logger;

    private final InternalChannelz channelz = InternalChannelz.instance();

    private final ChannelStatsReporter reporter = new ChannelStatsReporter();
    private final long timeout = 3000;

    public DefaultChannelzReporter(Logger logger) {
        this.logger = Assert.requireNonNull(logger, "logger");
    }

    @Override
    public void reportRootChannel(long id) {
        final InternalInstrumented<InternalChannelz.ChannelStats> iRootChannel = channelz.getRootChannel(id);
        if (iRootChannel == null) {
            logger.info("RootChannel not found:{}", id);
            return;
        }
        final String rootChannelName = "RootChannel-" + id;
        final InternalChannelz.ChannelStats channelStats = ChannelzUtils.getResult(rootChannelName, iRootChannel);
        if (channelStats == null) {
            logger.info("RootChannel channelStats.get() fail:{}", id);
            return;
        }

        this.reporter.reportChannelStats(rootChannelName, channelStats);

        reportSubChannel(id, channelStats.subchannels);
    }

    private void reportSubChannel(long rootChannelId, List<InternalWithLogId> subChannels) {
        for (InternalWithLogId subChannelLogId : subChannels) {
            final long subChannelId = subChannelLogId.getLogId().getId();
            final InternalInstrumented<InternalChannelz.ChannelStats> iSubChannelState = channelz.getSubchannel(subChannelId);
            if (iSubChannelState == null) {
                continue;
            }

            final String rootChannelName = rootChannelId + "-SubChannel-" + subChannelId;
            final InternalChannelz.ChannelStats subChannelStats = ChannelzUtils.getResult(rootChannelName, iSubChannelState);
            if (subChannelStats == null) {
                continue;
            }

            this.reporter.reportChannelStats(rootChannelName, subChannelStats);

            for (InternalWithLogId socketId : subChannelStats.sockets) {
                reportSocketStats(subChannelId, socketId);
            }
        }
    }

    private void reportSocketStats(long channelId, InternalWithLogId socketLogId) {
        final long socketId = socketLogId.getLogId().getId();
        InternalInstrumented<InternalChannelz.SocketStats> iSocket = channelz.getSocket(socketId);
        if (iSocket == null) {
            return;
        }
        final String socketName = channelId + "-Socket-" + socketId;
        final InternalChannelz.SocketStats socketStats = ChannelzUtils.getResult(socketName, iSocket);
        if (socketStats == null) {
            return;
        }

        MoreObjects.ToStringHelper title = MoreObjects.toStringHelper("");
        title.add("local", socketStats.local);
        title.add("remote", socketStats.remote);
        title.add("security", socketStats.security);
        logger.info("{} {}", socketName, title.toString());

        final InternalChannelz.TransportStats transportStats = socketStats.data;
        if (transportStats != null) {
            MoreObjects.ToStringHelper socketStrHelper = MoreObjects.toStringHelper("");
            socketStrHelper.add("streamsStarted", transportStats.streamsStarted);
            socketStrHelper.add("lastLocalStreamCreatedTime", toMillis(transportStats.lastLocalStreamCreatedTimeNanos));
            socketStrHelper.add("lastRemoteStreamCreatedTime", toMillis(transportStats.lastRemoteStreamCreatedTimeNanos));
            logger.info("{} {}", socketName, socketStrHelper.toString());

            MoreObjects.ToStringHelper socketStatStrHelper = MoreObjects.toStringHelper("");
            socketStatStrHelper.add("streamsSucceeded", transportStats.streamsSucceeded);
            socketStatStrHelper.add("streamsFailed", transportStats.streamsFailed);
            socketStatStrHelper.add("messagesSent", transportStats.messagesSent);
            socketStatStrHelper.add("messagesReceived", transportStats.messagesReceived);
            logger.info("{} {}", socketName, socketStatStrHelper.toString());

            MoreObjects.ToStringHelper socketStat2StrHelper = MoreObjects.toStringHelper("");
            socketStat2StrHelper.add("keepAlivesSent", transportStats.keepAlivesSent);
            socketStat2StrHelper.add("lastMessageSentTime", toMillis(transportStats.lastMessageSentTimeNanos));
            socketStat2StrHelper.add("lastMessageReceivedTime", toMillis(transportStats.lastMessageReceivedTimeNanos));
            socketStat2StrHelper.add("localFlowControlWindow", transportStats.localFlowControlWindow);
            socketStat2StrHelper.add("remoteFlowControlWindow", transportStats.remoteFlowControlWindow);
            logger.info("{} {}", socketName, socketStat2StrHelper.toString());
        }

//        InternalChannelz.SocketOptions socketOptions = socketStats.socketOptions;
//        logger.info("{} socketOptions soTimeoutMillis:{} lingerSeconds:{}", socketName, socketOptions.soTimeoutMillis, socketOptions.lingerSeconds);
//        logger.info("{} socketOptions others:{}", socketName, socketOptions.others);

//        InternalChannelz.TcpInfo tcpInfo = socketOptions.tcpInfo;
//        if (tcpInfo != null) {
//            logger.info("{} tcpInfo retransmits:{}", socketName, tcpInfo.retransmits);
//            logger.info("{} tcpInfo backoff:{}", socketName, tcpInfo.backoff);
//        }
    }

    public class ChannelStatsReporter {

        public void reportChannelStats(String name, InternalChannelz.ChannelStats channelStats) {
            if (!logger.isInfoEnabled()) {
                // Disable channelz log
                return;
            }
            MoreObjects.ToStringHelper title = MoreObjects.toStringHelper("");
            title.add("target", channelStats.target);
            title.add("stat", channelStats.state);
            logger.info("{} {}", name, title.toString());

            MoreObjects.ToStringHelper counter = MoreObjects.toStringHelper("");
            counter.add("callsFailed", channelStats.callsFailed);
            counter.add("callsStarted", channelStats.callsStarted);
            counter.add("callsSucceeded", channelStats.callsSucceeded);
            counter.add("lastCallStarted", toMillis(channelStats.lastCallStartedNanos));
            logger.info("{} {}", name, counter.toString());

            if (CollectionUtils.hasLength(channelStats.sockets)) {
                logger.info("{} sockets:{}", name, toLogIds(channelStats.sockets));
            }
            if (CollectionUtils.hasLength(channelStats.subchannels)) {
                logger.info("{} subchannels:{}", name, toLogIds(channelStats.subchannels));
            }
            InternalChannelz.ChannelTrace channelTrace = channelStats.channelTrace;
            if (channelTrace != null) {
                MoreObjects.ToStringHelper traceStrHelper = MoreObjects.toStringHelper("");
                traceStrHelper.add("numEventsLogged", channelTrace.numEventsLogged);
                traceStrHelper.add("creationTime", toMillis(channelTrace.creationTimeNanos));
                traceStrHelper.add("events.size", channelTrace.events.size());
                logger.info("{} channelTrace {}", name, traceStrHelper.toString());

                for (InternalChannelz.ChannelTrace.Event event : channelTrace.events) {
                    logger.info("{} channelTrace.events:{}", name, toString(event));
                }
            }
        }

        private String toString(InternalChannelz.ChannelTrace.Event event) {
            MoreObjects.ToStringHelper stringHelper = MoreObjects.toStringHelper(event);
            stringHelper.add("description", event.description);
            stringHelper.add("severity", event.severity);
            stringHelper.add("timestamp", toMillis(event.timestampNanos));
            if (event.channelRef != null) {
                stringHelper.add("channelRef", event.channelRef);
            }
            if (event.subchannelRef != null) {
                stringHelper.add("subchannelRef", event.subchannelRef);
            }
            return stringHelper.toString();
        }
    }

    public String toLogIds(List<InternalWithLogId> logIdList) {
        long[] longs = toLogIdArray(logIdList);
        return Arrays.toString(longs);
    }

    public long[] toLogIdArray(List<InternalWithLogId> logIdList) {
        if (logIdList == null) {
            return new long[0];
        }
        long[] longs = new long[logIdList.size()];
        for (int i = 0; i < logIdList.size(); i++) {
            InternalWithLogId internalWithLogId = logIdList.get(i);
            longs[i] = internalWithLogId.getLogId().getId();
        }
        return longs;
    }


    public static long toMillis(long duration) {
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }

}
