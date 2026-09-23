import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { isUnderPage } from './application';

/**
 * 403이 페이지 전체를 덮지 **않는** 화면. (이슈 #10744)
 *
 * 규칙은 "한 API라도 403이면 그 페이지는 권한 없음"이고, 기본이 그것이다. 여기 적힌 화면만
 * 예외로 남는다 — 덮으면 **되돌릴 방법이 화면에서 사라지는** 화면들이다.
 *
 * 이 표는 "덮지 말아야 할 화면"만 정한다. 실제로 덮이려면 그 화면이 안내를 그려야 하고
 * (`useIsForbiddenPath` → `forbiddenNoticeMountCountAtom`), 그리지 않는 화면은 여기 없어도 덮이지 않는다.
 *
 * ### map 계열이 예외인 이유
 *
 * map API에는 권한 검사가 아예 없다(`MapController`, `ServerMapHistogramController`). 권한 없는
 * 노드도 403이 아니라 `isAuthorized: false` + `serviceType: 'UNAUTHORIZED'`로 **200에 실려 온다.**
 * 화면은 그 노드를 그린 채 우측 패널만 권한 안내로 바꾼다(`ServerMapChartBoard`).
 *
 * 즉 map에서 403이 나는 것은 언제나 **고른 노드**를 묻는 조회이지 map 자체가 아니다. 그것으로
 * 화면을 덮으면 map이 사라져 다른 노드를 고를 방법이 없어진다 — 조회가 막힌 것을 알리려다
 * 볼 수 있는 것까지 못 보게 된다.
 *
 * `/serverMap/realtime`·`/serviceMap/realtime`은 각각 `/serverMap`·`/serviceMap`의 하위 경로라
 * 따로 적지 않아도 함께 걸린다.
 */
const FORBIDDEN_EXCLUDED_PAGES: string[] = [
  APP_PATH.SERVER_MAP,
  APP_PATH.SERVICE_MAP,
  APP_PATH.FILTERED_MAP,
];

/**
 * 이 경로에서 403을 받으면 페이지 전체를 권한 없음으로 볼 것인지.
 *
 * 화면만 보고 정한다 — 어떤 엔드포인트였는지는 따지지 않는다. 한 화면이 부르는 API의 권한은
 * 기능별로 갈리지만(`hasInspectorPermission` / `hasExceptionTracePermission` /
 * `hasUrlStatPermission`), 그중 하나라도 막혔다면 그 화면은 온전하지 않다는 것이 이 이슈의 규칙이다.
 *
 * 확대 화면(`/scatterFullScreenMode` 등)은 `/serverMap`의 하위 경로가 아니므로 예외가 아니다.
 * 차트 하나가 곧 페이지라 막히면 그릴 것이 남지 않는다.
 */
export const coversPageOnForbidden = (pathname = '') =>
  !FORBIDDEN_EXCLUDED_PAGES.some((page) => isUnderPage(pathname, page));
