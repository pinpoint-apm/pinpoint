import { useAtomValue } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

/**
 * `configurationAtom`에 담긴 현재 configuration을 읽는다.
 *
 * `Configuration`을 확장한 저장소(naver 등)는 타입 인자로 자기 타입을 넘겨,
 * 추가 필드까지 정확한 타입으로 읽는다.
 *
 * ```ts
 * // ui 패키지 내부 — 공용 필드만 쓰므로 타입 인자 없이
 * const configuration = useConfiguration();
 *
 * // 확장 저장소의 apps/web — 래퍼 훅을 하나 두고 호출부는 타입 인자 없이 쓴다
 * export const useConfiguration = () => useBaseConfiguration<NaverConfiguration>();
 * ```
 *
 * 타입 단언은 이 한 줄에만 둔다. atom을 넓히거나 호출부마다
 * `Configuration & Record<string, unknown>`으로 캐스팅하면 확장 필드가 `unknown`/`any`가 되어
 * 기존 필드의 오타 검출까지 함께 잃는다.
 *
 * configuration은 부트스트랩 이후 비동기로 로드되므로 채워지기 전에는 `undefined`다.
 * 페이지 서브트리는 `InitialFetchOutlet`이 atom이 채워질 때까지 렌더를 막지만,
 * 그 바깥(사이드 네비게이션 등)에서는 `undefined`를 그대로 만날 수 있다.
 */
export const useConfiguration = <T extends Configuration = Configuration>() =>
  useAtomValue(configurationAtom) as T | undefined;
