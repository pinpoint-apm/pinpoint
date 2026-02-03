# billboard.js → echarts 차트 마이그레이션 AI 작업 프롬프트 (초안)

아래 지시를 따라 이 프로젝트의 **billboard.js** 기반 차트를 **Apache ECharts**로 변환해 주세요.

---

## 1. 작업 목표

- `@billboard.js/react` 및 `billboard.js`를 사용하는 모든 차트 컴포넌트와 관련 훅을 **echarts**로 교체한다.
- 기존 **시각·동작·데이터 구조**는 유지한다. (차트 타입, 색상, 축 포맷, 툴팁, 범례(legend), 빈 데이터 메시지 등)
- 프로젝트에 이미 사용 중인 **echarts 패턴**(`LoadChart`, `ResponseSummaryChart`, `HeatmapChart` 등)을 참고하여 일관된 방식으로 구현한다.
- 변환 결과는 **기능과 스타일이 기존 billboard.js 차트와 완전히 동일**해야 한다.

### 1.1 문서 참조 (필수)

- **billboard.js** 공식 문서(`@billboard.js`)와 **ECharts** 공식 문서(`@ECharts`)를 반드시 참고하여 변환한다.
- **Cursor에 해당 문서가 등록되어 있지 않은 경우**에는 아래 URL을 직접 참고한다.
  - **billboard.js**: https://naver.github.io/billboard.js/release/latest/doc/
  - **ECharts**: https://echarts.apache.org/en/index.html
- 두 문서를 대조하여, 축/툴팁/색상/애니메이션/빈 데이터 처리 등 **동작과 시각 표현이 원본과 완전히 동일**하도록 ECharts 옵션을 구성한다.

---

## 2. 프로젝트 컨텍스트

- **스택**: React 18 + TypeScript + Vite
- **위치**: `packages/ui` 패키지 내 차트 및 `packages/ui/src/lib/charts` 훅
- **echarts 버전**: `echarts@^6.0.0` (이미 의존성에 포함됨)
- **스타일**: Tailwind CSS, `cn()` 유틸 사용. echarts는 **tree-shaking**으로 필요한 모듈만 `echarts.use([...])` 하여 등록한다.

---

## 3. 어떤 파일을 변환할지 (사용자 지정)

- **변환 대상 파일 목록은 두지 않는다.** 사용자가 파일 이름을 직접 말할 때만 해당 파일을 변환한다.
- 사용자가 예를 들어 **「`UrlStatTotalCountChart.tsx`를 전환해줘.」**라고 명시적으로 요청하기 전까지, 그 파일을 echarts로 변환하면 **안 된다**.
- 다른 파일도 마찬가지로, 사용자가 해당 파일을 전환해 달라고 말할 때만 변환한다.

---

## 4. 변환 시 필수 절차 (Temp 백업 후 변환)

어떤 파일을 echarts로 변환할 때는 **반드시** 아래 순서를 따른다.

1. **원본 백업 생성**  
   변환 대상 파일(예: `UrlStatTotalCountChart.tsx`)과 **같은 디렉터리**에 복사본을 만든다.
   - **파일 이름**: 원본 이름에서 확장자 앞에 `Temp`를 붙인다.  
     예: `UrlStatTotalCountChart.tsx` → `UrlStatTotalCountChartTemp.tsx` (같은 폴더에 생성)
   - **export 이름**: 복사본 파일 안에서 export된 **컴포넌트 이름**과 그에 대응하는 **Props 타입 이름** 뒤에도 `Temp`를 붙인다. (원본과 구분되어 동시에 import 가능하도록)  
     예: `UrlStatTotalCountChart` → `UrlStatTotalCountChartTemp`, `UrlStatTotalCountChartProps` → `UrlStatTotalCountChartTempProps`

2. **원본만 echarts로 변환**  
   `*Temp.tsx`는 건드리지 않고, **원본 파일**(`UrlStatTotalCountChart.tsx`)만 billboard.js 제거 후 echarts로 변환한다.

3. **Temp 파일의 용도**  
   `*Temp.tsx`는 나중에 삭제할 예정이지만, **echarts 변환이 제대로 됐는지 비교·검증할 때** 사용한다. 변환 작업 중에는 수정하거나 삭제하지 않는다.

**요약**: 사용자가 「`UrlStatTotalCountChart.tsx`를 전환해줘.」라고 하면 → 같은 자리에 `UrlStatTotalCountChartTemp.tsx`를 만들고, 원본 내용을 복사한 뒤 **export된 컴포넌트·Props 이름에 `Temp`를 붙여** 저장하고 → 그 다음 **원본** `UrlStatTotalCountChart.tsx`만 echarts로 변환한다.

---

## 5. 참고할 기존 echarts 구현 패턴 (변환 시 참고)

반드시 아래 파일들의 패턴을 따르세요.

- **라인/시계열**: `packages/ui/src/components/Chart/Load/LoadChart.tsx`
  - `echarts.init(ref)`, `ResizeObserver`로 `resize`, `dispose` 정리
  - `useEffect`로 인스턴스 생성/해제, 별도 `useEffect`로 `setOption` (데이터/옵션 의존)
  - xAxis `type: 'time'`, yAxis 포맷·툴팁 커스텀
- **막대(bar)**: `packages/ui/src/components/Chart/ResponseSummary/ResponseSummaryChart.tsx`
  - BarChart + GridComponent, 필요 시 AxisBreak
  - `chartRef` + `chartInstanceRef` 사용
- **히트맵**: `packages/ui/src/components/Heatmap/core/HeatmapChart.tsx`
  - echarts core/charts/components/renderers 조합 및 `echarts.use([...])`

공통 규칙:

- **ref**: `chartRef = useRef<HTMLDivElement>(null)`, `chartInstanceRef = useRef<echarts.EChartsType | null>(null)`
- **초기화**: 한 번만 수행하는 `useEffect`에서 `echarts.init(chartRef.current)`, `ResizeObserver` 등록, cleanup에서 `resizeObserver.disconnect()` 및 `chart.dispose()`
- **데이터/옵션 반영**: 데이터·옵션이 바뀌는 `useEffect`에서 `chartInstanceRef.current?.setOption(...)` 호출
- **빈 데이터**: `graphic`으로 중앙에 텍스트 표시 (LoadChart의 `emptyMessage` 패턴)
- **tree-shaking**: 상단에서 `import * as echarts from 'echarts/core'` 및 필요한 차트/컴포넌트/렌더러만 import 후 `echarts.use([...])` 한 번 호출

---

## 6. 변환 시 유지할 사항

- **Props 인터페이스**: 기존 컴포넌트의 `*Props` (예: `data`, `className`, `emptyMessage` 등)는 그대로 두고, 내부 구현만 echarts로 바꾼다. 호출부(`*Fetcher`, `*Chart.tsx` 사용처) 시그니처 변경은 최소화한다.
- **데이터 형식**: API/훅에서 내려오는 `timestamp`, `metricValueGroups`, `metricValues`, `fieldName`, `valueList` 등 기존 타입과 구조를 그대로 사용한다. billboard용 `columns` 형태는 컴포넌트 내부에서 echarts용 `series`/`xAxis.data` 등으로만 변환한다.
- **카테고리·축 순서**: 가로 막대(rotated bar) 등에서 **카테고리 표시 순서**가 원본과 동일해야 한다. ECharts category 축은 기본적으로 첫 항목이 아래에 오므로, billboard `axis.rotated: true`와 맞추려면 `yAxis.inverse: true`로 첫 항목이 위에 오도록 설정한다.
- **유틸**: `formatNewLinedDateString`, `abbreviateNumber`, `getFormat`, `colors` 등 기존 유틸·상수는 그대로 사용한다.
- **스타일**: `cn('w-full h-full', className)` 등 레이아웃 클래스와 `packages/ui` 디자인 토큰을 유지한다.
- **데이터 라벨(label) 스타일**: 기존 billboard.js 차트에서 데이터 라벨(값 텍스트)의 색상·폰트 등이 어떻게 적용되어 있는지 확인하고, **반드시 동일한 스타일**로 맞춘다. 예: 원본에서 라벨 색상이 막대 색상과 동일했다면 ECharts에서도 `series[].label.color`를 해당 막대 색과 동일하게 설정한다. (항상 “막대와 동일”이 아니라, **원본과 동일**하게 적용한다.)
- **그리드·데이터 겹침 순서**: 축 그리드 라인(splitLine)이 데이터 시리즈 **앞에** 와야 하는 경우, ECharts의 `zlevel`을 사용한다. `series`에 `zlevel: 0`, `xAxis`/`yAxis`에 `zlevel: 1`을 주면 축·그리드가 나중에 그려져 데이터 위에 표시된다.
- **범례(legend)**: billboard.js는 다중 시리즈일 때 기본으로 범례를 표시한다. 원본에 범례가 있는 경우(또는 다중 시리즈 차트인 경우) ECharts에서도 `LegendComponent`를 `echarts.use([...])`에 포함하고, `legend` 옵션(`data`, `bottom`, `icon`, `itemWidth`, `itemHeight`, `itemGap` 등)을 설정하여 **원본과 동일한 위치·스타일**로 표시한다. `grid.bottom`을 넉넉히 두어 범례와 겹치지 않도록 한다.

---

## 7. 파일별 변환 시 참고할 작업 순서 (사용자가 지정한 파일에 적용)

사용자가 특정 파일을 전환해 달라고 요청했을 때, 그 파일 유형에 맞게 아래를 참고한다.

1. **공통**: 먼저 §4에 따라 같은 위치에 `*Temp.tsx` 백업을 만들고, 원본만 echarts로 변환한다.
2. **시계열 라인/막대**: LoadChart, ResponseSummaryChart 패턴을 참고해 echarts로 구현한다.
3. **동적 타입 차트**(예: ChartCore): `chartOptions`에서 타입/툴팁 정보를 받아 echarts `series[].type`, `smooth`, `areaStyle` 등으로 매핑한다.
4. **가로 막대**: ResponseSummaryChart와 유사하게 echarts bar + grid를 쓰고, `xAxis`(value)/`yAxis`(category) 방향을 맞춘다. billboard.js의 `axis.rotated: true`와 카테고리 순서를 동일하게 하려면 ECharts의 **category 축에 `inverse: true`**를 설정해, 첫 번째 카테고리가 위에 오도록 한다. **x축 그리드 라인(splitLine)이 데이터 막대 앞에** 오게 하려면 `xAxis`/`yAxis`에 `zlevel: 1`, `series`에 `zlevel: 0`을 설정한다.
5. **훅/테스트**: 사용자가 훅·테스트 파일 전환을 요청한 경우, billboard 타입을 제거하고 echarts/공통 타입으로 바꾼 뒤, 테스트는 echarts 옵션·데이터 구조에 맞게 수정한다.
6. **의존성 정리**: 사용자가 “billboard 의존성 제거” 등을 요청할 때만, 변환된 파일에서 billboard import를 제거하고 `packages/ui/package.json`에서 `billboard.js`, `@billboard.js/react`를 제거한다.

---

## 8. 체크리스트 (해당 파일 변환 후 확인)

- [ ] **billboard.js** 문서(`@billboard.js`)와 **ECharts** 문서(`@ECharts`)를 참고하여 기능·스타일이 원본과 완전히 동일한지 확인
- [ ] 변환한 파일에서만 billboard.js / @billboard.js/react import 제거 (의존성 제거는 사용자 요청 시에만)
- [ ] 변환된 차트가 기존과 동일한 데이터 소스·props를 사용하는지 확인
- [ ] 가로 막대(rotated bar)인 경우, 카테고리 순서(Avg/Max 등)가 원본과 동일한지 확인(필요 시 `yAxis.inverse: true`)
- [ ] x축 그리드 라인이 데이터 막대 앞에 오는 등, 원본과 동일한 겹침 순서인지 확인(필요 시 `zlevel` 사용)
- [ ] 데이터 라벨(label) 색상·폰트 등이 원본 billboard.js 차트와 동일한지 확인
- [ ] 시계열 축 포맷(`formatNewLinedDateString` 등), 숫자 축 포맷(`abbreviateNumber`, `getFormat`) 유지
- [ ] 툴팁 내용·연동(linked) 동작이 기존과 동일한지 확인
- [ ] 원본에 범례(legend)가 있는 경우, ECharts에서도 LegendComponent 및 legend 옵션으로 동일하게 표시하는지 확인
- [ ] 빈 데이터 시 `emptyMessage` 표시
- [ ] ResizeObserver + dispose로 리소스 정리
- [ ] echarts는 필요한 모듈만 `echarts.use([...])`로 등록
- [ ] `useChartType` / `useChartConfig` / `useDataSourceChartConfig` 테스트 및 관련 컴포넌트 테스트 통과
- [ ] ESLint/TypeScript 빌드 에러 없음

---

## 9. 주의사항

- **any 최소화**: 툴팁 `formatter` 등에서 echarts 타입을 모를 경우 `echarts` 네임스페이스의 타입을 참고해 구체적인 타입을 지정한다.
- **번들 크기**: echarts는 사용하는 차트/컴포넌트만 import하고 `echarts.use([...])`에만 등록해 tree-shaking이 되도록 유지한다.
- **접근성**: 필요 시 `aria-label` 등은 컨테이너 div에 유지하거나 echarts 옵션에서 지원하는 접근성 설정을 검토한다.

---

이 프롬프트를 AI에게 준 뒤, **변환할 파일을 하나씩 지정**해서 요청한다.  
예: 「`UrlStatTotalCountChart.tsx`를 전환해줘.」  
AI는 §3에 따라 사용자가 파일 이름을 지정할 때만 해당 파일을 변환하고, §4에 따라 먼저 `*Temp.tsx` 백업을 만든 다음 원본만 echarts로 변환한다.
