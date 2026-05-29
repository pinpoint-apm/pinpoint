# 컬렉터 응답시간 기반 Tail Sampling 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 컬렉터에서 트랜잭션의 루트 응답시간(elapsed)에 따라 trace 상세 저장을 차등 샘플링하되(느린 트레이스 100% 보존), servermap·heatmap 집계는 100% 유지하는 tail-based sampling을 추가한다.

**Architecture:** 모든 span을 중앙 Redis 버퍼(`buffer:{txid}`)에 적재하고, 루트 span 도착 시 `elapsed`로 밴드를 판정해 keep/drop을 결정한다. 결정 전 도착한 span은 버퍼링, 결정 후 도착한 span은 `decision:{txid}` 조회로 즉시 처리한다. 루트 미도착 트레이스는 sweeper가 buffer-ttl 경과 후 기본 keep으로 flush한다. accept/decide는 Lua 스크립트로 원자화하고, 클러스터 중복 flush는 `SET NX`로 막는다. Redis 장애 시 fail-open(전량 기록)으로 유실 0을 보장한다.

**Tech Stack:** Java 17, Spring Boot 3.5.x, spring-data-redis 3.5.x + Lettuce 6.5.x, Redis Lua, Micrometer, JUnit 5 + Mockito + AssertJ + Testcontainers(Redis), Protocol Buffers(gRPC PSpan/PSpanChunk).

**설계 문서:** `docs/superpowers/specs/2026-05-29-collector-tail-sampling-design.md`

---

## File Structure

신규 패키지: `com.navercorp.pinpoint.collector.sampling.tail` (collector 모듈)

| 파일 | 책임 |
|---|---|
| `sampling/tail/TailSamplingProperties.java` | yml 바인딩(enable, ttl, sweep, bands) + `int rateFor(long elapsedMillis)` |
| `sampling/tail/TailSamplingProperties$Band` (정적 중첩) | `Duration maxElapsed`(nullable=catch-all), `int rate` |
| `sampling/tail/TailDecisions.java` | `static boolean keep(String txid, int ratePercent)` 결정론적 해시 |
| `sampling/tail/BufferedSpan.java` | 버퍼 엔벨로프 VO (type, header 4필드, requestTime, protoBytes) |
| `sampling/tail/BufferedSpanCodec.java` | `BufferedSpan` ↔ `byte[]` 직렬화/역직렬화 |
| `sampling/tail/ReconstructedServerHeader.java` | flush 시 `ServerHeader` 최소 복원 구현 |
| `sampling/tail/TailSamplingRepository.java` | Redis 연동(accept/decide/findStale) — Lua eval, 바이너리 안전 |
| `sampling/tail/StatisticsTraceService.java` | 마커 인터페이스(always 그룹: 100% 즉시) |
| `sampling/tail/ApplicationMapTraceService.java` | servermap을 always 그룹으로 분리한 `TraceService` |
| `sampling/tail/TailSampler.java` | 오케스트레이터: always 즉시 호출 + sampled 버퍼/결정/flush + fail-open |
| `sampling/tail/TailSamplingSweeper.java` | `@Scheduled` 만료 트레이스 기본 keep flush |
| `sampling/tail/TailSamplingConfiguration.java` | `@Configuration` — Redis 템플릿/스크립트/빈 와이어링, enable 토글 |
| `resources/redis/tail-accept.lua`, `resources/redis/tail-decide.lua` | Lua 스크립트 |

수정:
- `service/HbaseTraceService.java` — appmap 호출 제거(servermap은 `ApplicationMapTraceService`로 이동), trace+scatter만 담당(sampled 그룹).
- `heatmap/service/HeatmapService.java` — `implements StatisticsTraceService`(always 마커).
- `handler/grpc/GrpcSpanHandler.java` / `GrpcSpanChunkHandler.java` — tail 활성 시 `TailSampler`로 라우팅(기존 샘플러 우회), 비활성 시 기존 동작.
- `PinpointCollectorModule.java` — `@Import(TailSamplingConfiguration.class)`.
- `collector/src/main/resources/application.yml` — `collector.sampling.tail.*` 기본값.
- `collector/pom.xml` — spring-data-redis, lettuce-core, (test) testcontainers 의존성.

---

## Phase 1 — 설정 & 밴드 매칭 (순수 로직, Redis 무관)

### Task 1: TailSamplingProperties + 밴드 매칭

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingProperties.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingPropertiesTest.java`

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TailSamplingPropertiesTest {

    private TailSamplingProperties props() {
        TailSamplingProperties p = new TailSamplingProperties();
        TailSamplingProperties.Band b1 = new TailSamplingProperties.Band();
        b1.setMaxElapsed(Duration.ofMillis(50));
        b1.setRate(1);
        TailSamplingProperties.Band b2 = new TailSamplingProperties.Band();
        b2.setMaxElapsed(Duration.ofMillis(100));
        b2.setRate(5);
        TailSamplingProperties.Band b3 = new TailSamplingProperties.Band();
        b3.setMaxElapsed(Duration.ofMillis(500));
        b3.setRate(10);
        TailSamplingProperties.Band b4 = new TailSamplingProperties.Band(); // catch-all (maxElapsed = null)
        b4.setRate(100);
        p.setBands(List.of(b1, b2, b3, b4));
        return p;
    }

    @Test
    void rateFor_matchesFirstBandByUpperBound() {
        TailSamplingProperties p = props();
        assertThat(p.rateFor(49)).isEqualTo(1);
        assertThat(p.rateFor(50)).isEqualTo(5);   // 50 < 50 false -> next band
        assertThat(p.rateFor(99)).isEqualTo(5);
        assertThat(p.rateFor(100)).isEqualTo(10);
        assertThat(p.rateFor(499)).isEqualTo(10);
        assertThat(p.rateFor(500)).isEqualTo(100);
        assertThat(p.rateFor(5000)).isEqualTo(100);
    }

    @Test
    void rateFor_noBandMatches_defaultsToKeep100() {
        TailSamplingProperties p = new TailSamplingProperties();
        p.setBands(List.of()); // empty
        assertThat(p.rateFor(10)).isEqualTo(100); // fail-safe: keep
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingPropertiesTest`
Expected: FAIL — `TailSamplingProperties` 컴파일 불가(cannot find symbol).

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TailSamplingProperties {

    private boolean enable = false;
    private Duration bufferTtl = Duration.ofSeconds(300);
    private Duration sweepInterval = Duration.ofSeconds(5);
    private Duration decisionTtl = Duration.ofSeconds(600);
    private List<Band> bands = new ArrayList<>();

    /** elapsedMillis 가 속하는 첫 밴드의 수집률(%)을 반환. 매칭 없으면 100(keep, fail-safe). */
    public int rateFor(long elapsedMillis) {
        for (Band band : bands) {
            if (band.getMaxElapsed() == null) {
                return band.getRate(); // catch-all
            }
            if (elapsedMillis < band.getMaxElapsed().toMillis()) {
                return band.getRate();
            }
        }
        return 100;
    }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }
    public Duration getBufferTtl() { return bufferTtl; }
    public void setBufferTtl(Duration bufferTtl) { this.bufferTtl = bufferTtl; }
    public Duration getSweepInterval() { return sweepInterval; }
    public void setSweepInterval(Duration sweepInterval) { this.sweepInterval = sweepInterval; }
    public Duration getDecisionTtl() { return decisionTtl; }
    public void setDecisionTtl(Duration decisionTtl) { this.decisionTtl = decisionTtl; }
    public List<Band> getBands() { return bands; }
    public void setBands(List<Band> bands) { this.bands = bands; }

    public static class Band {
        private Duration maxElapsed; // null => catch-all (must be last)
        private int rate;            // 0..100

        public Duration getMaxElapsed() { return maxElapsed; }
        public void setMaxElapsed(Duration maxElapsed) { this.maxElapsed = maxElapsed; }
        public int getRate() { return rate; }
        public void setRate(int rate) { this.rate = rate; }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingPropertiesTest`
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingProperties.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingPropertiesTest.java
git commit -m "[#noissue] Add TailSamplingProperties with response-time band matching"
```

### Task 2: TailDecisions — 결정론적 keep 해시

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailDecisions.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailDecisionsTest.java`

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TailDecisionsTest {

    @Test
    void rate100_alwaysKeep() {
        assertThat(TailDecisions.keep("agent^1^1", 100)).isTrue();
        assertThat(TailDecisions.keep("agent^1^999999", 100)).isTrue();
    }

    @Test
    void rate0_neverKeep() {
        assertThat(TailDecisions.keep("agent^1^1", 0)).isFalse();
        assertThat(TailDecisions.keep("agent^1^999999", 0)).isFalse();
    }

    @Test
    void deterministicForSameTxid() {
        boolean first = TailDecisions.keep("agent^1^42", 10);
        for (int i = 0; i < 100; i++) {
            assertThat(TailDecisions.keep("agent^1^42", 10)).isEqualTo(first);
        }
    }

    @Test
    void approximatesRateAcrossManyTxids() {
        int kept = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            if (TailDecisions.keep("agent^1^" + i, 10)) {
                kept++;
            }
        }
        // 10% ± 2%p
        assertThat(kept).isBetween((int) (total * 0.08), (int) (total * 0.12));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=TailDecisionsTest`
Expected: FAIL — cannot find symbol `TailDecisions`.

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

public final class TailDecisions {

    private TailDecisions() {
    }

    /**
     * transactionId 기반 결정론적 keep 판정.
     * @param ratePercent 0..100
     */
    public static boolean keep(String txid, int ratePercent) {
        if (ratePercent >= 100) {
            return true;
        }
        if (ratePercent <= 0) {
            return false;
        }
        int bucket = Math.floorMod(txid.hashCode(), 100);
        return bucket < ratePercent;
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailDecisionsTest`
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailDecisions.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailDecisionsTest.java
git commit -m "[#noissue] Add deterministic keep decision for tail sampling"
```

---

## Phase 2 — 버퍼 엔벨로프 코덱 & 헤더 복원

### Task 3: ReconstructedServerHeader

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/ReconstructedServerHeader.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/ReconstructedServerHeaderTest.java`

> 바인더(`GrpcSpanBinder`)는 header에서 `getAgentId`, `getApplicationName`, `getAgentName`, `getAgentStartTime`만 사용한다. 나머지는 미사용이므로 기본값을 반환한다.

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReconstructedServerHeaderTest {

    @Test
    void exposesStoredFields() {
        ServerHeader header = new ReconstructedServerHeader("agent-1", "agent-name", "app-1", 1000L);
        assertThat(header.getAgentId()).isEqualTo("agent-1");
        assertThat(header.getAgentName()).isEqualTo("agent-name");
        assertThat(header.getApplicationName()).isEqualTo("app-1");
        assertThat(header.getAgentStartTime()).isEqualTo(1000L);
        assertThat(header.getServiceUid().get()).isEqualTo(ServiceUid.DEFAULT);
        assertThat(header.isGrpcBuiltInRetry()).isFalse();
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=ReconstructedServerHeaderTest`
Expected: FAIL — cannot find symbol.

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

/**
 * flush 시 버퍼된 span 을 재구성하기 위한 최소 ServerHeader.
 * GrpcSpanBinder 가 실제로 사용하는 4개 필드만 의미를 갖는다.
 */
public class ReconstructedServerHeader implements ServerHeader {

    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final long agentStartTime;

    public ReconstructedServerHeader(String agentId, String agentName, String applicationName, long agentStartTime) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.applicationName = applicationName;
        this.agentStartTime = agentStartTime;
    }

    @NonNull
    @Override
    public String getAgentId() { return agentId; }

    @NonNull
    @Override
    public String getAgentName() { return agentName; }

    @NonNull
    @Override
    public String getApplicationName() { return applicationName; }

    @Override
    public String getServiceName() { return null; }

    @Override
    public Supplier<ServiceUid> getServiceUid() { return () -> ServiceUid.DEFAULT; }

    @Override
    public long getAgentStartTime() { return agentStartTime; }

    @Override
    public int getServiceType() { return 0; }

    @Override
    public boolean isGrpcBuiltInRetry() { return false; }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=ReconstructedServerHeaderTest`
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/ReconstructedServerHeader.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/ReconstructedServerHeaderTest.java
git commit -m "[#noissue] Add ReconstructedServerHeader for buffered span replay"
```

### Task 4: BufferedSpan + BufferedSpanCodec

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpan.java`
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpanCodec.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpanCodecTest.java`

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BufferedSpanCodecTest {

    private final BufferedSpanCodec codec = new BufferedSpanCodec();

    @Test
    void roundTripSpan() {
        byte[] proto = new byte[]{1, 2, 3, 4, 5};
        BufferedSpan original = new BufferedSpan(BufferedSpan.Type.SPAN,
                "agent-1", "agent-name", "app-1", 1000L, 2000L, proto);

        byte[] encoded = codec.encode(original);
        BufferedSpan decoded = codec.decode(encoded);

        assertThat(decoded.type()).isEqualTo(BufferedSpan.Type.SPAN);
        assertThat(decoded.agentId()).isEqualTo("agent-1");
        assertThat(decoded.agentName()).isEqualTo("agent-name");
        assertThat(decoded.applicationName()).isEqualTo("app-1");
        assertThat(decoded.agentStartTime()).isEqualTo(1000L);
        assertThat(decoded.requestTime()).isEqualTo(2000L);
        assertThat(decoded.protoBytes()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void roundTripChunkPreservesType() {
        BufferedSpan original = new BufferedSpan(BufferedSpan.Type.SPAN_CHUNK,
                "a", "n", "app", 1L, 2L, new byte[]{9});
        BufferedSpan decoded = codec.decode(codec.encode(original));
        assertThat(decoded.type()).isEqualTo(BufferedSpan.Type.SPAN_CHUNK);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=BufferedSpanCodecTest`
Expected: FAIL — cannot find symbol.

- [ ] **Step 3: 최소 구현 (VO)**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

public record BufferedSpan(
        Type type,
        String agentId,
        String agentName,
        String applicationName,
        long agentStartTime,
        long requestTime,
        byte[] protoBytes) {

    public enum Type {
        SPAN, SPAN_CHUNK
    }
}
```

- [ ] **Step 4: 최소 구현 (코덱)**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * BufferedSpan <-> byte[] 길이-프리픽스 바이너리 직렬화. Redis 버퍼 저장용.
 */
public class BufferedSpanCodec {

    public byte[] encode(BufferedSpan span) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(span.type() == BufferedSpan.Type.SPAN ? 0 : 1);
            out.writeUTF(nullToEmpty(span.agentId()));
            out.writeUTF(nullToEmpty(span.agentName()));
            out.writeUTF(nullToEmpty(span.applicationName()));
            out.writeLong(span.agentStartTime());
            out.writeLong(span.requestTime());
            out.writeInt(span.protoBytes().length);
            out.write(span.protoBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    public BufferedSpan decode(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            BufferedSpan.Type type = in.readByte() == 0 ? BufferedSpan.Type.SPAN : BufferedSpan.Type.SPAN_CHUNK;
            String agentId = in.readUTF();
            String agentName = in.readUTF();
            String applicationName = in.readUTF();
            long agentStartTime = in.readLong();
            long requestTime = in.readLong();
            int len = in.readInt();
            byte[] proto = new byte[len];
            in.readFully(proto);
            return new BufferedSpan(type, agentId, agentName, applicationName, agentStartTime, requestTime, proto);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=BufferedSpanCodecTest`
Expected: PASS

- [ ] **Step 6: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpan.java \
        collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpanCodec.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/BufferedSpanCodecTest.java
git commit -m "[#noissue] Add BufferedSpan envelope codec for tail sampling buffer"
```

---

## Phase 3 — 의존성 & Lua 스크립트 & Redis 설정

### Task 5: collector 모듈에 Redis/Testcontainers 의존성 추가

**Files:**
- Modify: `collector/pom.xml`

- [ ] **Step 1: 의존성 추가**

`collector/pom.xml` 의 `<dependencies>` 블록에 다음을 추가한다(버전은 루트 pom `dependencyManagement` 가 관리하므로 생략).

```xml
<!-- tail sampling: redis buffer -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: 의존성 해석 확인**

Run: `./mvnw -pl collector dependency:resolve -q`
Expected: BUILD SUCCESS, 에러 없이 `spring-data-redis`, `lettuce-core`, `testcontainers` 해석.

> 만약 버전이 관리되지 않아 실패하면, 루트 `pom.xml` 의 `<dependencyManagement>` 에 이미 존재하는 좌표/버전을 확인한다(redis 모듈이 동일 의존성을 사용 중). 없으면 `spring-data-redis:3.5.3`, `io.lettuce:lettuce-core:6.5.2.RELEASE` 를 management 에 추가한다.

- [ ] **Step 3: 커밋**

```bash
git add collector/pom.xml
git commit -m "[#noissue] Add redis and testcontainers dependencies to collector"
```

### Task 6: Lua 스크립트 작성

**Files:**
- Create: `collector/src/main/resources/redis/tail-accept.lua`
- Create: `collector/src/main/resources/redis/tail-decide.lua`

> 이 단계는 리소스 파일 생성만 한다. 동작 검증은 Task 8(Repository 통합 테스트)에서 한다.

- [ ] **Step 1: tail-accept.lua 작성**

`collector/src/main/resources/redis/tail-accept.lua`:

```lua
-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- ARGV[1] = bufferedSpanBytes
-- ARGV[2] = txid (zset member)
-- ARGV[3] = firstSeenMillis
-- ARGV[4] = bufferTtlSeconds
-- returns: "keep" | "drop" | "buffered"
local d = redis.call('GET', KEYS[2])
if d then
    return d
end
redis.call('RPUSH', KEYS[1], ARGV[1])
redis.call('EXPIRE', KEYS[1], ARGV[4])
redis.call('ZADD', KEYS[3], 'NX', ARGV[3], ARGV[2])
return 'buffered'
```

- [ ] **Step 2: tail-decide.lua 작성**

`collector/src/main/resources/redis/tail-decide.lua`:

```lua
-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- ARGV[1] = "keep" | "drop"
-- ARGV[2] = txid (zset member)
-- ARGV[3] = decisionTtlSeconds
-- returns: false 이면 이미 다른 노드가 결정함(skip).
--          아니면 {decision, span1, span2, ...} (keep 일 때만 span 포함)
local ok = redis.call('SET', KEYS[2], ARGV[1], 'NX', 'EX', ARGV[3])
if not ok then
    return false
end
local spans = redis.call('LRANGE', KEYS[1], 0, -1)
redis.call('DEL', KEYS[1])
redis.call('ZREM', KEYS[3], ARGV[2])
local result = {ARGV[1]}
if ARGV[1] == 'keep' then
    for i = 1, #spans do
        result[#result + 1] = spans[i]
    end
end
return result
```

- [ ] **Step 3: 커밋**

```bash
git add collector/src/main/resources/redis/tail-accept.lua \
        collector/src/main/resources/redis/tail-decide.lua
git commit -m "[#noissue] Add Lua scripts for atomic tail sampling accept/decide"
```

### Task 7: TailSamplingRedisConfig — byte[] 템플릿 & 스크립트 빈

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRedisConfig.java`

> 이 단계는 빈 정의만 한다. enable 토글과 전체 와이어링은 Task 15 에서 한다. 검증은 Task 8.

- [ ] **Step 1: 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

@Configuration(proxyBeanMethods = false)
public class TailSamplingRedisConfig {

    @Bean("tailSamplingRedisConnectionFactory")
    public RedisConnectionFactory tailSamplingRedisConnectionFactory(TailSamplingProperties properties) {
        // 기본 host/port 는 spring.data.redis.* 를 따른다. 단순화를 위해 기본 LettuceConnectionFactory 사용.
        // (host/port 커스터마이즈가 필요하면 RedisStandaloneConfiguration 으로 확장)
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean("tailSamplingRedisTemplate")
    public RedisTemplate<String, byte[]> tailSamplingRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("tailSamplingRedisConnectionFactory")
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        return template;
    }

    @Bean("tailAcceptScript")
    public byte[] tailAcceptScript() {
        return loadScript("redis/tail-accept.lua");
    }

    @Bean("tailDecideScript")
    public byte[] tailDecideScript() {
        return loadScript("redis/tail-decide.lua");
    }

    private static byte[] loadScript(String path) {
        try {
            return StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load lua script: " + path, e);
        }
    }
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./mvnw test-compile -pl collector -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRedisConfig.java
git commit -m "[#noissue] Add redis template and lua script beans for tail sampling"
```

---

## Phase 4 — TailSamplingRepository (Redis 연동, Testcontainers 검증)

### Task 8: TailSamplingRepository + 통합 테스트

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRepository.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRepositoryIT.java`

- [ ] **Step 1: 실패 테스트 작성 (Testcontainers Redis)**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TailSamplingRepositoryIT {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.0")).withExposedPorts(6379);

    private LettuceConnectionFactory factory;
    private TailSamplingRepository repository;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        factory.afterPropertiesSet();
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();

        byte[] acceptScript = load("redis/tail-accept.lua");
        byte[] decideScript = load("redis/tail-decide.lua");
        repository = new TailSamplingRepository(template, acceptScript, decideScript, 300, 600);
        template.execute((org.springframework.data.redis.core.RedisCallback<Object>) c -> {
            c.serverCommands().flushDb();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        factory.destroy();
    }

    private static byte[] load(String path) {
        try {
            return org.springframework.util.StreamUtils.copyToByteArray(
                    new org.springframework.core.io.ClassPathResource(path).getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void accept_firstTime_returnsBuffered() {
        String r = repository.accept("tx-1", new byte[]{1}, 1000L);
        assertThat(r).isEqualTo("buffered");
    }

    @Test
    void decide_keep_returnsBufferedSpansAndClearsBuffer() {
        repository.accept("tx-2", new byte[]{1}, 1000L);
        repository.accept("tx-2", new byte[]{2}, 1000L);

        List<byte[]> spans = repository.decide("tx-2", true);

        assertThat(spans).hasSize(2);
        // 결정 후 새 span 은 keep 반환
        assertThat(repository.accept("tx-2", new byte[]{3}, 1000L)).isEqualTo("keep");
    }

    @Test
    void decide_drop_returnsEmptyAndSubsequentAcceptDrops() {
        repository.accept("tx-3", new byte[]{1}, 1000L);
        List<byte[]> spans = repository.decide("tx-3", false);
        assertThat(spans).isEmpty();
        assertThat(repository.accept("tx-3", new byte[]{9}, 1000L)).isEqualTo("drop");
    }

    @Test
    void decide_secondCall_returnsNull_noDoubleFlush() {
        repository.accept("tx-4", new byte[]{1}, 1000L);
        assertThat(repository.decide("tx-4", true)).isNotNull();
        assertThat(repository.decide("tx-4", true)).isNull(); // 이미 결정됨
    }

    @Test
    void findStale_returnsTxidsOlderThanThreshold() {
        repository.accept("tx-old", new byte[]{1}, 1000L);
        repository.accept("tx-new", new byte[]{1}, 5000L);
        List<String> stale = repository.findStale(2000L, 100);
        assertThat(stale).contains("tx-old").doesNotContain("tx-new");
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingRepositoryIT`
Expected: FAIL — cannot find symbol `TailSamplingRepository`.

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Redis 바이너리-안전 Lua eval 기반 tail sampling 저장소.
 * 키: buffer:{txid} (list), decision:{txid} (string), pending (zset).
 */
public class TailSamplingRepository {

    static final String BUFFER_PREFIX = "buffer:";
    static final String DECISION_PREFIX = "decision:";
    static final String PENDING_KEY = "pending";

    private final RedisTemplate<String, byte[]> template;
    private final byte[] acceptScript;
    private final byte[] decideScript;
    private final byte[] bufferTtlSeconds;
    private final byte[] decisionTtlSeconds;

    public TailSamplingRepository(RedisTemplate<String, byte[]> template,
                                  byte[] acceptScript, byte[] decideScript,
                                  long bufferTtlSeconds, long decisionTtlSeconds) {
        this.template = Objects.requireNonNull(template, "template");
        this.acceptScript = Objects.requireNonNull(acceptScript, "acceptScript");
        this.decideScript = Objects.requireNonNull(decideScript, "decideScript");
        this.bufferTtlSeconds = String.valueOf(bufferTtlSeconds).getBytes(StandardCharsets.UTF_8);
        this.decisionTtlSeconds = String.valueOf(decisionTtlSeconds).getBytes(StandardCharsets.UTF_8);
    }

    /** @return "keep" | "drop" | "buffered" */
    public String accept(String txid, byte[] bufferedSpanBytes, long firstSeenMillis) {
        byte[] bufferKey = key(BUFFER_PREFIX, txid);
        byte[] decisionKey = key(DECISION_PREFIX, txid);
        byte[] pendingKey = PENDING_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] txidBytes = txid.getBytes(StandardCharsets.UTF_8);
        byte[] firstSeen = String.valueOf(firstSeenMillis).getBytes(StandardCharsets.UTF_8);

        byte[] result = template.execute((RedisCallback<byte[]>) connection ->
                connection.scriptingCommands().eval(acceptScript, ReturnType.VALUE, 3,
                        bufferKey, decisionKey, pendingKey,
                        bufferedSpanBytes, txidBytes, firstSeen, bufferTtlSeconds));
        return result == null ? null : new String(result, StandardCharsets.UTF_8);
    }

    /**
     * @return null 이면 이미 다른 노드가 결정함(skip). 아니면 keep 시 버퍼된 span 바이트 목록(drop 시 빈 목록).
     */
    public List<byte[]> decide(String txid, boolean keep) {
        byte[] bufferKey = key(BUFFER_PREFIX, txid);
        byte[] decisionKey = key(DECISION_PREFIX, txid);
        byte[] pendingKey = PENDING_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] txidBytes = txid.getBytes(StandardCharsets.UTF_8);
        byte[] decisionValue = (keep ? "keep" : "drop").getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        List<byte[]> raw = template.execute((RedisCallback<List<byte[]>>) connection ->
                (List<byte[]>) (List<?>) connection.scriptingCommands().eval(decideScript, ReturnType.MULTI, 3,
                        bufferKey, decisionKey, pendingKey,
                        decisionValue, txidBytes, decisionTtlSeconds));

        if (raw == null || raw.isEmpty()) {
            return null; // SET NX 실패 = 이미 결정됨
        }
        // raw[0] = decision marker, 이후가 span 들
        List<byte[]> spans = new ArrayList<>(raw.size() - 1);
        for (int i = 1; i < raw.size(); i++) {
            spans.add(raw.get(i));
        }
        return spans;
    }

    /** firstSeen <= thresholdMillis 인 미결정 txid 목록(최대 limit). */
    public List<String> findStale(long thresholdMillis, int limit) {
        java.util.Set<byte[]> members = template.execute((RedisCallback<java.util.Set<byte[]>>) connection ->
                connection.zSetCommands().zRangeByScore(
                        PENDING_KEY.getBytes(StandardCharsets.UTF_8),
                        org.springframework.data.domain.Range.closed(0d, (double) thresholdMillis),
                        org.springframework.data.redis.connection.Limit.limit().count(limit)));
        List<String> result = new ArrayList<>();
        if (members != null) {
            for (byte[] m : members) {
                result.add(new String(m, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private static byte[] key(String prefix, String txid) {
        return (prefix + txid).getBytes(StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingRepositoryIT`
Expected: PASS (Docker 필요; 5개 테스트 통과)

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRepository.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingRepositoryIT.java
git commit -m "[#noissue] Add TailSamplingRepository with atomic Lua accept/decide/findStale"
```

---

## Phase 5 — always/sampled TraceService 분리

### Task 9: StatisticsTraceService 마커 + HeatmapService 적용

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/StatisticsTraceService.java`
- Modify: `collector/src/main/java/com/navercorp/pinpoint/collector/heatmap/service/HeatmapService.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/StatisticsTraceServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.heatmap.service.HeatmapService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StatisticsTraceServiceTest {

    @Test
    void heatmapServiceIsStatisticsTraceService() {
        assertThat(StatisticsTraceService.class)
                .isAssignableFrom(HeatmapService.class);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=StatisticsTraceServiceTest`
Expected: FAIL — cannot find symbol `StatisticsTraceService` 또는 assignable 실패.

- [ ] **Step 3: 마커 인터페이스 생성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;

/**
 * 항상 100% 즉시 처리되어야 하는 집계성 TraceService 마커.
 * tail sampling 의 버퍼/결정 게이트를 거치지 않는다. (servermap, heatmap)
 */
public interface StatisticsTraceService extends TraceService {
}
```

- [ ] **Step 4: HeatmapService 에 마커 적용**

`HeatmapService.java` 의 클래스 선언을 수정:

```java
// import 추가
import com.navercorp.pinpoint.collector.sampling.tail.StatisticsTraceService;

// 변경 전: public class HeatmapService implements TraceService {
// 변경 후:
@Service
public class HeatmapService implements StatisticsTraceService {
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=StatisticsTraceServiceTest`
Expected: PASS

- [ ] **Step 6: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/StatisticsTraceService.java \
        collector/src/main/java/com/navercorp/pinpoint/collector/heatmap/service/HeatmapService.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/StatisticsTraceServiceTest.java
git commit -m "[#noissue] Mark HeatmapService as always-on StatisticsTraceService"
```

### Task 10: ApplicationMapTraceService 분리 + HbaseTraceService 에서 appmap 제거

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/service/ApplicationMapTraceService.java`
- Modify: `collector/src/main/java/com/navercorp/pinpoint/collector/service/HbaseTraceService.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/ApplicationMapTraceServiceTest.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/service/HbaseTraceServiceTest.java`

> servermap 집계를 always 그룹으로 옮긴다. **중요:** `ApplicationMapTraceService` 는 `com.navercorp.pinpoint.collector.service` 패키지(이미 `@ComponentScan` 대상)에 두어 **tail 활성화 여부와 무관하게 항상 빈으로 등록**되게 한다. (tail OFF 시 핸들러가 모든 TraceService 를 순회하므로 servermap 이 그대로 호출되어야 한다.) `sampling.tail` 패키지는 스캔 대상이 아니므로 거기에 두면 안 된다.

- [ ] **Step 1: ApplicationMapTraceService 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.service.ApplicationMapTraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class ApplicationMapTraceServiceTest {

    @Test
    void insertSpanDelegatesToApplicationMapService() {
        ApplicationMapService delegate = Mockito.mock(ApplicationMapService.class);
        ApplicationMapTraceService service = new ApplicationMapTraceService(delegate);
        SpanBo spanBo = new SpanBo();

        service.insertSpan(spanBo);

        verify(delegate).insertSpan(spanBo);
    }

    @Test
    void insertSpanChunkDelegatesToApplicationMapService() {
        ApplicationMapService delegate = Mockito.mock(ApplicationMapService.class);
        ApplicationMapTraceService service = new ApplicationMapTraceService(delegate);
        SpanChunkBo chunkBo = new SpanChunkBo();

        service.insertSpanChunk(chunkBo);

        verify(delegate).insertSpanChunk(chunkBo);
    }

    @Test
    void isStatisticsTraceService() {
        org.assertj.core.api.Assertions.assertThat(StatisticsTraceService.class)
                .isAssignableFrom(ApplicationMapTraceService.class);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=ApplicationMapTraceServiceTest`
Expected: FAIL — cannot find symbol `ApplicationMapTraceService`.

- [ ] **Step 3: ApplicationMapTraceService 구현**

```java
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.sampling.tail.StatisticsTraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * servermap 집계를 always 그룹(100% 즉시)으로 분리한 TraceService.
 * 이전에는 HbaseTraceService 내부에서 호출되었다.
 * com.navercorp.pinpoint.collector.service 패키지(@ComponentScan 대상)에 두어
 * tail 활성화 여부와 무관하게 항상 빈으로 등록된다.
 */
@Service
public class ApplicationMapTraceService implements StatisticsTraceService {

    private final ApplicationMapService applicationMapService;

    public ApplicationMapTraceService(ApplicationMapService applicationMapService) {
        this.applicationMapService = Objects.requireNonNull(applicationMapService, "applicationMapService");
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        this.applicationMapService.insertSpan(spanBo);
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        this.applicationMapService.insertSpanChunk(spanChunkBo);
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=ApplicationMapTraceServiceTest`
Expected: PASS

- [ ] **Step 5: HbaseTraceService 에서 appmap 제거 — 실패 테스트 작성**

`HbaseTraceServiceTest.java`:

```java
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.collector.scatter.service.ScatterService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HbaseTraceServiceTest {

    @Test
    void insertSpan_doesNotCallApplicationMapService() {
        TraceDao traceDao = Mockito.mock(TraceDao.class);
        ScatterService scatterService = Mockito.mock(ScatterService.class);
        ApplicationMapService applicationMapService = Mockito.mock(ApplicationMapService.class);
        SpanStorePublisher publisher = Mockito.mock(SpanStorePublisher.class);
        when(publisher.captureContext(any(SpanBo.class))).thenReturn(Mockito.mock(SpanInsertEvent.class));
        when(traceDao.asyncInsert(any(SpanBo.class))).thenReturn(CompletableFuture.completedFuture(null));
        Executor direct = Runnable::run;

        HbaseTraceService service = new HbaseTraceService(traceDao, scatterService, applicationMapService, publisher, direct);
        SpanBo spanBo = new SpanBo();

        service.insertSpan(spanBo);

        verify(traceDao).asyncInsert(spanBo);
        verify(scatterService).insert(spanBo);
        verify(applicationMapService, never()).insertSpan(any());
    }
}
```

- [ ] **Step 6: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=HbaseTraceServiceTest`
Expected: FAIL — `applicationMapService.insertSpan` 이 호출되어 `never()` 위반.

- [ ] **Step 7: HbaseTraceService 수정 — appmap 호출 제거**

`HbaseTraceService.insertSpan` 에서 `this.applicationMapService.insertSpan(spanBo);` 줄 삭제.
`HbaseTraceService.insertSpanChunk` 에서 `this.applicationMapService.insertSpanChunk(spanChunkBo);` 줄 삭제.

> 생성자의 `applicationMapService` 파라미터와 필드는 **유지**한다(테스트 호환 및 향후 참조). 사용하지 않는 경고가 거슬리면 필드를 제거하고 생성자에서도 제거하되, 이 경우 위 테스트의 생성자 인자도 함께 수정한다. 본 계획은 필드 유지를 택한다.

수정 후 `insertSpan`:

```java
@Override
public void insertSpan(final SpanBo spanBo) {
    SpanInsertEvent event = publisher.captureContext(spanBo);

    CompletableFuture<Void> future = traceDao.asyncInsert(spanBo);

    this.scatterService.insert(spanBo);

    future.whenCompleteAsync((unused, throwable) -> {
        final boolean result = throwable == null;
        if (logger.isTraceEnabled()) {
            logger.trace("success {}", result);
        }
        publisher.publishEvent(event, result);
    }, grpcSpanServerExecutor);
}
```

수정 후 `insertSpanChunk`:

```java
@Override
public void insertSpanChunk(final SpanChunkBo spanChunkBo) {
    SpanChunkInsertEvent event = publisher.captureContext(spanChunkBo);

    this.traceDao.insertSpanChunk(spanChunkBo);

    // TODO should be able to tell whether the span chunk is successfully inserted
    publisher.publishEvent(event, true);
}
```

- [ ] **Step 8: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=HbaseTraceServiceTest,ApplicationMapTraceServiceTest`
Expected: PASS

- [ ] **Step 9: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/service/ApplicationMapTraceService.java \
        collector/src/main/java/com/navercorp/pinpoint/collector/service/HbaseTraceService.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/ApplicationMapTraceServiceTest.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/service/HbaseTraceServiceTest.java
git commit -m "[#noissue] Split servermap into always-on ApplicationMapTraceService"
```

---

## Phase 6 — TailSampler 오케스트레이터

### Task 11: TailSampler — always 즉시 + sampled 버퍼/결정/flush + fail-open

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSampler.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplerTest.java`

> 의존: `TraceService[]`(always/sampled 분류), `TailSamplingRepository`, `TailSamplingProperties`, `BufferedSpanCodec`, `GrpcSpanFactory`, `MeterRegistry`.

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TailSamplerTest {

    private StatisticsTraceService always;
    private TraceService sampled;
    private TailSamplingRepository repository;
    private GrpcSpanFactory factory;
    private TailSampler tailSampler;

    @BeforeEach
    void setUp() {
        always = Mockito.mock(StatisticsTraceService.class);
        sampled = Mockito.mock(TraceService.class);
        repository = Mockito.mock(TailSamplingRepository.class);
        factory = Mockito.mock(GrpcSpanFactory.class);
        TailSamplingProperties props = new TailSamplingProperties();
        props.setBands(List.of()); // rateFor -> 100

        tailSampler = new TailSampler(
                new TraceService[]{always, sampled},
                repository, props, new BufferedSpanCodec(), factory, new SimpleMeterRegistry());
    }

    private SpanBo rootSpan(int elapsed) {
        SpanBo bo = new SpanBo();
        bo.setTransactionId(new PinpointServerTraceId("agent", 1L, 100L));
        bo.setParentSpanId(-1L); // root
        bo.setElapsed(elapsed);
        bo.setAgentId("agent");
        bo.setApplicationName("app");
        bo.setAgentName("agentName");
        return bo;
    }

    @Test
    void alwaysGroupCalledImmediately_regardlessOfDecision() {
        when(repository.accept(anyString(), any(), anyLong())).thenReturn("buffered");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(always).insertSpan(bo);
    }

    @Test
    void decisionDrop_sampledNotWritten() {
        when(repository.accept(anyString(), any(), anyLong())).thenReturn("drop");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(sampled, never()).insertSpan(any());
        verify(always).insertSpan(bo); // always 는 항상
    }

    @Test
    void decisionKeep_writesThroughToSampledLive() {
        when(repository.accept(anyString(), any(), anyLong())).thenReturn("keep");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(sampled).insertSpan(bo);
    }

    @Test
    void rootBuffered_triggersDecide_andReplaysKeptSpans() throws Exception {
        when(repository.accept(anyString(), any(), anyLong())).thenReturn("buffered");
        // props rateFor=100 -> keep=true
        BufferedSpan buffered = new BufferedSpan(BufferedSpan.Type.SPAN,
                "agent", "agentName", "app", 1L, 2L, new byte[]{7});
        byte[] encoded = new BufferedSpanCodec().encode(buffered);
        when(repository.decide(anyString(), org.mockito.ArgumentMatchers.eq(true)))
                .thenReturn(List.of(encoded));
        SpanBo rebuilt = new SpanBo();
        when(factory.buildSpanBo(any(), any(), anyLong())).thenReturn(rebuilt);

        tailSampler.acceptSpan(rootSpan(10), new byte[]{1});

        verify(sampled).insertSpan(rebuilt);
    }

    @Test
    void redisFailure_failsOpen_writesSampledLive() {
        when(repository.accept(anyString(), any(), anyLong()))
                .thenThrow(new RuntimeException("redis down"));
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(always).insertSpan(bo);
        verify(sampled).insertSpan(bo); // fail-open
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplerTest`
Expected: FAIL — cannot find symbol `TailSampler`.

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TailSampler {

    private final Logger logger = LogManager.getLogger(getClass());

    private final TraceService[] alwaysServices;
    private final TraceService[] sampledServices;
    private final TailSamplingRepository repository;
    private final TailSamplingProperties properties;
    private final BufferedSpanCodec codec;
    private final GrpcSpanFactory spanFactory;

    private final Counter keptCounter;
    private final Counter droppedCounter;
    private final Counter bufferedCounter;
    private final Counter redisErrorCounter;

    public TailSampler(TraceService[] traceServices,
                       TailSamplingRepository repository,
                       TailSamplingProperties properties,
                       BufferedSpanCodec codec,
                       GrpcSpanFactory spanFactory,
                       MeterRegistry meterRegistry) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");

        List<TraceService> always = new ArrayList<>();
        List<TraceService> sampled = new ArrayList<>();
        for (TraceService ts : traceServices) {
            if (ts instanceof StatisticsTraceService) {
                always.add(ts);
            } else {
                sampled.add(ts);
            }
        }
        this.alwaysServices = always.toArray(new TraceService[0]);
        this.sampledServices = sampled.toArray(new TraceService[0]);

        this.keptCounter = meterRegistry.counter("collector.tail.sampling", "result", "kept");
        this.droppedCounter = meterRegistry.counter("collector.tail.sampling", "result", "dropped");
        this.bufferedCounter = meterRegistry.counter("collector.tail.sampling", "result", "buffered");
        this.redisErrorCounter = meterRegistry.counter("collector.tail.sampling", "result", "redis-error");
    }

    public void acceptSpan(SpanBo spanBo, byte[] protoBytes) {
        // always 그룹은 항상 100% 즉시 (servermap, heatmap)
        for (TraceService ts : alwaysServices) {
            ts.insertSpan(spanBo);
        }

        final String txid = spanBo.getTransactionId().toString();
        try {
            byte[] envelope = codec.encode(new BufferedSpan(BufferedSpan.Type.SPAN,
                    spanBo.getAgentId(), spanBo.getAgentName(), spanBo.getApplicationName(),
                    spanBo.getAgentStartTime(), spanBo.getCollectorAcceptTime(), protoBytes));
            String decision = repository.accept(txid, envelope, System.currentTimeMillis());

            if ("keep".equals(decision)) {
                keptCounter.increment();
                insertSampledSpanLive(spanBo);
            } else if ("drop".equals(decision)) {
                droppedCounter.increment();
                // discard
            } else { // buffered
                bufferedCounter.increment();
                if (spanBo.isRoot()) {
                    decideAndFlush(txid, spanBo.getElapsed());
                }
            }
        } catch (Exception e) {
            redisErrorCounter.increment();
            logger.warn("tail sampling redis error, fail-open write-through. txid={}", txid, e);
            insertSampledSpanLive(spanBo); // fail-open
        }
    }

    public void acceptSpanChunk(SpanChunkBo spanChunkBo, byte[] protoBytes) {
        for (TraceService ts : alwaysServices) {
            ts.insertSpanChunk(spanChunkBo);
        }

        final String txid = spanChunkBo.getTransactionId().toString();
        try {
            byte[] envelope = codec.encode(new BufferedSpan(BufferedSpan.Type.SPAN_CHUNK,
                    spanChunkBo.getAgentId(), null, spanChunkBo.getApplicationName(),
                    spanChunkBo.getAgentStartTime(), spanChunkBo.getCollectorAcceptTime(), protoBytes));
            String decision = repository.accept(txid, envelope, System.currentTimeMillis());

            if ("keep".equals(decision)) {
                insertSampledSpanChunkLive(spanChunkBo);
            } else if ("drop".equals(decision)) {
                // discard
            }
            // chunk 는 결정 트리거 아님(root 아님) -> buffered 면 대기
        } catch (Exception e) {
            redisErrorCounter.increment();
            logger.warn("tail sampling redis error (chunk), fail-open. txid={}", txid, e);
            insertSampledSpanChunkLive(spanChunkBo);
        }
    }

    private void decideAndFlush(String txid, int elapsedMillis) {
        int rate = properties.rateFor(elapsedMillis);
        boolean keep = TailDecisions.keep(txid, rate);
        List<byte[]> won = repository.decide(txid, keep);
        if (won == null) {
            return; // 다른 노드가 이미 처리
        }
        if (keep) {
            keptCounter.increment();
            replay(won);
        } else {
            droppedCounter.increment();
        }
    }

    /** 버퍼된 엔벨로프들을 재구성해 sampled 그룹에 기록. */
    void replay(List<byte[]> encodedSpans) {
        for (byte[] encoded : encodedSpans) {
            BufferedSpan buffered = codec.decode(encoded);
            ServerHeader header = new ReconstructedServerHeader(
                    buffered.agentId(), buffered.agentName(), buffered.applicationName(), buffered.agentStartTime());
            try {
                if (buffered.type() == BufferedSpan.Type.SPAN) {
                    SpanBo bo = spanFactory.buildSpanBo(PSpan.parseFrom(buffered.protoBytes()), header, buffered.requestTime());
                    insertSampledSpanLive(bo);
                } else {
                    SpanChunkBo bo = spanFactory.buildSpanChunkBo(PSpanChunk.parseFrom(buffered.protoBytes()), header, buffered.requestTime());
                    insertSampledSpanChunkLive(bo);
                }
            } catch (Exception e) {
                logger.warn("failed to replay buffered span", e);
            }
        }
    }

    private void insertSampledSpanLive(SpanBo spanBo) {
        for (TraceService ts : sampledServices) {
            ts.insertSpan(spanBo);
        }
    }

    private void insertSampledSpanChunkLive(SpanChunkBo spanChunkBo) {
        for (TraceService ts : sampledServices) {
            ts.insertSpanChunk(spanChunkBo);
        }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplerTest`
Expected: PASS (5개 통과)

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSampler.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplerTest.java
git commit -m "[#noissue] Add TailSampler orchestrator with fail-open write-through"
```

---

## Phase 7 — Sweeper

### Task 12: TailSamplingSweeper — 만료 트레이스 기본 keep flush

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingSweeper.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingSweeperTest.java`

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TailSamplingSweeperTest {

    @Test
    void sweep_forcesKeepDecisionForStaleTxids_andReplays() {
        TailSamplingRepository repository = Mockito.mock(TailSamplingRepository.class);
        TailSampler tailSampler = Mockito.mock(TailSampler.class);
        TailSamplingProperties props = new TailSamplingProperties();
        props.setBufferTtl(Duration.ofSeconds(300));

        when(repository.findStale(anyLong(), anyInt())).thenReturn(List.of("tx-stale"));
        List<byte[]> spans = List.of(new byte[]{1});
        when(repository.decide(eq("tx-stale"), eq(true))).thenReturn(spans);

        TailSamplingSweeper sweeper = new TailSamplingSweeper(repository, tailSampler, props);
        sweeper.sweep();

        verify(repository).decide("tx-stale", true); // 기본 keep
        verify(tailSampler).replay(spans);
    }

    @Test
    void sweep_skipsWhenDecideReturnsNull() {
        TailSamplingRepository repository = Mockito.mock(TailSamplingRepository.class);
        TailSampler tailSampler = Mockito.mock(TailSampler.class);
        TailSamplingProperties props = new TailSamplingProperties();

        when(repository.findStale(anyLong(), anyInt())).thenReturn(List.of("tx-x"));
        when(repository.decide(eq("tx-x"), eq(true))).thenReturn(null); // 다른 노드 선점

        TailSamplingSweeper sweeper = new TailSamplingSweeper(repository, tailSampler, props);
        sweeper.sweep();

        verify(tailSampler, Mockito.never()).replay(Mockito.any());
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingSweeperTest`
Expected: FAIL — cannot find symbol `TailSamplingSweeper`.

- [ ] **Step 3: 최소 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Objects;

/**
 * 루트가 끝내 도착하지 않은 트레이스를 buffer-ttl 경과 후 기본 keep 으로 flush.
 * 클러스터에서 다수 인스턴스가 동시에 돌아도 decide()의 SET NX 로 1회만 flush 된다.
 */
public class TailSamplingSweeper {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final int BATCH_LIMIT = 500;

    private final TailSamplingRepository repository;
    private final TailSampler tailSampler;
    private final TailSamplingProperties properties;

    public TailSamplingSweeper(TailSamplingRepository repository,
                               TailSampler tailSampler,
                               TailSamplingProperties properties) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tailSampler = Objects.requireNonNull(tailSampler, "tailSampler");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Scheduled(fixedDelayString = "${collector.sampling.tail.sweep-interval:5s}")
    public void sweep() {
        try {
            long threshold = System.currentTimeMillis() - properties.getBufferTtl().toMillis();
            List<String> stale = repository.findStale(threshold, BATCH_LIMIT);
            for (String txid : stale) {
                List<byte[]> won = repository.decide(txid, true); // 기본 keep
                if (won != null) {
                    tailSampler.replay(won);
                }
            }
            if (!stale.isEmpty() && logger.isInfoEnabled()) {
                logger.info("tail sampling sweeper flushed {} stale traces (default keep)", stale.size());
            }
        } catch (Exception e) {
            logger.warn("tail sampling sweeper error", e);
        }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingSweeperTest`
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingSweeper.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingSweeperTest.java
git commit -m "[#noissue] Add TailSamplingSweeper for orphaned-trace default-keep flush"
```

---

## Phase 8 — 와이어링 & 핸들러 통합 & 설정

### Task 13: TailSamplingConfiguration — 빈 조립 (enable 토글)

**Files:**
- Create: `collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingConfiguration.java`

> `collector.sampling.tail.enable=true` 일 때만 활성화. `TraceService[]`, `GrpcSpanFactory`, `MeterRegistry` 는 collector 컨텍스트에 이미 존재. `ApplicationMapTraceService`(service 패키지)/`HeatmapService`(HeatmapCollectorModule) 는 tail 비활성 시에도 항상 빈으로 등록되므로 이 Configuration 에서 등록하지 않는다. 이 Configuration 은 tail 전용 빈(Redis/codec/repository/sampler/sweeper)만 조립한다.

- [ ] **Step 1: 구현**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "collector.sampling.tail", name = "enable", havingValue = "true")
@EnableConfigurationProperties
@EnableScheduling
@Import(TailSamplingRedisConfig.class)
public class TailSamplingConfiguration {

    @Bean
    @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "collector.sampling.tail")
    public TailSamplingProperties tailSamplingProperties() {
        return new TailSamplingProperties();
    }

    @Bean
    public BufferedSpanCodec bufferedSpanCodec() {
        return new BufferedSpanCodec();
    }

    @Bean
    public TailSamplingRepository tailSamplingRepository(
            @Qualifier("tailSamplingRedisTemplate") RedisTemplate<String, byte[]> redisTemplate,
            @Qualifier("tailAcceptScript") byte[] acceptScript,
            @Qualifier("tailDecideScript") byte[] decideScript,
            TailSamplingProperties properties) {
        return new TailSamplingRepository(redisTemplate, acceptScript, decideScript,
                properties.getBufferTtl().toSeconds(), properties.getDecisionTtl().toSeconds());
    }

    @Bean
    public TailSampler tailSampler(TraceService[] traceServices,
                                   TailSamplingRepository repository,
                                   TailSamplingProperties properties,
                                   BufferedSpanCodec codec,
                                   GrpcSpanFactory spanFactory,
                                   MeterRegistry meterRegistry) {
        return new TailSampler(traceServices, repository, properties, codec, spanFactory, meterRegistry);
    }

    @Bean
    public TailSamplingSweeper tailSamplingSweeper(TailSamplingRepository repository,
                                                   TailSampler tailSampler,
                                                   TailSamplingProperties properties) {
        return new TailSamplingSweeper(repository, tailSampler, properties);
    }
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./mvnw test-compile -pl collector -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingConfiguration.java
git commit -m "[#noissue] Wire tail sampling beans behind enable toggle"
```

### Task 14: 핸들러 통합 — tail 활성 시 라우팅

**Files:**
- Modify: `collector/src/main/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanHandler.java`
- Modify: `collector/src/main/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanChunkHandler.java`
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanHandlerTailTest.java`

> `ObjectProvider<TailSampler>` 로 선택적 주입. tail 빈이 없으면(비활성) 기존 동작 유지. 있으면 기존 per-span 샘플러 우회 후 `TailSampler` 로 라우팅.

- [ ] **Step 1: 실패 테스트 작성**

```java
package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.sampler.SpanSamplerFactory;
import com.navercorp.pinpoint.collector.sampler.TrueSampler;
import com.navercorp.pinpoint.collector.sampling.tail.TailSampler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcSpanHandlerTailTest {

    @Test
    void whenTailEnabled_routesToTailSampler_bypassingTraceServiceLoop() {
        TraceService traceService = Mockito.mock(TraceService.class);
        GrpcSpanFactory factory = Mockito.mock(GrpcSpanFactory.class);
        SpanSamplerFactory samplerFactory = Mockito.mock(SpanSamplerFactory.class);
        when(samplerFactory.createBasicSpanSampler()).thenReturn(TrueSampler.instance());
        TailSampler tailSampler = Mockito.mock(TailSampler.class);

        @SuppressWarnings("unchecked")
        ObjectProvider<TailSampler> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(tailSampler);

        SpanBo spanBo = new SpanBo();
        when(factory.buildSpanBo(any(), any(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(spanBo);

        @SuppressWarnings("unchecked")
        ServerRequest<PSpan> request = Mockito.mock(ServerRequest.class);
        when(request.getData()).thenReturn(PSpan.getDefaultInstance());
        when(request.getHeader()).thenReturn(Mockito.mock(ServerHeader.class));
        when(request.getRequestTime()).thenReturn(123L);

        GrpcSpanHandler handler = new GrpcSpanHandler(
                new TraceService[]{traceService}, factory, samplerFactory, provider);
        handler.handleSimple(request);

        verify(tailSampler).acceptSpan(org.mockito.ArgumentMatchers.eq(spanBo), any(byte[].class));
        verify(traceService, never()).insertSpan(any()); // 기존 루프 우회
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./mvnw test -pl collector -Dtest=GrpcSpanHandlerTailTest`
Expected: FAIL — `GrpcSpanHandler` 생성자에 `ObjectProvider<TailSampler>` 인자 없음.

- [ ] **Step 3: GrpcSpanHandler 수정**

import 추가:

```java
import com.navercorp.pinpoint.collector.sampling.tail.TailSampler;
import org.springframework.beans.factory.ObjectProvider;
```

필드/생성자 수정:

```java
private final TailSampler tailSampler; // nullable

public GrpcSpanHandler(TraceService[] traceServices, GrpcSpanFactory spanFactory,
                       SpanSamplerFactory spanSamplerFactory,
                       ObjectProvider<TailSampler> tailSamplerProvider) {
    this.traceServices = Objects.requireNonNull(traceServices, "traceServices");
    this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
    this.sampler = spanSamplerFactory.createBasicSpanSampler();
    this.tailSampler = tailSamplerProvider.getIfAvailable();

    logger.info("TraceServices {}, tailSampling={}", Arrays.toString(traceServices), tailSampler != null);
}
```

`handleSpan` 의 SpanBo 빌드 직후 분기 추가:

```java
private void handleSpan(PSpan span, ServerHeader serverHeader, long requestTime) {
    if (isDebug) {
        logger.debug("Handle {} {}", serverHeader, createSimpleSpanLog(span));
    }

    final SpanBo spanBo = spanFactory.buildSpanBo(span, serverHeader, requestTime);

    // tail sampling: 활성 시 기존 per-span 샘플러를 우회하고 TailSampler 로 라우팅
    if (tailSampler != null) {
        try {
            tailSampler.acceptSpan(spanBo, span.toByteArray());
        } catch (Throwable e) {
            logger.warn("Failed to tail-sample Span {} {}", serverHeader, MessageFormatUtils.debugLog(span), e);
        }
        return;
    }

    if (!sampler.isSampling(spanBo)) {
        if (isDebug) {
            logger.debug("Unsampled {} {}", serverHeader, createSimpleSpanLog(span));
        } else {
            infoLog.log(() -> {
                if (logger.isInfoEnabled()) {
                    logger.info("Unsampled {} {}", serverHeader, createSimpleSpanLog(span));
                }
            });
        }
        return;
    }
    for (TraceService traceService : traceServices) {
        try {
            traceService.insertSpan(spanBo);
        } catch (RequestNotPermittedException notPermitted) {
            warnLog.log((c) -> logger.warn("Failed to handle Span {} RequestNotPermitted:{} {}", serverHeader, notPermitted.getMessage(), c));
        } catch (Throwable e) {
            logger.warn("Failed to handle {} {}", serverHeader, MessageFormatUtils.debugLog(span), e);
        }
    }
}
```

- [ ] **Step 4: GrpcSpanChunkHandler 동일 패턴 수정**

import 추가(`TailSampler`, `ObjectProvider`). 생성자에 `ObjectProvider<TailSampler> tailSamplerProvider` 추가하고 `this.tailSampler = tailSamplerProvider.getIfAvailable();`. `handleSpanChunk` 의 `spanChunkBo` 빌드 직후:

```java
final SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(spanChunk, header, requestTime);

if (tailSampler != null) {
    try {
        tailSampler.acceptSpanChunk(spanChunkBo, spanChunk.toByteArray());
    } catch (Throwable e) {
        logger.warn("Failed to tail-sample SpanChunk {} {}", header, MessageFormatUtils.debugLog(spanChunk), e);
    }
    return;
}
// ... 기존 sampler/loop 유지
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=GrpcSpanHandlerTailTest`
Expected: PASS

> 기존 핸들러 테스트가 생성자 시그니처 변경으로 깨질 수 있다. 깨지면 해당 테스트의 `new GrpcSpanHandler(...)` 호출에 4번째 인자로 빈 provider 를 추가한다:
> `org.springframework.beans.factory.support.StaticListableBeanFactory` 대신 간단히 mock: `Mockito.mock(ObjectProvider.class)` 에 `getIfAvailable()` → null 스텁.

- [ ] **Step 6: 전체 collector 컴파일/기존 핸들러 테스트 회귀 확인**

Run: `./mvnw test -pl collector -Dtest=GrpcSpanHandler*`
Expected: PASS (기존 테스트 포함)

- [ ] **Step 7: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanHandler.java \
        collector/src/main/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanChunkHandler.java \
        collector/src/test/java/com/navercorp/pinpoint/collector/handler/grpc/GrpcSpanHandlerTailTest.java
git commit -m "[#noissue] Route span handlers through TailSampler when enabled"
```

### Task 15: PinpointCollectorModule 등록 + application.yml 기본값

**Files:**
- Modify: `collector/src/main/java/com/navercorp/pinpoint/collector/PinpointCollectorModule.java`
- Modify: `collector/src/main/resources/application.yml`

- [ ] **Step 1: 모듈 @Import 추가**

`PinpointCollectorModule.java` 의 `@Import({...})` 목록에 `TailSamplingConfiguration.class` 추가하고 import 문 추가:

```java
import com.navercorp.pinpoint.collector.sampling.tail.TailSamplingConfiguration;
```

`@Import` 배열 마지막에 추가:

```java
        CollectorEventConfiguration.class,
        TailSamplingConfiguration.class
```

- [ ] **Step 2: application.yml 기본 설정 추가**

`collector/src/main/resources/application.yml` 최상위에 다음 블록 추가:

```yaml
collector:
  sampling:
    tail:
      enable: false            # 기본 비활성 (활성화 시 Redis 필요)
      buffer-ttl: 300s
      sweep-interval: 5s
      decision-ttl: 600s
      bands:
        - max-elapsed: 50ms
          rate: 1
        - max-elapsed: 100ms
          rate: 5
        - max-elapsed: 500ms
          rate: 10
        - rate: 100            # catch-all (>= 500ms)
```

> Redis 접속 정보는 표준 `spring.data.redis.host`/`spring.data.redis.port` 를 사용한다(Task 7 의 `LettuceConnectionFactory` 기본값이 이를 따른다). 운영 환경에서 활성화 시 해당 프로퍼티를 설정한다.

- [ ] **Step 3: 컨텍스트 로딩 회귀 확인 (tail 비활성 기본값)**

Run: `./mvnw test -pl collector -q`
Expected: 전체 collector 테스트 PASS. tail 비활성이므로 기존 동작 불변.

- [ ] **Step 4: 커밋**

```bash
git add collector/src/main/java/com/navercorp/pinpoint/collector/PinpointCollectorModule.java \
        collector/src/main/resources/application.yml
git commit -m "[#noissue] Register tail sampling module and default config"
```

### Task 16: 엔드투엔드 통합 테스트 (Testcontainers Redis)

**Files:**
- Test: `collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingEndToEndIT.java`

> `TailSampler` + 실제 `TailSamplingRepository`(Redis 컨테이너) + mock TraceService 로 핵심 시나리오를 검증한다.

- [ ] **Step 1: 통합 테스트 작성**

```java
package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Testcontainers
class TailSamplingEndToEndIT {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.0")).withExposedPorts(6379);

    private LettuceConnectionFactory factory;
    private TraceService always;
    private TraceService sampled;
    private TailSampler tailSampler;
    private TailSamplingRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        factory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        factory.afterPropertiesSet();
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        template.execute((RedisCallback<Object>) c -> { c.serverCommands().flushDb(); return null; });

        byte[] acceptScript = StreamUtils.copyToByteArray(new ClassPathResource("redis/tail-accept.lua").getInputStream());
        byte[] decideScript = StreamUtils.copyToByteArray(new ClassPathResource("redis/tail-decide.lua").getInputStream());
        repository = new TailSamplingRepository(template, acceptScript, decideScript, 300, 600);

        always = Mockito.mock(StatisticsTraceService.class);
        sampled = Mockito.mock(TraceService.class);
        GrpcSpanFactory spanFactory = Mockito.mock(GrpcSpanFactory.class);
        Mockito.when(spanFactory.buildSpanBo(any(), any(), Mockito.anyLong())).thenReturn(new SpanBo());

        TailSamplingProperties props = new TailSamplingProperties();
        TailSamplingProperties.Band fast = new TailSamplingProperties.Band();
        fast.setMaxElapsed(java.time.Duration.ofMillis(50));
        fast.setRate(0); // 빠른 트레이스 0% (테스트 결정론)
        TailSamplingProperties.Band slow = new TailSamplingProperties.Band();
        slow.setRate(100); // catch-all keep
        props.setBands(List.of(fast, slow));

        tailSampler = new TailSampler(new TraceService[]{always, sampled},
                repository, props, new BufferedSpanCodec(), spanFactory, new SimpleMeterRegistry());
    }

    @AfterEach
    void tearDown() {
        factory.destroy();
    }

    private SpanBo span(long seq, int elapsed, boolean root) {
        SpanBo bo = new SpanBo();
        bo.setTransactionId(new PinpointServerTraceId("agent", 1L, seq));
        bo.setParentSpanId(root ? -1L : 10L);
        bo.setElapsed(elapsed);
        bo.setAgentId("agent");
        bo.setApplicationName("app");
        bo.setAgentName("agentName");
        return bo;
    }

    @Test
    void slowTrace_childBeforeRoot_allKept() {
        // 자식 먼저(버퍼), 루트 나중(elapsed 500 -> 100% keep)
        tailSampler.acceptSpan(span(1, 0, false), new byte[]{1});
        tailSampler.acceptSpan(span(1, 500, true), new byte[]{2});
        // sampled 그룹에 2건 기록 (replay) — always 는 매번
        verify(sampled, Mockito.atLeast(1)).insertSpan(any());
    }

    @Test
    void fastTrace_dropped() {
        // 루트 먼저, elapsed 10 -> band rate 0 -> drop
        tailSampler.acceptSpan(span(2, 10, true), new byte[]{1});
        // 이후 자식 도착 -> decision=drop -> 즉시 폐기
        tailSampler.acceptSpan(span(2, 0, false), new byte[]{2});
        verify(sampled, never()).insertSpan(any());
        // always 는 항상 호출
        verify(always, Mockito.times(2)).insertSpan(any());
    }
}
```

- [ ] **Step 2: 테스트 통과 확인**

Run: `./mvnw test -pl collector -Dtest=TailSamplingEndToEndIT`
Expected: PASS (Docker 필요)

- [ ] **Step 3: 커밋**

```bash
git add collector/src/test/java/com/navercorp/pinpoint/collector/sampling/tail/TailSamplingEndToEndIT.java
git commit -m "[#noissue] Add end-to-end tail sampling integration test"
```

### Task 17: 전체 빌드 & 회귀 검증

- [ ] **Step 1: collector 전체 테스트**

Run: `./mvnw test -pl collector`
Expected: BUILD SUCCESS (신규 + 기존 테스트 모두 통과)

- [ ] **Step 2: collector 패키징(테스트 스킵) 으로 와이어링 점검**

Run: `./mvnw install -pl collector -am -Dmaven.test.skip=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: 최종 확인**

tail 비활성(기본) 상태에서 컨텍스트가 정상 기동하고, 기존 동작이 보존되는지 확인(Step 1 의 컨텍스트 로딩 테스트로 충족). 활성화 시나리오는 Task 8/16 통합 테스트가 커버.

---

## 향후 작업 (이 계획 범위 밖, spec §14)

- EVALSHA 캐싱으로 Lua eval 최적화(현재는 매 호출 eval; Redis 가 SHA 캐시).
- `decision-ttl` 실측 기반 튜닝(비동기/MQ 최대 지연).
- Redis 메모리/엔벨로프 압축 측정.
- 늦은-span(decision-ttl 만료 후) stray 조각 모니터링 대시보드.
- accept 시 `applicationMapService`(always) 가 Redis 와 무관하게 항상 1회 실행됨을 메트릭으로 확인.
