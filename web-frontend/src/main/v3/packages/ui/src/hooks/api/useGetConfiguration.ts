import { useQuery } from '@tanstack/react-query';
import { getDefaultStore } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { queryClient, queryFn } from './reactQueryHelper';

/**
 * configuration은 웹이 뜬 뒤 바뀌지 않는 서버 설정이므로 캐시 엔트리를 하나만 두고 공유한다.
 * (`serviceScopedQueryKeyHashFn`의 service 무관 엔드포인트라 service를 바꿔도 같은 키다.)
 */
const CONFIGURATION_QUERY_KEY = [END_POINTS.CONFIGURATION];

export const useGetConfiguration = <T>() => {
  const { data, error, isLoading, refetch } = useQuery<T>({
    queryKey: CONFIGURATION_QUERY_KEY,
    queryFn: queryFn(END_POINTS.CONFIGURATION),
    // 한 번 받은 값을 계속 쓴다. 페이지를 옮기며 이 훅이 remount 되어도 재요청하지 않는다.
    // (실패한 쿼리는 data가 없으므로 다음 mount에서 정상적으로 다시 시도된다.)
    staleTime: Infinity,
    gcTime: Infinity,
  });
  return { data, error, isLoading, refetch };
};

/**
 * 렌더 밖(라우트 로더 등)에서 configuration을 읽는 경로.
 *
 * 라우트 로더는 페이지에 진입할 때마다, 그리고 날짜 파라미터를 정규화하며 redirect 할 때마다
 * 다시 실행된다. 그래서 이 함수가 매번 fetch를 하면 페이지 이동마다 `/api/configuration`이
 * 재요청된다. `useGetConfiguration`과 같은 queryKey로 공유 캐시에 넣어 두어, 웹 접근 후
 * 최초 1회만 실제 요청이 나가고 이후에는 캐시된 값을 그대로 반환한다.
 *
 * 백엔드가 죽었을 때 로더가 기본값으로 진행하는 기존 동작을 유지하려면 재시도 없이 즉시
 * 실패해야 하므로 `retry: false`를 준다. 같은 이유로 `ignoreGlobalError`를 켜서 로더 단계의
 * 실패는 조용히 넘긴다. 이 실패는 로더가 catch 해 기본값으로 진행하는, 사용자에게 알릴 필요가
 * 없는 경로다. 사용자에게 보이는 에러 토스트는 (변경 전과 동일하게) 화면에서 실제로
 * configuration을 필요로 하는 `useGetConfiguration` 쪽 실패에서만 발생한다.
 * (`meta`는 fetch 단위 옵션이라 훅의 재요청에는 이 설정이 묻지 않는다.)
 *
 * **읽어 온 값은 `configurationAtom`에도 넣는다.** 렌더 밖에서 설정을 읽는 곳들
 * (`getEnableServiceMap` → `getRequestService` → 요청 헤더·캐시 키·라우트 로더)은 그 아톰을
 * 보는데, 아톰을 채우는 것은 화면(`InitialFetchOutlet`)이고 **라우트 로더는 화면보다 먼저
 * 돈다.** 그래서 로더가 이 함수로 설정을 제대로 읽어 놓고도 아톰은 비어 있는 구간이 생기고,
 * 그 구간에서는 servicemap이 서버 설정으로 켜져 있어도 `getEnableServiceMap`이 "꺼짐"으로
 * 읽는다. `getRequestService`가 undefined가 되어, 첫 진입이 골라 둔 service가 아니라 DEFAULT로
 * 떨어졌다(`/` → `/serverMap` → `/serviceMap/DEFAULT`, 이어서 경로를 따라간 전역 선택값까지
 * DEFAULT로 덮였다).
 *
 * 설정의 저장소를 하나로 유지해 그 구간을 없앤다. 화면이 나중에 같은 값을 다시 넣지만, jotai는
 * 값이 같으면(`ensureQueryData`가 같은 캐시 객체를 돌려준다) 알림을 건너뛰므로 리렌더는 늘지
 * 않는다. 여기서 채우면 **앞으로 추가될 로더도 따로 챙길 것이 없다** — 로더마다 configuration을
 * 넘기게 하면 한 곳만 빠뜨려도 그 화면만 조용히 DEFAULT로 조회된다.
 */
export const getConfiguration = async <T extends Configuration>(): Promise<T> => {
  const configuration = await queryClient.ensureQueryData<T>({
    queryKey: CONFIGURATION_QUERY_KEY,
    queryFn: queryFn(END_POINTS.CONFIGURATION),
    // 로더가 먼저 캐시를 만드는 경로에서도 gcTime을 함께 지정해야 한다. 로더만 이 값을 읽는
    // 구간(훅이 마운트되기 전, 또는 훅 없이 로더만 도는 경로)에는 관찰자가 없어서, 기본
    // gcTime(5분)이면 캐시가 수거되고 다음 페이지 진입에서 다시 요청이 나간다.
    staleTime: Infinity,
    gcTime: Infinity,
    retry: false,
    meta: { ignoreGlobalError: true },
  });

  getDefaultStore().set(configurationAtom, configuration);

  return configuration;
};
