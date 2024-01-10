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
package com.navercorp.pinpoint.realtime.serde;

import com.navercorp.pinpoint.channel.serde.Serde;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import com.navercorp.pinpoint.realtime.vo.ProfilerDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author youngjin.kim2
 */
public class CollectorStateSerde implements Serde<CollectorState> {

    private static final Logger logger = LogManager.getLogger(CollectorStateSerde.class);

    @Override
    @Nonnull
    public CollectorState deserialize(@Nonnull InputStream inputStream) throws IOException {
        String[] profilerArray = BytesUtils.toString(inputStream.readAllBytes()).split("\r\n");
        List<ProfilerDescription> profilers = new ArrayList<>(profilerArray.length);
        for (String key: profilerArray) {
            try {
                profilers.add(ProfilerDescription.fromString(key));
            } catch (Exception e) {
                logger.error("Invalid cluster key: {}", key, e);
            }
        }
        return new CollectorState(profilers);
    }

    @Override
    public void serialize(@Nonnull CollectorState state, @Nonnull OutputStream outputStream) throws IOException {
        String serialized = state.getProfilers().stream()
                .map(ProfilerDescription::toString)
                .collect(Collectors.joining("\r\n"));
        outputStream.write(BytesUtils.toBytes(serialized));
    }

}
