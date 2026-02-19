package com.navercorp.pinpoint.profiler.cache;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.function.Function;

public interface UidGenerator extends Function<String, byte[]> {
    class Murmur implements UidGenerator {
        private static final HashFunction hashFunction = Hashing.murmur3_128();

        @Override
        public byte[] apply(String uid) {
            return hashFunction.hashBytes(BytesUtils.toBytes(uid)).asBytes();
        }
    }
}
