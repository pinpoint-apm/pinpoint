package com.navercorp.pinpoint.collector.receiver.grpc.keepalive;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.Attributes;
import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerTransportFilter;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server that manages startup/shutdown of a keep alive server.
 */
public class KeepAliveServer {
    private static final Logger logger = LogManager.getLogger(KeepAliveServer.class);

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;

        // Start a server with the following configurations (demo only, you should set more appropriate
        // values based on your real environment):
        // keepAliveTime: Ping the client if it is idle for 5 seconds to ensure the connection is
        // still active. Set to an appropriate value in reality, e.g. in minutes.
        // keepAliveTimeout: Wait 1 second for the ping ack before assuming the connection is dead.
        // Set to an appropriate value in reality, e.g. (10, TimeUnit.SECONDS).
        // permitKeepAliveTime: If a client pings more than once every 5 seconds, terminate the
        // connection.
        // permitKeepAliveWithoutCalls: Allow pings even when there are no active streams.
        // maxConnectionIdle: If a client is idle for 15 seconds, send a GOAWAY.
        // maxConnectionAge: If any connection is alive for more than 30 seconds, send a GOAWAY.
        // maxConnectionAgeGrace: Allow 5 seconds for pending RPCs to complete before forcibly closing
        // connections.
        // Use JAVA_OPTS=-Djava.util.logging.config.file=logging.properties to see keep alive ping
        // frames.
        // More details see: https://github.com/grpc/proposal/blob/master/A9-server-side-conn-mgt.md
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addTransportFilter(new ServerTransportFilter() {
                    @Override
                    public Attributes transportReady(Attributes transportAttrs) {
                        logger.info("transportReady: {}", transportAttrs);
                        return transportAttrs;
                    }

                    @Override
                    public void transportTerminated(Attributes transportAttrs) {
                        logger.info("transportTerminated: {}", transportAttrs);
                    }
                })
                .addService(new SpanImpl())
                .keepAliveTime(5, TimeUnit.SECONDS)
                .keepAliveTimeout(1, TimeUnit.SECONDS)
                .permitKeepAliveTime(7, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(false)
//                .intercept(new StreamKeepAliveInterceptor("SpanStream"))
                .maxConnectionIdle(10, TimeUnit.SECONDS)
                .maxConnectionAge(2000, TimeUnit.SECONDS)
                .maxConnectionAgeGrace(5, TimeUnit.SECONDS)
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    KeepAliveServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final KeepAliveServer server = new KeepAliveServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class SpanImpl extends SpanGrpc.SpanImplBase {
        private final AtomicLong idAllocator = new AtomicLong(0);

        @Override
        public StreamObserver<PSpanMessage> sendSpan(StreamObserver<Empty> response) {
            long id = idAllocator.incrementAndGet();
            Context current = Context.current();
            Deadline deadline = current.getDeadline();

            logger.info("connect stream id:{} {} {}", id, current, deadline);

//            response.onNext();
            return new StreamObserver<>() {
                @Override
                public void onNext(PSpanMessage pSpanMessage) {

                    logger.info("{} onNext: {}", id,  pSpanMessage);

                }

                @Override
                public void onError(Throwable throwable) {
                    Status status = Status.fromThrowable(throwable);
                    Metadata metadata = Status.trailersFromThrowable(throwable);
                    logger.info("{} onError throwable {} {}", id, status, metadata);
                }

                @Override
                public void onCompleted() {
                    logger.info("{} onCompleted", id);
                }
            };
        }

    }
}
