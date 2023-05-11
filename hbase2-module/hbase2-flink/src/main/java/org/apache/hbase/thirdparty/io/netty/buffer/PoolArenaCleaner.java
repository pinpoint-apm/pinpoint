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
package org.apache.hbase.thirdparty.io.netty.buffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/**
 * @author youngjin.kim2
 */
public class PoolArenaCleaner {

    private static final Logger logger = LogManager.getLogger(PoolArenaCleaner.class);

    public static void finalizeExplicitly() {
        try {
            logger.info("Destroying netty direct buffer chunks");
            final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
            final PoolArena<?>[] arenas = getDirectBufferArenas(allocator);
            for (final PoolArena<?> arena: arenas) {
                if (arena != null) {
                    arena.finalize();
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to destroy netty direct buffer chunks", e);
        }
    }

    private static PoolArena<?>[] getDirectBufferArenas(PooledByteBufAllocator allocator) throws Exception {
        final Field directArenasField = PooledByteBufAllocator.class.getDeclaredField("directArenas");
        directArenasField.setAccessible(true);
        final Object arenas = directArenasField.get(allocator);
        if (arenas instanceof PoolArena<?>[]) {
            return (PoolArena<?>[]) arenas;
        } else {
            throw new RuntimeException("Invalid field: directArenas");
        }
    }

}
