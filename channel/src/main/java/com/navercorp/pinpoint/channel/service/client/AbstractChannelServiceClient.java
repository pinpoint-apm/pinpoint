/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.channel.service.client;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubConsumer;
import com.navercorp.pinpoint.channel.Subscription;
import com.navercorp.pinpoint.channel.reactor.DeferredDisposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.Disposable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
public class AbstractChannelServiceClient<D, S> implements ChannelServiceClient {

    private final ChannelProviderRepository channelProviderRepository;
    private final ChannelServiceClientProtocol<D, S> protocol;

    public AbstractChannelServiceClient(
            ChannelProviderRepository channelProviderRepository,
            ChannelServiceClientProtocol<D, S> protocol
    ) {
        this.channelProviderRepository = Objects.requireNonNull(channelProviderRepository, "channelProviderRepository");
        this.protocol = Objects.requireNonNull(protocol, "protocol");
    }

    protected PubChannel getDemandPubChannel(D demand) {
        return this.channelProviderRepository.getPubChannel(this.protocol.getDemandPubChannelURI(demand));
    }

    protected SubChannel getSupplySubChannel(D demand) {
        return this.channelProviderRepository.getSubChannel(this.protocol.getSupplyChannelURI(demand));
    }

    protected ChannelServiceClientProtocol<D, S> getProtocol() {
        return this.protocol;
    }

    protected Subscription subscribe(
            D demand,
            Consumer<S> valueEmitter,
            Consumer<Exception> errorEmitter,
            Runnable completeEmitter
    ) {
        DeferredDisposable deferredDisposable = new DeferredDisposable();
        SubChannel supplyChannel = getSupplySubChannel(demand);
        SubConsumer subConsumer = new SupplyProxyConsumer<>(
                valueEmitter,
                errorEmitter,
                completeEmitter,
                deferredDisposable,
                getProtocol()
        );
        Subscription subscription = supplyChannel.subscribe(subConsumer);
        deferredDisposable.setDisposable(() -> subscription.unsubscribe());
        return subscription;
    }

    private static class SupplyProxyConsumer<S> implements SubConsumer {

        private static final Logger logger = LogManager.getLogger(SupplyProxyConsumer.class);

        private final Consumer<S> valueEmitter;
        private final Consumer<Exception> errorEmitter;
        private final Runnable completeEmitter;
        private final Disposable disposable;
        private final ChannelServiceClientProtocol<?, S> protocol;

        private final AtomicBoolean isComplete = new AtomicBoolean(false);

        SupplyProxyConsumer(
                Consumer<S> valueEmitter,
                Consumer<Exception> errorEmitter,
                Runnable completeEmitter,
                Disposable disposable,
                ChannelServiceClientProtocol<?, S> protocol
        ) {
            this.valueEmitter = valueEmitter;
            this.errorEmitter = errorEmitter;
            this.completeEmitter = completeEmitter;
            this.disposable = disposable;
            this.protocol = protocol;
        }

        @Override
        public boolean consume(byte[] rawSupply) {
            S supply = deserializeRawSupply(rawSupply);
            ChannelState channelState = protocol.getChannelState(supply);
            if (channelState == ChannelState.ALIVE) {
                this.next(supply);
            } else if (channelState == ChannelState.SENT_LAST_MESSAGE) {
                this.next(supply);
                this.complete();
            } else {
                this.complete();
            }
            return true;
        }

        private S deserializeRawSupply(byte[] rawSupply) {
            try {
                return this.protocol.deserializeSupply(rawSupply);
            } catch (Exception e) {
                logger.error("Failed to deserialize raw supply", e);
                this.error(e);
                throw new IllegalArgumentException("Failed to deserialize raw supply", e);
            }
        }

        private void next(S data) {
            if (!this.isComplete.get()) {
                this.valueEmitter.accept(data);
            }
        }

        private void complete() {
            if (!this.isComplete.getAndSet(true)) {
                this.completeEmitter.run();
                this.disposable.dispose();
            }
        }

        private void error(Exception t) {
            if (!this.isComplete.getAndSet(true)) {
                this.errorEmitter.accept(t);
                this.disposable.dispose();
            }
        }

    }

}
