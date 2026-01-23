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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public class CollectorStateSerde implements Serde<CollectorState> {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final Logger logger = LogManager.getLogger(CollectorStateSerde.class);

    private static final byte[] DELIMITER = "\r\n".getBytes(UTF_8);

    @Override
    @Nonnull
    public CollectorState deserialize(@Nonnull InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));

        List<ProfilerDescription> profilers = new ArrayList<>();
        String line;
        while(((line = reader.readLine()) != null)) {
            try {
                profilers.add(ProfilerDescription.fromString(line));
            } catch (Exception e) {
                logger.error("Invalid cluster key: {}", line, e);
            }
        }
        return new CollectorState(profilers);
    }

    @Override
    public void serialize(@Nonnull CollectorState state, @Nonnull OutputStream outputStream) throws IOException {
        List<ProfilerDescription> profilers = state.getProfilers();
        for (int i = 0; i < profilers.size(); i++) {
            ProfilerDescription key = profilers.get(i);

            outputStream.write(BytesUtils.toBytes(key.toString()));
            if (i < profilers.size() - 1) {
                outputStream.write(DELIMITER);
            }
        }
        outputStream.flush();
    }

}
