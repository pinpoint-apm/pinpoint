package com.navercorp.pinpoint.collector.receiver.grpc.keepalive;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * A simple client that requests a greeting from the {@link KeepAliveServer}.
 */
public class KeepAliveClient {
    private static final Logger logger = LogManager.getLogger(KeepAliveClient.class);

    private final SpanGrpc.SpanStub stub;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public KeepAliveClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.
        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        this.stub = SpanGrpc.newStub(channel);
    }

    /** Say hello to server. */
    public void greet(String name) {
        logger.info("Will try to greet {} ...", name);
        PSpanMessage request = PSpanMessage.newBuilder()
                .setSpan(PSpan.newBuilder()
                        .setStartTime(11)
                        .build())
                .build();

        try {

            StreamObserver<PSpanMessage> sender = this.stub.sendSpan(new StreamObserver<>() {
                @Override
                public void onNext(Empty empty) {
                    logger.info("onNext: {}", empty);
                }

                @Override
                public void onError(Throwable throwable) {
                    Status status = Status.fromThrowable(throwable);
                    Metadata metadata = Status.trailersFromThrowable(throwable);
                    logger.info("onError: {} {}", status, metadata);
                }

                @Override
                public void onCompleted() {
                    logger.info("sender2 ----------onCompleted");
                }
            });
//            for (int i = 0; i < 100_000_000; i++) {
//                for (int j = 0; j < 100; j++) {
//                    newstream();
//                }
//                System.out.println("sleep");
//                Thread.sleep(1000);
//            }

            sender.onNext(request);
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    System.out.println("--------------run");
//                    try {
//                        if (sender2.isReady()) {
//                            sender2.onNext(request);
//                        } else {
//                            System.out.println("--------------not ready");
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 1000, 1000);
            System.out.println("--------------sleep");
            Thread.sleep(1000);
            System.out.println("--------------complete");
//
//            sender.onCompleted();
//            sender2.onCompleted();

            Thread.sleep(1000_000);

        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        logger.info("Greeting: " + response.getMessage());
    }

    private void newstream() {
        CallStreamObserver<PSpanMessage> sender2 = (CallStreamObserver) this.stub.sendSpan(new StreamObserver<>() {
            @Override
            public void onNext(Empty empty) {
                logger.info("sender2 onNext: {}", empty);
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                Metadata metadata = Status.trailersFromThrowable(throwable);
                logger.info("sender2 onError: {} {}", status, metadata);
            }


            @Override
            public void onCompleted() {
                logger.info("sender2 ----------onCompleted");
            }
        });
    }

    /**
     * Greet server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        // Create a channel with the following keep alive configurations (demo only, you should set
        // more appropriate values based on your environment):
        // keepAliveTime: Send pings every 10 seconds if there is no activity. Set to an appropriate
        // value in reality, e.g. (5, TimeUnit.MINUTES).
        // keepAliveTimeout: Wait 1 second for ping ack before considering the connection dead. Set to a
        // larger value in reality, e.g. (10, TimeUnit.SECONDS). You should only set such a small value,
        // e.g. (1, TimeUnit.SECONDS) in certain low latency environments.
        // keepAliveWithoutCalls: Send pings even without active streams. Normally disable it.
        // Use JAVA_OPTS=-Djava.util.logging.config.file=logging.properties to see the keep alive ping
        // frames.
        // More details see: https://github.com/grpc/proposal/blob/master/A8-client-side-keepalive.md
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .keepAliveTime(5, TimeUnit.MINUTES)
                .keepAliveTime(10, TimeUnit.SECONDS) // Change to a larger value, e.g. 5min.
                .keepAliveTimeout(1, TimeUnit.SECONDS) // Change to a larger value, e.g. 10s.
                .keepAliveWithoutCalls(false)// You should normally avoid enabling this.
                .build();

        try {
            ConnectivityState state = channel.getState(false);
            channel.notifyWhenStateChanged(state, new ConnectivityStateMonitor(channel, state));
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ConnectivityState state = channel.getState(false);
                    logger.info("client ConnectivityState:{}", state);
                }
            }, 2000, 2000);
            KeepAliveClient client = new KeepAliveClient(channel);
            client.greet("Keep-alive Demo");
            Thread.sleep(30_000);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static class ConnectivityStateMonitor implements Runnable {
        private final ConnectivityState before;
        private final ManagedChannel channel;

        public ConnectivityStateMonitor(ManagedChannel channel, ConnectivityState before) {
            this.channel = Objects.requireNonNull(channel, "channel");
            this.before = Objects.requireNonNull(before, "before");
        }

        @Override
        public void run() {
            final ConnectivityState change = channel.getState(false);
            logger.info("ConnectivityState changed before:{}, change:{}", before, change);
            channel.notifyWhenStateChanged(change, new ConnectivityStateMonitor(channel, change));
        }
    }


    private static Timer timer = new Timer();

}