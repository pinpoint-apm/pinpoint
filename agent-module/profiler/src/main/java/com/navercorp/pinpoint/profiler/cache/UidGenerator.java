package com.navercorp.pinpoint.profiler.cache;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.function.Function;

public interface UidGenerator extends Function<String, byte[]> {
    class Murmur implements UidGenerator {
        private static final HashFunction hashFunction = Hashing.murmur3_128();

        @Override
        public byte[] apply(String s) {
            return hashFunction.hashBytes(s.getBytes()).asBytes();
        }
    }
}
