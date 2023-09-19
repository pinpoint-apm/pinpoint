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
package com.navercorp.pinpoint.realtime.collector.dao;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.channel.serde.Serde;
import com.navercorp.pinpoint.realtime.serde.CollectorStateSerde;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
class PubCollectorStateDao implements CollectorStateDao {

    private final Logger logger = LogManager.getLogger(PubCollectorStateDao.class);

    private final ChannelProviderRepository channelProviderRepository;
    private final URI pubChannelURI;

    private final AtomicReference<PubChannel> pubChannelRef = new AtomicReference<>();
    private final Serde<CollectorState> serde = new CollectorStateSerde();

    public PubCollectorStateDao(ChannelProviderRepository channelProviderRepository, URI pubChannelURI) {
        this.channelProviderRepository = Objects.requireNonNull(channelProviderRepository, "channelProviderRepository");
        this.pubChannelURI = Objects.requireNonNull(pubChannelURI, "pubChannelURI");
    }

    @Override
    public void update(CollectorState state) {
        try {
            byte[] bytes = this.serde.serializeToByteArray(state);
            this.getPubChannel().publish(bytes);
        } catch (Exception e) {
            logger.error("Failed to update collector state {}", state);
        }
    }

    private PubChannel getPubChannel(){
        PubChannel pubChannel = this.pubChannelRef.get();
        if (pubChannel != null) {
            return pubChannel;
        }

        PubChannel newPubChannel = this.buildPubChannel();
        if (!this.pubChannelRef.compareAndSet(null, newPubChannel)) {
            return this.getPubChannel();
        } else {
            return newPubChannel;
        }
    }

    private PubChannel buildPubChannel() {
        return this.channelProviderRepository.getPubChannel(this.pubChannelURI);
    }

}
