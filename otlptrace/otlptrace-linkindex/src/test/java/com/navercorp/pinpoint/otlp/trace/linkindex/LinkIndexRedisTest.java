/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.linkindex;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LettuceLinkIndexRedisConnector;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntryCodecTest.traceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Writer, join and cleanup against a real Redis (same major version as nbase-arc2).
 */
@Testcontainers(disabledWithoutDocker = true)
class LinkIndexRedisTest {

    private static final int BUCKETS = 16;
    private static final long TTL_MILLIS = 300_000;
    private static final int OTEL_SERVER = ServiceType.OPENTELEMETRY_SERVER.getCode();

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:5.0"))
            .withExposedPorts(6379);

    private LettuceLinkIndexRedisConnector connector;
    private LinkIndexConnection admin;
    private final AtomicLong now = new AtomicLong();
    private final LinkMapWriter mapWriter = mock(LinkMapWriter.class);

    // EXPIREAT is an absolute time checked against the Redis clock, so the slots must be current
    private final long slot = LinkIndexKeys.slotOf(System.currentTimeMillis());

    @BeforeEach
    void setUp() throws Exception {
        connector = new LettuceLinkIndexRedisConnector(LettuceLinkIndexRedisConnector.Mode.STANDALONE,
                List.of(REDIS.getHost() + ":" + REDIS.getFirstMappedPort()), null,
                Duration.ofSeconds(5), Duration.ofSeconds(5));
        admin = connector.connect();
        admin.async().flushall().get(5, TimeUnit.SECONDS);
        when(mapWriter.write(anyMap())).thenAnswer(invocation -> ((Map<?, ?>) invocation.getArgument(0)).size());
    }

    @AfterEach
    void tearDown() {
        admin.close();
        connector.close();
    }

    private LinkIndexWriter newWriter(int maxEntriesPerSlot) {
        LinkIndexWriter writer = new LinkIndexWriter(connector.connect(), BUCKETS, TTL_MILLIS, 10_000,
                maxEntriesPerSlot, 1, Duration.ofSeconds(5), now::get);
        writer.start();
        return writer;
    }

    private LinkJoinService newJoinService() {
        LinkIndexStore store = new LinkIndexStore(connector.connect(), Duration.ofSeconds(5));
        return new LinkJoinService(store, mapWriter, LinkAggregatorTest.LOOKUP, BUCKETS, 3, 1);
    }

    private static LinkEntry server(int trace) {
        return LinkEntry.server(traceId(trace), trace, 3, "p.cart", OTEL_SERVER, trace, false);
    }

    private static LinkEntry client(int trace) {
        return LinkEntry.client(traceId(trace), trace, 1, "p.checkout", OTEL_SERVER, "cart:7070");
    }

    private void writeAt(LinkIndexWriter writer, long slot, List<LinkEntry> entries) {
        now.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
        long before = writer.getWritten();
        for (LinkEntry entry : entries) {
            assertThat(writer.offer(entry)).isTrue();
        }
        long deadline = System.currentTimeMillis() + 10_000;
        while (writer.getWritten() < before + entries.size()) {
            assertThat(System.currentTimeMillis()).as("writer flush").isLessThan(deadline);
            Thread.yield();
        }
    }

    private RedisClusterAsyncCommands<byte[], byte[]> redis() {
        return admin.async();
    }

    @Test
    void writerSetsTheAbsoluteExpiryOncePerSlotAndBucket() throws Exception {
        try (LinkIndexWriter writer = newWriter(1_000)) {
            List<LinkEntry> entries = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                entries.add(server(i));
            }
            writeAt(writer, slot, entries);

            long touchedBuckets = entries.stream().map(e -> LinkIndexKeys.bucketOf(e.traceId(), BUCKETS)).distinct().count();
            assertThat(writer.getExpireAtSent()).isEqualTo(touchedBuckets);

            int bucket = LinkIndexKeys.bucketOf(entries.get(0).traceId(), BUCKETS);
            byte[] key = LinkIndexKeys.entryKey(slot, bucket);
            long expected = LinkIndexKeys.expireAtSeconds(slot, bucket, TTL_MILLIS);
            long ttl = redis().ttl(key).get(5, TimeUnit.SECONDS);
            long expireAt = System.currentTimeMillis() / 1000 + ttl;
            assertThat(Math.abs(expireAt - expected)).isLessThanOrEqualTo(2);

            // the same buckets again: no further EXPIREAT
            writeAt(writer, slot, List.of(server(1), server(2)));
            assertThat(writer.getExpireAtSent()).isEqualTo(touchedBuckets);
        }
    }

    @Test
    void aFailedCommandIsAWriteFailureAndTheExpiryIsSentAgainWithTheNextWrite() throws Exception {
        try (LinkIndexWriter writer = newWriter(1_000)) {
            byte[] key = LinkIndexKeys.entryKey(slot, LinkIndexKeys.bucketOf(traceId(1), BUCKETS));
            // the key exists as a string: HSET fails with WRONGTYPE and awaitAll throws instead of returning false
            redis().set(key, new byte[]{1}).get(5, TimeUnit.SECONDS);
            now.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
            assertThat(writer.offer(server(1))).isTrue();
            long deadline = System.currentTimeMillis() + 10_000;
            while (writer.getWriteErrors() < 1) {
                assertThat(System.currentTimeMillis()).as("write failure").isLessThan(deadline);
                Thread.yield();
            }
            assertThat(writer.getWritten()).isZero();
            assertThat(writer.getExpireAtSent()).isZero();

            // the key is gone: the next write to the bucket creates the hash and sends EXPIREAT again
            redis().del(key).get(5, TimeUnit.SECONDS);
            writeAt(writer, slot, List.of(server(1)));
            assertThat(writer.getExpireAtSent()).isEqualTo(1);
            assertThat(redis().hlen(key).get(5, TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(redis().ttl(key).get(5, TimeUnit.SECONDS)).isPositive();
        }
    }

    @Test
    void aLateWriteAfterTheCleanupIsDroppedInsteadOfRecreatingTheKeyWithoutExpiry() throws Exception {
        // the ingest threads stamp the slot with their clock, the writer thread judges lateness with its own
        AtomicLong writerNow = new AtomicLong();
        LongSupplier clock = () -> Thread.currentThread().getName().equals("otlp-link-index-writer") ? writerNow.get() : now.get();
        LinkIndexWriter writer = new LinkIndexWriter(connector.connect(), BUCKETS, TTL_MILLIS, 10_000, 1_000, 1, Duration.ofSeconds(5), clock);
        writer.start();
        try (writer; LinkJoinService join = newJoinService()) {
            // slot k: EXPIREAT sent for the bucket of trace 1
            writerNow.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
            writeAt(writer, slot, List.of(server(1)));
            byte[] key = LinkIndexKeys.entryKey(slot, LinkIndexKeys.bucketOf(traceId(1), BUCKETS));
            assertThat(redis().ttl(key).get(5, TimeUnit.SECONDS)).isPositive();

            // an entry of k written during k+1 is in time (the job of k+1 reads it)
            writerNow.set(LinkIndexKeys.slotStartMillis(slot + 1) + 1_000);
            writeAt(writer, slot, List.of(server(2)));
            byte[] key2 = LinkIndexKeys.entryKey(slot, LinkIndexKeys.bucketOf(traceId(2), BUCKETS));
            assertThat(redis().ttl(key2).get(5, TimeUnit.SECONDS)).isPositive();

            // the cleanup of k+1 (slot k+2) deletes the keys of k
            join.join(slot + 1);
            join.cleanup(slot + 1);
            assertThat(redis().exists(key).get(5, TimeUnit.SECONDS)).isZero();

            // an entry of k still queued at that point: dropped, the key is not recreated
            writerNow.set(LinkIndexKeys.slotStartMillis(slot + 2) + 1_000);
            now.set(LinkIndexKeys.slotStartMillis(slot) + 2_000);
            long written = writer.getWritten();
            assertThat(writer.offer(server(1))).isTrue();
            long deadline = System.currentTimeMillis() + 10_000;
            while (writer.getDroppedLate() < 1) {
                assertThat(System.currentTimeMillis()).as("late drop").isLessThan(deadline);
                Thread.yield();
            }
            assertThat(writer.getWritten()).isEqualTo(written);
            assertThat(redis().exists(key).get(5, TimeUnit.SECONDS)).isZero();
        }
    }

    @Test
    void oversizedBucketsAreReported() {
        LinkIndexStore store = new LinkIndexStore(connector.connect(), Duration.ofSeconds(5));
        try (LinkIndexWriter writer = newWriter(1_000);
             LinkJoinService join = new LinkJoinService(store, mapWriter, LinkAggregatorTest.LOOKUP, BUCKETS, 2, 1, 4, 5)) {
            List<LinkEntry> entries = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                entries.add(server(i));
            }
            writeAt(writer, slot, entries);

            LinkJoinService.JoinSummary summary = join.join(slot);

            int[] perBucket = new int[BUCKETS];
            for (LinkEntry entry : entries) {
                perBucket[LinkIndexKeys.bucketOf(entry.traceId(), BUCKETS)]++;
            }
            long expectedOversized = Arrays.stream(perBucket).filter(n -> n > 5).count();
            int expectedMax = Arrays.stream(perBucket).max().orElse(0);
            assertThat(expectedOversized).isPositive();
            assertThat(summary.oversizedBuckets()).isEqualTo(expectedOversized);
            assertThat(summary.maxBucketEntries()).isEqualTo(expectedMax);
            join.cleanup(slot);
        }
    }

    @Test
    void writerDropsBeyondTheSlotLimit() {
        try (LinkIndexWriter writer = newWriter(3)) {
            now.set(LinkIndexKeys.slotStartMillis(slot));
            assertThat(writer.offer(server(1))).isTrue();
            assertThat(writer.offer(server(2))).isTrue();
            assertThat(writer.offer(server(3))).isTrue();
            assertThat(writer.offer(server(4))).isFalse();
            assertThat(writer.getDroppedSlotLimit()).isEqualTo(1);

            now.set(LinkIndexKeys.slotStartMillis(slot + 1));
            assertThat(writer.offer(server(5))).isTrue();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void joinAcrossSlotsCleanupAndRerun() throws Exception {
        try (LinkIndexWriter writer = newWriter(1_000); LinkJoinService join = newJoinService()) {
            // slot k-1: call 2's SERVER arrives first, call 3 only has a CLIENT (never matched)
            writeAt(writer, slot - 1, List.of(server(2), client(3)));
            LinkJoinService.JoinSummary previous = join.join(slot - 1);
            assertThat(previous.pairs()).isZero();
            assertThat(previous.unmatched()).isEqualTo(2);
            join.cleanup(slot - 1);
            reset(mapWriter);
            when(mapWriter.write(anyMap())).thenAnswer(invocation -> ((Map<?, ?>) invocation.getArgument(0)).size());

            // the job wrote nothing to the store: the entries of k-1 are still there for the job of k
            int bucket2 = LinkIndexKeys.bucketOf(traceId(2), BUCKETS);
            assertThat(redis().hlen(LinkIndexKeys.entryKey(slot - 1, bucket2)).get(5, TimeUnit.SECONDS)).isEqualTo(1);

            // slot k: call 1 fully in k, call 2's CLIENT arrives in k
            writeAt(writer, slot, List.of(server(1), client(1), client(2), server(4)));
            LinkJoinService.JoinSummary summary = join.join(slot);
            assertThat(summary.entries()).isEqualTo(4);
            assertThat(summary.pairs()).isEqualTo(2);
            assertThat(summary.unmatched()).isEqualTo(1);      // server(4)
            assertThat(summary.droppedClient()).isEqualTo(1);  // client(3) of k-1

            ArgumentCaptor<Map<LinkKey, LinkStat>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mapWriter).write(captor.capture());
            Map<LinkKey, LinkStat> stats = captor.getValue();
            assertThat(stats.keySet()).extracting(LinkKey::slot, LinkKey::source)
                    .containsExactlyInAnyOrder(
                            tuple(slot, LinkSource.SAME_SLOT),
                            tuple(slot - 1, LinkSource.CROSS_SLOT));

            // a re-run before cleanup reads the same input and produces the same output
            LinkJoinService.JoinSummary rerun = join.join(slot);
            assertThat(rerun).isEqualTo(summary);
            ArgumentCaptor<Map<LinkKey, LinkStat>> both = ArgumentCaptor.forClass(Map.class);
            verify(mapWriter, times(2)).write(both.capture());
            Map<LinkKey, LinkStat> first = both.getAllValues().get(0);
            Map<LinkKey, LinkStat> second = both.getAllValues().get(1);
            assertThat(second.keySet()).isEqualTo(first.keySet());
            for (LinkKey key : first.keySet()) {
                assertThat(second.get(key).getHistogram()).isEqualTo(first.get(key).getHistogram());
                assertThat(second.get(key).getSum()).isEqualTo(first.get(key).getSum());
            }

            // cleanup of k deletes the entries of k-1 and keeps the entries of k for the job of k+1
            LinkJoinService.CleanupSummary cleanup = join.cleanup(slot);
            assertThat(cleanup.deletedKeys()).isPositive();
            for (int bucket = 0; bucket < BUCKETS; bucket++) {
                assertThat(redis().exists(LinkIndexKeys.entryKey(slot - 1, bucket)).get(5, TimeUnit.SECONDS)).isZero();
            }
            int bucket4 = LinkIndexKeys.bucketOf(traceId(4), BUCKETS);
            assertThat(redis().exists(LinkIndexKeys.entryKey(slot, bucket4)).get(5, TimeUnit.SECONDS)).isEqualTo(1);

            // the job of k+1 reads k again but does not count the calls completed inside k
            reset(mapWriter);
            when(mapWriter.write(anyMap())).thenAnswer(invocation -> ((Map<?, ?>) invocation.getArgument(0)).size());
            LinkJoinService.JoinSummary next = join.join(slot + 1);
            assertThat(next.pairs()).isZero();
            assertThat(next.droppedServer()).isEqualTo(1);     // server(4) never matched

            // cleanup is idempotent
            join.cleanup(slot);
            assertThat(join.cleanup(slot).deletedKeys()).isZero();

            // running totals sum every join (k-1, k, re-run of k, k+1) and every cleanup
            assertThat(join.getTotalEntries()).isEqualTo(previous.entries() + summary.entries() * 2 + next.entries());
            assertThat(join.getTotalPairs()).isEqualTo(summary.pairs() * 2);
            assertThat(join.getTotalRows()).isEqualTo(summary.rows() * 2);
            assertThat(join.getTotalUnmatched()).isEqualTo(previous.unmatched() + summary.unmatched() * 2 + next.unmatched());
            // server(4) by the job of k+1; client(3) by the job of k and its re-run, client(2) by the job of k+1
            // (its partner server(2) is in k-1, which that job does not read)
            assertThat(join.getTotalDroppedServer()).isEqualTo(1);
            assertThat(join.getTotalDroppedClient()).isEqualTo(3);
            assertThat(join.getTotalDeletedKeys()).isEqualTo(cleanup.deletedKeys());
            assertThat(join.getLastMaxBucketEntries()).isEqualTo(next.maxBucketEntries());
        }
    }
}
