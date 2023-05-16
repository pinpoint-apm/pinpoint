/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.Attributes;
import io.grpc.ConnectivityState;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.grpc.ConnectivityState.*;

/**
 * @author youngjin.kim2
 */
public class SubconnectionExpiringLoadBalancer extends LoadBalancer {
    private static final Logger logger = LogManager.getLogger(SubconnectionExpiringLoadBalancer.class);
    private final long subchannelMaxAgeMillis;

    private final Helper helper;
    private List<EquivalentAddressGroup> currentAddresses;
    private Subchannel readySubchannel;
    private Subchannel failureSubchannel;
    private Subchannel connectingSubchannel;

    private Status failureStatus;

    private boolean initialized = false;

    SubconnectionExpiringLoadBalancer(Helper helper, long renewPeriod, TimeUnit renewTimeUnit) {
        this.helper = helper;
        this.subchannelMaxAgeMillis = renewTimeUnit.toMillis(renewPeriod);
    }

    @Override
    public void handleResolvedAddresses(ResolvedAddresses resolvedAddresses) {
        final List<EquivalentAddressGroup> addresses = resolvedAddresses.getAddresses();
        this.updateAddresses(addresses);
        this.currentAddresses = addresses;

        if (!initialized) {
            initialized = true;
            this.createSubchannel();
            helper.updateBalancingState(CONNECTING, new Picker(PickResult.withNoResult()));
        }
    }

    private void updateAddresses(List<EquivalentAddressGroup> addresses) {
        if (this.readySubchannel != null) {
            this.readySubchannel.updateAddresses(addresses);
        }
        if (this.failureSubchannel != null) {
            this.failureSubchannel.updateAddresses(addresses);
        }
        if (this.connectingSubchannel != null) {
            this.connectingSubchannel.updateAddresses(addresses);
        }
    }

    private void createSubchannel() {
        Subchannel subchannel = helper.createSubchannel(
                CreateSubchannelArgs.newBuilder()
                        .setAddresses(this.currentAddresses)
                        .setAttributes(
                                Attributes.newBuilder()
                                        .set(ATTR_PICK_PROGRESS, new AtomicReference<>(PickProgress.NOT_PICKED_YET))
                                        .set(ATTR_CREATED_AT, System.currentTimeMillis())
                                        .build()
                        )
                        .build()
        );

        subchannel.start(stateInfo -> {
            final ConnectivityState state = stateInfo.getState();
            if (state == SHUTDOWN) {
                return;
            }
            if (state == TRANSIENT_FAILURE || state == IDLE) {
                helper.refreshNameResolution();
            }

            moveTo(subchannel, stateInfo);
            updateBalancingState();
        });

        subchannel.requestConnection();
    }

    private void moveTo(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
        if (this.readySubchannel == subchannel) {
            this.readySubchannel = null;
        }
        if (this.failureSubchannel == subchannel) {
            this.failureSubchannel = null;
        }
        if (this.connectingSubchannel == subchannel) {
            this.connectingSubchannel = null;
        }

        final ConnectivityState position = stateInfo.getState();

        if (position == READY) {
            if (this.readySubchannel != null) {
                this.readySubchannel.shutdown();
                logger.info("{} is replaced with {}", this.readySubchannel, subchannel);
            } else {
                logger.info("{} is now on READY", subchannel);
            }
            this.readySubchannel = subchannel;
        } else if (position == TRANSIENT_FAILURE) {
            if (this.failureSubchannel != null) {
                subchannel.shutdown();
                logger.info("{} is shutdown by conflict in FAILURE", subchannel);
            } else {
                this.failureSubchannel = subchannel;
                this.failureStatus = stateInfo.getStatus();
                logger.info("{} is now on FAILURE", subchannel);
            }
        } else if (position == CONNECTING) {
            if (this.connectingSubchannel != null) {
                subchannel.shutdown();
                logger.info("{} is shutdown by conflict in CONNECTING", subchannel);
            } else {
                this.connectingSubchannel = subchannel;
                logger.info("{} is now on CONNECTING", subchannel);
            }
        } else if (position == IDLE) {
            subchannel.requestConnection();
            logger.info("{} requested connection", subchannel);
        }

        logger.info("SE-LB state: { READY: {}, FAILURE: {}, CONNECTING: {} }",
                readySubchannel, failureSubchannel, connectingSubchannel);
    }

    private void updateBalancingState() {
        if (this.readySubchannel != null) {
            helper.updateBalancingState(READY, new Picker(PickResult.withSubchannel(this.readySubchannel), args -> requestSuccessor(this.readySubchannel)));
            return;
        }

        if (this.connectingSubchannel != null) {
            helper.updateBalancingState(CONNECTING, new Picker(PickResult.withNoResult()));
            return;
        }

        if (this.failureSubchannel != null) {
            helper.updateBalancingState(TRANSIENT_FAILURE, new Picker(PickResult.withError(failureStatus)));
            return;
        }

        helper.updateBalancingState(IDLE, new Picker(PickResult.withNoResult()));
    }

    private void requestSuccessor(Subchannel subchannel) {
        if (subchannelMaxAgeMillis >= TimeUnit.DAYS.toMillis(365)) {
            return;
        }

        final long createdAt = getCreatedAt(subchannel);
        if (createdAt < System.currentTimeMillis() - subchannelMaxAgeMillis) {
            final AtomicReference<PickProgress> progress = getPickProgress(subchannel);
            if (progress != null && progress.compareAndSet(PickProgress.NOT_PICKED_YET, PickProgress.PICKED)) {
                helper.getSynchronizationContext().execute(this::createSubchannel);
            }
        }
    }

    @Override
    public void requestConnection() {
        if (this.readySubchannel != null) {
            return;
        }
        this.createSubchannel();
    }

    @Override
    public void handleNameResolutionError(Status error) {
        clear();
        updateBalancingState();
    }

    @Override
    public void shutdown() {
        clear();
    }

    private void clear() {
        if (readySubchannel != null) {
            readySubchannel.shutdown();
            readySubchannel = null;
        }
        if (connectingSubchannel != null) {
            connectingSubchannel.shutdown();
            connectingSubchannel = null;
        }
        if (failureSubchannel != null) {
            failureSubchannel.shutdown();
            failureSubchannel = null;
        }
    }

    private static final class Picker extends SubchannelPicker {
        private final PickResult result;
        private final Consumer<PickSubchannelArgs> beforePick;

        Picker(PickResult result) {
            this(result, null);
        }

        Picker(PickResult result, Consumer<PickSubchannelArgs> beforePick) {
            this.result = result;
            this.beforePick = beforePick;
        }

        @Override
        public PickResult pickSubchannel(PickSubchannelArgs args) {
            if (beforePick != null) {
                beforePick.accept(args);
            }
            return result;
        }
    }

    private enum PickProgress {
        NOT_PICKED_YET,
        PICKED,
    }

    static final Attributes.Key<AtomicReference<PickProgress>> ATTR_PICK_PROGRESS =
            Attributes.Key.create("pick_progress");
    static final Attributes.Key<Long> ATTR_CREATED_AT =
            Attributes.Key.create("created_at");

    @Nullable
    private AtomicReference<PickProgress> getPickProgress(Subchannel subchannel) {
        final AtomicReference<PickProgress> pickProgress = subchannel.getAttributes().get(ATTR_PICK_PROGRESS);
        if (pickProgress == null) {
            logger.warn("{} does not have pickProgress", subchannel);
        }
        return pickProgress;
    }

    private long getCreatedAt(Subchannel subchannel) {
        final Long createdAt = subchannel.getAttributes().get(ATTR_CREATED_AT);
        if (createdAt == null) {
            return 0L;
        }
        return createdAt;
    }
}
