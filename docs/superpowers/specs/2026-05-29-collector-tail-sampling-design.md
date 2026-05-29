# 컬렉터 응답시간 기반 Tail Sampling 설계

- **작성일:** 2026-05-29
- **대상 컴포넌트:** `collector` / `collector-starter`
- **버전 컨텍스트:** v3.1.0 (히트맵 도입 이후)

## 1. 개요 & 목표

컬렉터에서 트랜잭션의 **응답시간(루트 span의 `elapsed`)에 따라 트레이스 상세 저장 비율을 차등 적용**하는 tail-based sampling을 추가한다.

응답시간 밴드별 수집률(기본값, 설정 가능):

| 응답시간 | 수집률 |
|---|---|
| `< 50ms` | 1% |
| `50ms ≤ t < 100ms` | 5% |
| `100ms ≤ t < 500ms` | 10% |
| `≥ 500ms` | 100% |

### 우선순위
1. **(최우선) 느린 트레이스 100% 보존** — 결정 전에는 어떤 span도 버리지 않는다. 장애·예외 경로에서도 유실 0을 보장한다(fail-open, 타임아웃 기본 keep).
2. **(부차) 쓰기/저장 부하 절감** — 빠른 트레이스를 결정 시점에 폐기하여 trace 테이블 write·저장량을 줄인다.

### 비목표 (YAGNI)
- 에이전트 측 샘플링 변경 없음(상위·독립).
- servermap·heatmap 등 집계 통계의 정확도는 **건드리지 않는다**(항상 100%).
- 동적/엔드포인트별 정책, ML 기반 샘플링 등은 범위 밖.

## 2. 배경 & 제약 (현재 구조)

- span 수신 경로: `SpanService`(gRPC) → `GrpcSpanHandler.handleSimple` → (line 84) `Sampler<BasicSpan>` per-span 게이트 → `traceService.insertSpan(spanBo)` 루프.
- `TraceService[]` 루프에는 최소 두 구현이 등록됨:
  - `HbaseTraceService.insertSpan` — 내부에서 (1) `traceDao.asyncInsert` (trace 테이블), (2) `scatterService.insert` (scatter), (3) `applicationMapService.insertSpan` (servermap) 를 모두 수행.
  - `HeatmapService.insertSpan` — 원본 `elapsed`를 200ms 버킷으로 Kafka→Pinot(`heatmapStatApp`)에 COUNT 집계.
- 응답시간은 **루트 span(`SpanBo.isRoot()` = `parentSpanId == -1`)의 `elapsed`** 에만 존재. 자식 span / `SpanChunk`에는 전체 응답시간 없음.
- 컬렉터는 span을 **도착 즉시** HBase에 기록(버퍼링·트레이스 완료 신호 없음).
- 분산 트레이스의 모든 span은 동일 `transactionId`(`agentId^agentStartTime^sequence`)를 공유하나, 에이전트별로 **다른 컬렉터 인스턴스**(클러스터)로 흩어질 수 있음.
- 기존 per-span 샘플러는 `transactionSequence` 해시 기반(head sampling)이라 조율 없이 일관 결정이 가능하지만, **응답시간은 transactionId로부터 유도 불가** → 공유 상태(Redis)가 필요.

→ 따라서 본 기능은 본질적으로 **tail-based**이며, 결정 전까지 span을 모아두는 **중앙 버퍼(Redis)** 방식을 채택한다.

## 3. 핵심 설계 결정 요약

| # | 결정 | 근거 |
|---|---|---|
| D1 | **중앙 Redis 버퍼** 방식(안 1) | 상태를 한 곳에 모아 운영·추론 단순화. Redis 성능으로 처리량 감내 가능 판단. |
| D2 | 결정 트리거 = **루트 span 도착** 시 `elapsed`로 밴드 판정 | 루트만 전체 응답시간 보유. |
| D3 | keep 여부 = `hash(transactionId) % 100 < 밴드 rate` (결정 1회 계산 후 Redis에 저장) | 기존 `PercentRateSampler` 방식 재활용, 1회 계산으로 클러스터 일관성 확보. |
| D4 | 루트 미도착 시 **buffer-ttl 경과 후 기본 keep** (sweeper가 flush) | 우선순위 1(유실 0) 보장. |
| D5 | 샘플링 적용 범위 = **C안**: `trace + scatter`만 샘플링, `servermap + heatmap`은 100% 즉시 | 집계 통계 왜곡 방지. 특히 히트맵은 응답시간 분포 뷰라 왜곡 시 치명적. |
| D6 | `accept`/`decide`를 **Lua 스크립트로 원자화** | check-then-buffer / set-then-flush 경합 제거. |
| D7 | Redis 장애 시 **fail-open**(전량 즉시 HBase 기록) | 우선순위 1 보장. |
| D8 | tail 샘플링 ON 시 기존 per-span `Sampler<BasicSpan>` 게이트 **우회** | 사전 드롭은 트레이스 완전성·집계 100%를 깨뜨림. |

## 4. 설정 (`application.yml`)

```yaml
collector:
  sampling:
    tail:
      enable: true
      buffer-ttl: 300s          # 루트 미도착 시 강제 flush까지 대기(기본 keep)
      sweep-interval: 5s        # sweeper 주기
      decision-ttl: 600s        # 결정 레코드 보관(늦은 span 조회용; buffer-ttl보다 충분히 길게,
                                #   현실적 비동기/MQ 지연 상한보다 길게 설정)
      # 응답시간 밴드 — 위에서부터 매칭, 첫 매칭 적용
      bands:
        - max-elapsed: 50ms     # t < 50ms
          rate: 1               # 1%
        - max-elapsed: 100ms    # 50ms ≤ t < 100ms
          rate: 5
        - max-elapsed: 500ms    # 100ms ≤ t < 500ms
          rate: 10
        - max-elapsed: -1       # t ≥ 500ms (상한 없음)
          rate: 100
  # Redis 연결 설정(Lettuce) — host/port/password/timeout 등
  # (CollectorApp 의 Redis auto-config exclude 해제 또는 전용 클라이언트 구성)
```

- `@ConfigurationProperties("collector.sampling.tail")` → `TailSamplingProperties`.
- 밴드는 정렬된 리스트. 매칭: `elapsed < max-elapsed`인 **첫** 밴드의 `rate` 적용. `max-elapsed: -1`은 상한 없음(마지막 fallthrough).
- 모든 값(비율·경계·TTL)은 재배포 없이 yml만 수정하면 됨.

## 5. 아키텍처 & 컴포넌트

신규/수정 컴포넌트:

- **`TailSamplingProperties`** (신규) — yml 바인딩, 밴드 매처 제공(`int rateFor(int elapsed)`).
- **`TailSamplingBuffer`** (신규) — 핵심 오케스트레이터. `accept(spanBo, protoBytes)` / `accept(spanChunkBo, protoBytes)` 진입. Redis 연동, 결정·flush 수행.
- **Redis 클라이언트(Lettuce)** (신규 설정) — 컬렉터는 현재 Redis auto-config를 `exclude` 중이므로 전용 빈/설정 추가.
- **Lua 스크립트 2종** (신규) — `accept`, `decide` 원자 실행.
- **`TailSamplingSweeper`** (신규) — `sweep-interval` 주기 스케줄러. 오래된 미결정 트레이스를 기본 keep flush.
- **TraceService 분류** (수정) — `always`(100% 즉시) 그룹과 `sampled`(버퍼링) 그룹으로 구분.
- **`HbaseTraceService`** (수정) — 내부 3작업을 분리 호출 가능하도록 리팩터: `servermap`(즉시) vs `trace+scatter`(지연).
- **`GrpcSpanHandler` / `GrpcSpanChunkHandler`** (수정) — tail ON 시 기존 per-span 샘플러 우회, `TailSamplingBuffer.accept(...)`로 라우팅.

### 샘플링 적용 범위 (D5, C안)

| 그룹 | 작업 | 처리 시점 |
|---|---|---|
| **always (100%)** | `HeatmapService` (heatmap), `applicationMapService` (servermap) | 매 span `accept` 시 즉시 1회 |
| **sampled (버퍼링)** | trace 테이블 write(`traceDao`), `scatterService` (scatter) | 버퍼 적재 후 keep 결정 시에만 flush |

- 히트맵은 이미 독립 `TraceService`라 "버퍼로 보내지 않는다"는 분류만으로 100% 유지.
- servermap은 `HbaseTraceService` 내부에 묶여 있으므로 즉시 그룹으로 분리.
- **이중집계 방지:** `always` 작업은 첫 `accept`에서만 실행하고 flush 시 **재실행하지 않는다**. flush(및 늦은 span write-through)는 `sampled`(trace+scatter)만 replay.

## 6. 데이터 흐름

```
span 도착 (GrpcSpanHandler / GrpcSpanChunkHandler)
  └─ (tail ON) 기존 per-span 샘플러 우회
     └─ TailSamplingBuffer.accept(bo, protoBytes)
        ├─ [always] applicationMapService + HeatmapService 즉시 호출 (100%, 1회)
        └─ [sampled] 결정 상태에 따라 분기 (아래 §8)
```

flush 시 `sampled` replay 경로는 **기존 검증된 경로 재사용**: 버퍼의 원본 protobuf 바이트 → 기존 팩토리로 `SpanBo`/`SpanChunkBo` 재생성 → `traceDao.asyncInsert` + `scatterService.insert`.

## 7. Redis 데이터 모델

```
buffer:{txid}    → LIST. 해당 트레이스의 모든 sampled-대상 span/chunk 원본 protobuf 바이트.
                   루트/자식/SpanChunk 구분 없이 RPUSH로 append.
pending          → ZSET. member=txid, score=firstSeen(ms). sweeper 탐색용.
decision:{txid}  → STRING. "keep" | "drop". 1회 기록, TTL=decision-ttl.
```

- `buffer:{txid}`에는 **always 그룹은 들어가지 않음**(즉시 처리되므로). 오직 trace+scatter replay용 원본만.
- 키 메모리 상한 ≈ (평균 트레이스 지속시간 + 여유) × 유입속도. buffer-ttl은 루트 유실 최악 케이스 상한.

## 8. 생명주기

### 8.1 accept(bo, protoBytes) — 모든 span 공통
1. `applicationMapService` + `HeatmapService` 즉시 호출 (always, 1회).
2. Lua(accept) 원자 실행:
   - `d = GET decision:{txid}`
   - `d == "keep"` → 즉시 trace+scatter write-through (버퍼링 안 함)
   - `d == "drop"` → trace+scatter 폐기
   - `d == null` (미결정):
     - `RPUSH buffer:{txid} protoBytes`
     - `ZADD pending txid firstSeen` (신규일 때만)
     - `EXPIRE buffer:{txid} buffer-ttl` (누수 방지 안전망)
3. (accept 밖) `bo.isRoot()`이면 → `decide(txid, bo.elapsed)` 호출.

> `SpanChunk`는 `elapsed`/root 개념이 없어 결정 트리거가 되지 않으며 항상 자식으로 버퍼링된다.

### 8.2 decide(txid, elapsed) — 결정 & flush (Lua 원자)
1. `rate = properties.rateFor(elapsed)`; `keep = (hash(txid) % 100 < rate)`.
2. `SET decision:{txid} (keep?"keep":"drop") NX EX decision-ttl`
   - 성공(선점):
     - `spans = LRANGE buffer:{txid} 0 -1`
     - keep이면 spans 전부 trace+scatter replay, drop이면 아무것도 안 씀
     - `DEL buffer:{txid}`; `ZREM pending txid`
   - 실패(다른 컬렉터/sweeper가 이미 처리) → skip.

### 8.3 케이스별 동작
- **정상(루트 ~1초 내 도착):** 자식들 버퍼 적재 → 루트 도착 시 decide → flush.
- **루트 먼저, 자식 늦게(비동기/MQ/reorder):** decide가 먼저 끝나 `decision` 존재 → 늦은 자식은 accept에서 즉시 write-through(keep)/폐기(drop).
- **루트 미도착(크래시·유실·미계측):** sweeper가 `pending`에서 `firstSeen` 기준 age > buffer-ttl인 txid 발견 → `decide`를 **기본 keep** 으로 강제(밴드 rate=100 동등) → flush.
- **아주 늦은 span(decision-ttl 만료 후):** `decision` 없음 → 신규처럼 재버퍼링 → sweeper가 기본 keep. 원결정 keep이면 일관, drop이면 stray 조각 잔존(경미). → `decision-ttl`을 현실적 비동기/MQ 지연 상한보다 충분히 길게 설정해 회피.

## 9. 동시성 (클러스터)

- 컬렉터 다중 인스턴스 + sweeper 다중 실행 환경.
- `decide`의 `SET ... NX`로 **단일 인스턴스만 flush** 선점. 루트 도착 결정과 sweeper가 경합해도 NX로 1회만 수행.
- `accept`/`decide`를 각각 **Lua 스크립트**로 묶어 "결정 조회+RPUSH", "결정 SET+flush 대상 확정"의 경합 창을 제거.

## 10. 안정성 (fail-open)

- Redis 연결 실패/타임아웃 등 예외 시 → **샘플링 우회, 전량 즉시 HBase 기록**(현행 동작과 동일). 샘플링은 일시 중단되나 유실 0(우선순위 1) 보장.
- always 그룹(servermap/heatmap)은 Redis와 무관하게 항상 수행되므로 Redis 장애의 영향 없음.

## 11. 관측성 / 메트릭

Micrometer 카운터/게이지로 노출:
- `tail.sampling.buffered` (적재 span 수)
- `tail.sampling.kept` / `tail.sampling.dropped` (결정 결과)
- `tail.sampling.flush.timeout` (sweeper 기본 keep flush 수)
- `tail.sampling.redis.error` (fail-open 발동 수)
- 밴드별 분포 카운터(밴드 rate 튜닝·B 검증용)
- (게이지) `pending` ZSET 크기, `buffer:*` 추정 메모리

## 12. 테스트 전략

테스트 스택: JUnit 5, Mockito, AssertJ, TestContainers(Redis).

- **단위:**
  - 밴드 매칭 경계값(49/50/99/100/499/500ms), `max-elapsed: -1` fallthrough.
  - keep 해시 결정론(`hash(txid)%100 < rate`).
  - always/sampled 분류·이중집계 방지(servermap·heatmap 1회, trace+scatter는 keep시에만).
- **통합(TestContainers Redis):**
  - 루트-먼저 / 자식-먼저(비동기) 순서 모두에서 일관 keep/drop.
  - 루트 유실 → sweeper 기본 keep flush.
  - 늦은 span write-through(keep)/폐기(drop).
  - 클러스터 NX 중복 flush 방지(decide 동시 호출 시 1회만 flush).
  - Lua accept/decide 원자성(경합 시나리오).
  - Redis 장애 → fail-open(전량 기록).
- **회귀:** tail OFF 시 기존 동작 불변.

## 13. 영향받는/신규 파일 (예상)

신규:
- `.../collector/sampling/tail/TailSamplingProperties.java`
- `.../collector/sampling/tail/TailSamplingBuffer.java`
- `.../collector/sampling/tail/TailSamplingSweeper.java`
- `.../collector/sampling/tail/RedisTailSamplingConfig.java` (Lettuce 빈/스크립트 로드)
- Lua 리소스: `accept.lua`, `decide.lua`

수정:
- `.../collector/service/HbaseTraceService.java` — servermap(즉시) vs trace+scatter(지연) 분리.
- `.../collector/handler/grpc/GrpcSpanHandler.java` / `GrpcSpanChunkHandler.java` — tail ON 시 기존 샘플러 우회 + 버퍼 라우팅.
- `.../collector/CollectorApp.java` — Redis(Lettuce) 활성화/설정.
- `collector` 프로파일 `application.yml` — `collector.sampling.tail.*`.
- `HeatmapService` 등록부 확인(always 그룹 분류).

## 14. 미해결 / 추후 고려

- `decision-ttl`(기본 600s)의 적정값은 실제 비동기/MQ 최대 지연 측정 후 조정.
- `buffer-ttl` 300s는 루트 유실 트레이스의 UI 가시성 지연 상한(정상 트레이스 무관). 길다고 판단되면 하향.
- 버퍼 원본을 protobuf 바이트로 저장(직렬화 재사용) — 압축 적용 여부는 메모리 측정 후 결정.
- Redis 메모리 eviction 정책: 버퍼 키는 명시 TTL로 보호하되 maxmemory 정책과의 상호작용 검토.
