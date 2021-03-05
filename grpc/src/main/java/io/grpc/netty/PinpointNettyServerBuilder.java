/*
 * Copyright 2014 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.netty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.grpc.internal.GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE;
import static io.grpc.internal.GrpcUtil.DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS;
import static io.grpc.internal.GrpcUtil.DEFAULT_SERVER_KEEPALIVE_TIME_NANOS;
import static io.grpc.internal.GrpcUtil.SERVER_KEEPALIVE_TIME_NANOS_DISABLED;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.grpc.Attributes;
import io.grpc.ExperimentalApi;
import io.grpc.Internal;
import io.grpc.ServerBuilder;
import io.grpc.ServerCredentials;
import io.grpc.ServerStreamTracer;
import io.grpc.internal.AbstractServerImplBuilder;
import io.grpc.internal.FixedObjectPool;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.InternalServer;
import io.grpc.internal.KeepAliveManager;
import io.grpc.internal.ObjectPool;
import io.grpc.internal.ServerImplBuilder;
import io.grpc.internal.ServerImplBuilder.ClientTransportServersBuilder;
import io.grpc.internal.ServerListener;
import io.grpc.internal.SharedResourcePool;
import io.grpc.internal.TransportTracer;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckReturnValue;
import javax.net.ssl.SSLException;

/**
 * copy & modify : NettyServerBuilder
 * A builder to help simplify the construction of a Netty-based GRPC server.
 */
@ExperimentalApi("https://github.com/grpc/grpc-java/issues/1784")
@CanIgnoreReturnValue
public final class PinpointNettyServerBuilder extends AbstractServerImplBuilder<PinpointNettyServerBuilder> {

    // 1MiB
    public static final int DEFAULT_FLOW_CONTROL_WINDOW = 1024 * 1024;

    static final long MAX_CONNECTION_IDLE_NANOS_DISABLED = Long.MAX_VALUE;
    static final long MAX_CONNECTION_AGE_NANOS_DISABLED = Long.MAX_VALUE;
    static final long MAX_CONNECTION_AGE_GRACE_NANOS_INFINITE = Long.MAX_VALUE;

    private static final long MIN_KEEPALIVE_TIME_NANO = TimeUnit.MILLISECONDS.toNanos(1L);
    private static final long MIN_KEEPALIVE_TIMEOUT_NANO = TimeUnit.MICROSECONDS.toNanos(499L);
    private static final long MIN_MAX_CONNECTION_IDLE_NANO = TimeUnit.SECONDS.toNanos(1L);
    private static final long MIN_MAX_CONNECTION_AGE_NANO = TimeUnit.SECONDS.toNanos(1L);
    private static final long AS_LARGE_AS_INFINITE = TimeUnit.DAYS.toNanos(1000L);
    private static final ObjectPool<? extends EventLoopGroup> DEFAULT_BOSS_EVENT_LOOP_GROUP_POOL =
            SharedResourcePool.forResource(Utils.DEFAULT_BOSS_EVENT_LOOP_GROUP);
    private static final ObjectPool<? extends EventLoopGroup> DEFAULT_WORKER_EVENT_LOOP_GROUP_POOL =
            SharedResourcePool.forResource(Utils.DEFAULT_WORKER_EVENT_LOOP_GROUP);

    private final ServerImplBuilder serverImplBuilder;
    private final List<SocketAddress> listenAddresses = new ArrayList<>();

    private TransportTracer.Factory transportTracerFactory = TransportTracer.getDefaultFactory();
    private ChannelFactory<? extends ServerChannel> channelFactory =
            Utils.DEFAULT_SERVER_CHANNEL_FACTORY;
    private final Map<ChannelOption<?>, Object> channelOptions = new HashMap<>();
    private final Map<ChannelOption<?>, Object> childChannelOptions = new HashMap<>();
    private ObjectPool<? extends EventLoopGroup> bossEventLoopGroupPool =
            DEFAULT_BOSS_EVENT_LOOP_GROUP_POOL;
    private ObjectPool<? extends EventLoopGroup> workerEventLoopGroupPool =
            DEFAULT_WORKER_EVENT_LOOP_GROUP_POOL;
    private boolean forceHeapBuffer;
    private ProtocolNegotiator.ServerFactory protocolNegotiatorFactory;
    private final boolean freezeProtocolNegotiatorFactory;
    private int maxConcurrentCallsPerConnection = Integer.MAX_VALUE;
    private boolean autoFlowControl = true;
    private int flowControlWindow = DEFAULT_FLOW_CONTROL_WINDOW;
    private int maxMessageSize = DEFAULT_MAX_MESSAGE_SIZE;
    private int maxHeaderListSize = GrpcUtil.DEFAULT_MAX_HEADER_LIST_SIZE;
    private long keepAliveTimeInNanos = DEFAULT_SERVER_KEEPALIVE_TIME_NANOS;
    private long keepAliveTimeoutInNanos = DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS;
    private long maxConnectionIdleInNanos = MAX_CONNECTION_IDLE_NANOS_DISABLED;
    private long maxConnectionAgeInNanos = MAX_CONNECTION_AGE_NANOS_DISABLED;
    private long maxConnectionAgeGraceInNanos = MAX_CONNECTION_AGE_GRACE_NANOS_INFINITE;
    private boolean permitKeepAliveWithoutCalls;
    private long permitKeepAliveTimeInNanos = TimeUnit.MINUTES.toNanos(5);
    private Attributes eagAttributes = Attributes.EMPTY;

    /**
     * Creates a server builder that will bind to the given port.
     *
     * @param port the port on which the server is to be bound.
     * @return the server builder.
     */
    @CheckReturnValue
    public static PinpointNettyServerBuilder forPort(int port) {
        return forAddress(new InetSocketAddress(port));
    }

    /**
     * Creates a server builder that will bind to the given port.
     *
     * @param port the port on which the server is to be bound.
     * @return the server builder.
     */
    @CheckReturnValue
    public static PinpointNettyServerBuilder forPort(int port, ServerCredentials creds) {
        return forAddress(new InetSocketAddress(port), creds);
    }

    /**
     * Creates a server builder configured with the given {@link SocketAddress}.
     *
     * @param address the socket address on which the server is to be bound.
     * @return the server builder
     */
    @CheckReturnValue
    public static PinpointNettyServerBuilder forAddress(SocketAddress address) {
        return new PinpointNettyServerBuilder(address);
    }

    /**
     * Creates a server builder configured with the given {@link SocketAddress}.
     *
     * @param address the socket address on which the server is to be bound.
     * @return the server builder
     */
    @CheckReturnValue
    public static PinpointNettyServerBuilder forAddress(SocketAddress address, ServerCredentials creds) {
        ProtocolNegotiators.FromServerCredentialsResult result = ProtocolNegotiators.from(creds);
        if (result.error != null) {
            throw new IllegalArgumentException(result.error);
        }
        return new PinpointNettyServerBuilder(address, result.negotiator);
    }

    private final class NettyClientTransportServersBuilder implements ClientTransportServersBuilder {
        @Override
        public List<? extends InternalServer> buildClientTransportServers(
                List<? extends ServerStreamTracer.Factory> streamTracerFactories) {
            return buildTransportServers(streamTracerFactories);
        }
    }

    @CheckReturnValue
    private PinpointNettyServerBuilder(SocketAddress address) {
        serverImplBuilder = new ServerImplBuilder(new NettyClientTransportServersBuilder());
        this.listenAddresses.add(address);
        this.protocolNegotiatorFactory = ProtocolNegotiators.serverPlaintextFactory();
        this.freezeProtocolNegotiatorFactory = false;
    }

    @CheckReturnValue
    PinpointNettyServerBuilder(
            SocketAddress address, ProtocolNegotiator.ServerFactory negotiatorFactory) {
        serverImplBuilder = new ServerImplBuilder(new NettyClientTransportServersBuilder());
        this.listenAddresses.add(address);
        this.protocolNegotiatorFactory = checkNotNull(negotiatorFactory, "negotiatorFactory");
        this.freezeProtocolNegotiatorFactory = true;
    }

    @Internal
    @Override
    protected ServerBuilder<?> delegate() {
        return serverImplBuilder;
    }

    /**
     * Adds an additional address for this server to listen on.  Callers must ensure that all socket
     * addresses are compatible with the Netty channel type, and that they don't conflict with each
     * other.
     */
    public PinpointNettyServerBuilder addListenAddress(SocketAddress listenAddress) {
        this.listenAddresses.add(checkNotNull(listenAddress, "listenAddress"));
        return this;
    }

    /**
     * Specifies the channel type to use, by default we use {@code EpollServerSocketChannel} if
     * available, otherwise using {@link NioServerSocketChannel}.
     *
     * <p>You either use this or {@link #channelFactory(io.netty.channel.ChannelFactory)} if your
     * {@link ServerChannel} implementation has no no-args constructor.
     *
     * <p>It's an optional parameter. If the user has not provided an Channel type or ChannelFactory
     * when the channel is built, the builder will use the default one which is static.
     *
     * <p>You must also provide corresponding {@link EventLoopGroup} using {@link
     * #workerEventLoopGroup(EventLoopGroup)} and {@link #bossEventLoopGroup(EventLoopGroup)}. For
     * example, {@link NioServerSocketChannel} must use {@link
     * io.netty.channel.nio.NioEventLoopGroup}, otherwise your server won't start.
     */
    public PinpointNettyServerBuilder channelType(Class<? extends ServerChannel> channelType) {
        checkNotNull(channelType, "channelType");
        return channelFactory(new ReflectiveChannelFactory<>(channelType));
    }

    /**
     * Specifies the {@link ChannelFactory} to create {@link ServerChannel} instances. This method is
     * usually only used if the specific {@code ServerChannel} requires complex logic which requires
     * additional information to create the {@code ServerChannel}. Otherwise, recommend to use {@link
     * #channelType(Class)}.
     *
     * <p>It's an optional parameter. If the user has not provided an Channel type or ChannelFactory
     * when the channel is built, the builder will use the default one which is static.
     *
     * <p>You must also provide corresponding {@link EventLoopGroup} using {@link
     * #workerEventLoopGroup(EventLoopGroup)} and {@link #bossEventLoopGroup(EventLoopGroup)}. For
     * example, if the factory creates {@link NioServerSocketChannel} you must use {@link
     * io.netty.channel.nio.NioEventLoopGroup}, otherwise your server won't start.
     */
    public PinpointNettyServerBuilder channelFactory(ChannelFactory<? extends ServerChannel> channelFactory) {
        this.channelFactory = checkNotNull(channelFactory, "channelFactory");
        return this;
    }

    /**
     * Specifies a channel option. As the underlying channel as well as network implementation may
     * ignore this value applications should consider it a hint.
     *
     * @since 1.30.0
     */
    public <T> PinpointNettyServerBuilder withOption(ChannelOption<T> option, T value) {
        this.channelOptions.put(option, value);
        return this;
    }

    /**
     * Specifies a child channel option. As the underlying channel as well as network implementation
     * may ignore this value applications should consider it a hint.
     *
     * @since 1.9.0
     */
    public <T> PinpointNettyServerBuilder withChildOption(ChannelOption<T> option, T value) {
        this.childChannelOptions.put(option, value);
        return this;
    }

    /**
     * Provides the boss EventGroupLoop to the server.
     *
     * <p>It's an optional parameter. If the user has not provided one when the server is built, the
     * builder will use the default one which is static.
     *
     * <p>You must also provide corresponding {@link io.netty.channel.Channel} type using {@link
     * #channelType(Class)} and {@link #workerEventLoopGroup(EventLoopGroup)}. For example, {@link
     * NioServerSocketChannel} must use {@link io.netty.channel.nio.NioEventLoopGroup} for both boss
     * and worker {@link EventLoopGroup}, otherwise your server won't start.
     *
     * <p>The server won't take ownership of the given EventLoopGroup. It's caller's responsibility
     * to shut it down when it's desired.
     *
     * <p>Grpc uses non-daemon {@link Thread}s by default and thus a {@link io.grpc.Server} will
     * continue to run even after the main thread has terminated. However, users have to be cautious
     * when providing their own {@link EventLoopGroup}s.
     * For example, Netty's {@link EventLoopGroup}s use daemon threads by default
     * and thus an application with only daemon threads running besides the main thread will exit as
     * soon as the main thread completes.
     * A simple solution to this problem is to call {@link io.grpc.Server#awaitTermination()} to
     * keep the main thread alive until the server has terminated.
     */
    public PinpointNettyServerBuilder bossEventLoopGroup(EventLoopGroup group) {
        if (group != null) {
            return bossEventLoopGroupPool(new FixedObjectPool<>(group));
        }
        return bossEventLoopGroupPool(DEFAULT_BOSS_EVENT_LOOP_GROUP_POOL);
    }

    PinpointNettyServerBuilder bossEventLoopGroupPool(
            ObjectPool<? extends EventLoopGroup> bossEventLoopGroupPool) {
        this.bossEventLoopGroupPool = checkNotNull(bossEventLoopGroupPool, "bossEventLoopGroupPool");
        return this;
    }

    /**
     * Provides the worker EventGroupLoop to the server.
     *
     * <p>It's an optional parameter. If the user has not provided one when the server is built, the
     * builder will create one.
     *
     * <p>You must also provide corresponding {@link io.netty.channel.Channel} type using {@link
     * #channelType(Class)} and {@link #bossEventLoopGroup(EventLoopGroup)}. For example, {@link
     * NioServerSocketChannel} must use {@link io.netty.channel.nio.NioEventLoopGroup} for both boss
     * and worker {@link EventLoopGroup}, otherwise your server won't start.
     *
     * <p>The server won't take ownership of the given EventLoopGroup. It's caller's responsibility
     * to shut it down when it's desired.
     *
     * <p>Grpc uses non-daemon {@link Thread}s by default and thus a {@link io.grpc.Server} will
     * continue to run even after the main thread has terminated. However, users have to be cautious
     * when providing their own {@link EventLoopGroup}s.
     * For example, Netty's {@link EventLoopGroup}s use daemon threads by default
     * and thus an application with only daemon threads running besides the main thread will exit as
     * soon as the main thread completes.
     * A simple solution to this problem is to call {@link io.grpc.Server#awaitTermination()} to
     * keep the main thread alive until the server has terminated.
     */
    public PinpointNettyServerBuilder workerEventLoopGroup(EventLoopGroup group) {
        if (group != null) {
            return workerEventLoopGroupPool(new FixedObjectPool<>(group));
        }
        return workerEventLoopGroupPool(DEFAULT_WORKER_EVENT_LOOP_GROUP_POOL);
    }

    PinpointNettyServerBuilder workerEventLoopGroupPool(
            ObjectPool<? extends EventLoopGroup> workerEventLoopGroupPool) {
        this.workerEventLoopGroupPool =
                checkNotNull(workerEventLoopGroupPool, "workerEventLoopGroupPool");
        return this;
    }

    /**
     * Force using heap buffer when custom allocator is enabled.
     */
    void setForceHeapBuffer(boolean value) {
        forceHeapBuffer = value;
    }

    /**
     * Sets the TLS context to use for encryption. Providing a context enables encryption. It must
     * have been configured with {@link GrpcSslContexts}, but options could have been overridden.
     */
    public PinpointNettyServerBuilder sslContext(SslContext sslContext) {
        checkState(!freezeProtocolNegotiatorFactory,
                "Cannot change security when using ServerCredentials");
        if (sslContext != null) {
            checkArgument(sslContext.isServer(),
                    "Client SSL context can not be used for server");
            GrpcSslContexts.ensureAlpnAndH2Enabled(sslContext.applicationProtocolNegotiator());
            protocolNegotiatorFactory = ProtocolNegotiators.serverTlsFactory(sslContext);
        } else {
            protocolNegotiatorFactory = ProtocolNegotiators.serverPlaintextFactory();
        }
        return this;
    }

    /**
     * Sets the {@link ProtocolNegotiator} to be used. Overrides the value specified in {@link
     * #sslContext(SslContext)}.
     */
    @Internal
    public final PinpointNettyServerBuilder protocolNegotiator(ProtocolNegotiator protocolNegotiator) {
        checkState(!freezeProtocolNegotiatorFactory,
                "Cannot change security when using ServerCredentials");
        this.protocolNegotiatorFactory = ProtocolNegotiators.fixedServerFactory(protocolNegotiator);
        return this;
    }

    public void setTracingEnabled(boolean value) {
        this.serverImplBuilder.setTracingEnabled(value);
    }

    public void setStatsEnabled(boolean value) {
        this.serverImplBuilder.setStatsEnabled(value);
    }

    public void setStatsRecordStartedRpcs(boolean value) {
        this.serverImplBuilder.setStatsRecordStartedRpcs(value);
    }

    public void setStatsRecordRealTimeMetrics(boolean value) {
        this.serverImplBuilder.setStatsRecordRealTimeMetrics(value);
    }

    /**
     * The maximum number of concurrent calls permitted for each incoming connection. Defaults to no
     * limit.
     */
    public PinpointNettyServerBuilder maxConcurrentCallsPerConnection(int maxCalls) {
        checkArgument(maxCalls > 0, "max must be positive: %s", maxCalls);
        this.maxConcurrentCallsPerConnection = maxCalls;
        return this;
    }

    /**
     * Sets the initial flow control window in bytes. Setting initial flow control window enables auto
     * flow control tuning using bandwidth-delay product algorithm. To disable auto flow control
     * tuning, use {@link #flowControlWindow(int)}. By default, auto flow control is enabled with
     * initial flow control window size of {@link #DEFAULT_FLOW_CONTROL_WINDOW}.
     */
    public PinpointNettyServerBuilder initialFlowControlWindow(int initialFlowControlWindow) {
        checkArgument(initialFlowControlWindow > 0, "initialFlowControlWindow must be positive");
        this.flowControlWindow = initialFlowControlWindow;
        this.autoFlowControl = true;
        return this;
    }

    /**
     * Sets the flow control window in bytes. Setting flowControlWindow disables auto flow control
     * tuning; use {@link #initialFlowControlWindow(int)} to enable auto flow control tuning. If not
     * called, the default value is {@link #DEFAULT_FLOW_CONTROL_WINDOW}) with auto flow control
     * tuning.
     */
    public PinpointNettyServerBuilder flowControlWindow(int flowControlWindow) {
        checkArgument(flowControlWindow > 0, "flowControlWindow must be positive: %s",
                flowControlWindow);
        this.flowControlWindow = flowControlWindow;
        this.autoFlowControl = false;
        return this;
    }

    /**
     * Sets the maximum message size allowed to be received on the server. If not called,
     * defaults to 4 MiB. The default provides protection to services who haven't considered the
     * possibility of receiving large messages while trying to be large enough to not be hit in normal
     * usage.
     *
     * @deprecated Call {@link #maxInboundMessageSize} instead. This method will be removed in a
     *     future release.
     */
    @Deprecated
    public PinpointNettyServerBuilder maxMessageSize(int maxMessageSize) {
        return maxInboundMessageSize(maxMessageSize);
    }

    /** {@inheritDoc} */
    @Override
    public PinpointNettyServerBuilder maxInboundMessageSize(int bytes) {
        checkArgument(bytes >= 0, "bytes must be non-negative: %s", bytes);
        this.maxMessageSize = bytes;
        return this;
    }

    /**
     * Sets the maximum size of header list allowed to be received. This is cumulative size of the
     * headers with some overhead, as defined for
     * <a href="http://httpwg.org/specs/rfc7540.html#rfc.section.6.5.2">
     * HTTP/2's SETTINGS_MAX_HEADER_LIST_SIZE</a>. The default is 8 KiB.
     *
     * @deprecated Use {@link #maxInboundMetadataSize} instead
     */
    @Deprecated
    public PinpointNettyServerBuilder maxHeaderListSize(int maxHeaderListSize) {
        return maxInboundMetadataSize(maxHeaderListSize);
    }

    /**
     * Sets the maximum size of metadata allowed to be received. This is cumulative size of the
     * entries with some overhead, as defined for
     * <a href="http://httpwg.org/specs/rfc7540.html#rfc.section.6.5.2">
     * HTTP/2's SETTINGS_MAX_HEADER_LIST_SIZE</a>. The default is 8 KiB.
     *
     * @param bytes the maximum size of received metadata
     * @return this
     * @throws IllegalArgumentException if bytes is non-positive
     * @since 1.17.0
     */
    @Override
    public PinpointNettyServerBuilder maxInboundMetadataSize(int bytes) {
        checkArgument(bytes > 0, "maxInboundMetadataSize must be positive: %s", bytes);
        this.maxHeaderListSize = bytes;
        return this;
    }

    /**
     * Sets a custom keepalive time, the delay time for sending next keepalive ping. An unreasonably
     * small value might be increased, and {@code Long.MAX_VALUE} nano seconds or an unreasonably
     * large value will disable keepalive.
     *
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder keepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
        checkArgument(keepAliveTime > 0L, "keepalive time must be positive：%s", keepAliveTime);
        keepAliveTimeInNanos = timeUnit.toNanos(keepAliveTime);
        keepAliveTimeInNanos = KeepAliveManager.clampKeepAliveTimeInNanos(keepAliveTimeInNanos);
        if (keepAliveTimeInNanos >= AS_LARGE_AS_INFINITE) {
            // Bump keepalive time to infinite. This disables keep alive.
            keepAliveTimeInNanos = SERVER_KEEPALIVE_TIME_NANOS_DISABLED;
        }
        if (keepAliveTimeInNanos < MIN_KEEPALIVE_TIME_NANO) {
            // Bump keepalive time.
            keepAliveTimeInNanos = MIN_KEEPALIVE_TIME_NANO;
        }
        return this;
    }

    /**
     * Sets a custom keepalive timeout, the timeout for keepalive ping requests. An unreasonably small
     * value might be increased.
     *
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder keepAliveTimeout(long keepAliveTimeout, TimeUnit timeUnit) {
        checkArgument(keepAliveTimeout > 0L, "keepalive timeout must be positive: %s",
                keepAliveTimeout);
        keepAliveTimeoutInNanos = timeUnit.toNanos(keepAliveTimeout);
        keepAliveTimeoutInNanos =
                KeepAliveManager.clampKeepAliveTimeoutInNanos(keepAliveTimeoutInNanos);
        if (keepAliveTimeoutInNanos < MIN_KEEPALIVE_TIMEOUT_NANO) {
            // Bump keepalive timeout.
            keepAliveTimeoutInNanos = MIN_KEEPALIVE_TIMEOUT_NANO;
        }
        return this;
    }

    /**
     * Sets a custom max connection idle time, connection being idle for longer than which will be
     * gracefully terminated. Idleness duration is defined since the most recent time the number of
     * outstanding RPCs became zero or the connection establishment. An unreasonably small value might
     * be increased. {@code Long.MAX_VALUE} nano seconds or an unreasonably large value will disable
     * max connection idle.
     *
     * @since 1.4.0
     */
    public PinpointNettyServerBuilder maxConnectionIdle(long maxConnectionIdle, TimeUnit timeUnit) {
        checkArgument(maxConnectionIdle > 0L, "max connection idle must be positive: %s",
                maxConnectionIdle);
        maxConnectionIdleInNanos = timeUnit.toNanos(maxConnectionIdle);
        if (maxConnectionIdleInNanos >= AS_LARGE_AS_INFINITE) {
            maxConnectionIdleInNanos = MAX_CONNECTION_IDLE_NANOS_DISABLED;
        }
        if (maxConnectionIdleInNanos < MIN_MAX_CONNECTION_IDLE_NANO) {
            maxConnectionIdleInNanos = MIN_MAX_CONNECTION_IDLE_NANO;
        }
        return this;
    }

    /**
     * Sets a custom max connection age, connection lasting longer than which will be gracefully
     * terminated. An unreasonably small value might be increased.  A random jitter of +/-10% will be
     * added to it. {@code Long.MAX_VALUE} nano seconds or an unreasonably large value will disable
     * max connection age.
     *
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder maxConnectionAge(long maxConnectionAge, TimeUnit timeUnit) {
        checkArgument(maxConnectionAge > 0L, "max connection age must be positive: %s",
                maxConnectionAge);
        maxConnectionAgeInNanos = timeUnit.toNanos(maxConnectionAge);
        if (maxConnectionAgeInNanos >= AS_LARGE_AS_INFINITE) {
            maxConnectionAgeInNanos = MAX_CONNECTION_AGE_NANOS_DISABLED;
        }
        if (maxConnectionAgeInNanos < MIN_MAX_CONNECTION_AGE_NANO) {
            maxConnectionAgeInNanos = MIN_MAX_CONNECTION_AGE_NANO;
        }
        return this;
    }

    /**
     * Sets a custom grace time for the graceful connection termination. Once the max connection age
     * is reached, RPCs have the grace time to complete. RPCs that do not complete in time will be
     * cancelled, allowing the connection to terminate. {@code Long.MAX_VALUE} nano seconds or an
     * unreasonably large value are considered infinite.
     *
     * @see #maxConnectionAge(long, TimeUnit)
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder maxConnectionAgeGrace(long maxConnectionAgeGrace, TimeUnit timeUnit) {
        checkArgument(maxConnectionAgeGrace >= 0L, "max connection age grace must be non-negative: %s",
                maxConnectionAgeGrace);
        maxConnectionAgeGraceInNanos = timeUnit.toNanos(maxConnectionAgeGrace);
        if (maxConnectionAgeGraceInNanos >= AS_LARGE_AS_INFINITE) {
            maxConnectionAgeGraceInNanos = MAX_CONNECTION_AGE_GRACE_NANOS_INFINITE;
        }
        return this;
    }

    /**
     * Specify the most aggressive keep-alive time clients are permitted to configure. The server will
     * try to detect clients exceeding this rate and when detected will forcefully close the
     * connection. The default is 5 minutes.
     *
     * <p>Even though a default is defined that allows some keep-alives, clients must not use
     * keep-alive without approval from the service owner. Otherwise, they may experience failures in
     * the future if the service becomes more restrictive. When unthrottled, keep-alives can cause a
     * significant amount of traffic and CPU usage, so clients and servers should be conservative in
     * what they use and accept.
     *
     * @see #permitKeepAliveWithoutCalls(boolean)
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder permitKeepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
        checkArgument(keepAliveTime >= 0, "permit keepalive time must be non-negative: %s",
                keepAliveTime);
        permitKeepAliveTimeInNanos = timeUnit.toNanos(keepAliveTime);
        return this;
    }

    /**
     * Sets whether to allow clients to send keep-alive HTTP/2 PINGs even if there are no outstanding
     * RPCs on the connection. Defaults to {@code false}.
     *
     * @see #permitKeepAliveTime(long, TimeUnit)
     * @since 1.3.0
     */
    public PinpointNettyServerBuilder permitKeepAliveWithoutCalls(boolean permit) {
        permitKeepAliveWithoutCalls = permit;
        return this;
    }

    /** Sets the EAG attributes available to protocol negotiators. Not for general use. */
    void eagAttributes(Attributes eagAttributes) {
        this.eagAttributes = checkNotNull(eagAttributes, "eagAttributes");
    }

    //-------------------------------- modify pinpoint
    private ServerListenerDelegator serverListenerDelegator = new EmptyServerListenerDelegator();

    public void serverListenerDelegator(ServerListenerDelegator serverListenerDelegator) {
        this.serverListenerDelegator = Objects.requireNonNull(serverListenerDelegator, "serverListenerDelegator");
    }
    //--------------------------------  modify pinpoint

    @CheckReturnValue
    List<NettyServer> buildTransportServers(
            List<? extends ServerStreamTracer.Factory> streamTracerFactories) {
        assertEventLoopsAndChannelType();

        ProtocolNegotiator negotiator = protocolNegotiatorFactory.newNegotiator(
                this.serverImplBuilder.getExecutorPool());

        List<NettyServer> transportServers = new ArrayList<>(listenAddresses.size());
        for (SocketAddress listenAddress : listenAddresses) {
            NettyServer transportServer = new NettyServer(
                    listenAddress, channelFactory, channelOptions, childChannelOptions,
                    bossEventLoopGroupPool, workerEventLoopGroupPool, forceHeapBuffer, negotiator,
                    streamTracerFactories, transportTracerFactory, maxConcurrentCallsPerConnection,
                    autoFlowControl, flowControlWindow, maxMessageSize, maxHeaderListSize,
                    keepAliveTimeInNanos, keepAliveTimeoutInNanos,
                    maxConnectionIdleInNanos, maxConnectionAgeInNanos,
                    maxConnectionAgeGraceInNanos, permitKeepAliveWithoutCalls, permitKeepAliveTimeInNanos,
                    eagAttributes, this.serverImplBuilder.getChannelz()) {
                //--------------------------------  modify pinpoint
                @Override
                public void start(final ServerListener serverListener) throws IOException {
                    ServerListener delegate = serverListenerDelegator.wrapServerListener(serverListener);
                    super.start(delegate);
                }
                //--------------------------------  modify pinpoint
            };
            transportServers.add(transportServer);
        }
        return Collections.unmodifiableList(transportServers);
    }

    @VisibleForTesting
    void assertEventLoopsAndChannelType() {
        boolean allProvided = channelFactory != Utils.DEFAULT_SERVER_CHANNEL_FACTORY
                && bossEventLoopGroupPool != DEFAULT_BOSS_EVENT_LOOP_GROUP_POOL
                && workerEventLoopGroupPool != DEFAULT_WORKER_EVENT_LOOP_GROUP_POOL;
        boolean nonProvided = channelFactory == Utils.DEFAULT_SERVER_CHANNEL_FACTORY
                && bossEventLoopGroupPool == DEFAULT_BOSS_EVENT_LOOP_GROUP_POOL
                && workerEventLoopGroupPool == DEFAULT_WORKER_EVENT_LOOP_GROUP_POOL;
        checkState(
                allProvided || nonProvided,
                "All of BossEventLoopGroup, WorkerEventLoopGroup and ChannelType should be provided or "
                        + "neither should be");
    }

    PinpointNettyServerBuilder setTransportTracerFactory(
            TransportTracer.Factory transportTracerFactory) {
        this.transportTracerFactory = transportTracerFactory;
        return this;
    }

    @Override
    public PinpointNettyServerBuilder useTransportSecurity(File certChain, File privateKey) {
        checkState(!freezeProtocolNegotiatorFactory,
                "Cannot change security when using ServerCredentials");
        SslContext sslContext;
        try {
            sslContext = GrpcSslContexts.forServer(certChain, privateKey).build();
        } catch (SSLException e) {
            // This should likely be some other, easier to catch exception.
            throw new RuntimeException(e);
        }
        protocolNegotiatorFactory = ProtocolNegotiators.serverTlsFactory(sslContext);
        return this;
    }

    @Override
    public PinpointNettyServerBuilder useTransportSecurity(InputStream certChain, InputStream privateKey) {
        checkState(!freezeProtocolNegotiatorFactory,
                "Cannot change security when using ServerCredentials");
        SslContext sslContext;
        try {
            sslContext = GrpcSslContexts.forServer(certChain, privateKey).build();
        } catch (SSLException e) {
            // This should likely be some other, easier to catch exception.
            throw new RuntimeException(e);
        }
        protocolNegotiatorFactory = ProtocolNegotiators.serverTlsFactory(sslContext);
        return this;
    }
}


