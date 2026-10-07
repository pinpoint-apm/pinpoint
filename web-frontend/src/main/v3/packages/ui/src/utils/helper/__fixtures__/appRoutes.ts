import type { RouteObject } from 'react-router';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

/**
 * 테스트가 등록하는 앱 라우트(`registerAppRoutes`). `jest.setupAfterEnv.cjs`가 매 테스트 전에 넣는다.
 *
 * 이 패키지의 테스트는 실제 라우터(`apps/web/src/routes`)를 불러올 수 없어서(패키지 의존 방향이
 * 반대다) **경로 모양만** 옮겨 둔다. serviceName을 읽는지는 경로의 `:serviceName` 유무로만 정해지므로
 * element·loader는 필요 없다. 실제 라우트에 serviceName 세그먼트를 추가·제거하면 여기도 맞춰야
 * 테스트가 실제 동작을 확인한다.
 *
 * 앱처럼 레이아웃 라우트 아래에 '/'로 시작하는 자식 경로를 둔다 — 경로를 이어 붙이는 규칙도
 * 실제와 같은 모양으로 확인하기 위해서다.
 */
export const TEST_APP_ROUTES: RouteObject[] = [
  { path: '/' },
  { path: '/main', children: [{ path: '' }, { path: ':application/:period/:endTime' }] },
  {
    children: [
      { path: APP_PATH.API_CHECK },
      {
        children: [
          { path: `${APP_PATH.SERVER_MAP}/:application?` },
          { path: `${APP_PATH.SERVER_MAP_REALTIME}/:application?` },
          { path: `${APP_PATH.SERVICE_MAP_REALTIME}/:serviceName?/:application?` },
          { path: `${APP_PATH.SERVICE_MAP}/:serviceName?/:application?` },
          { path: `${APP_PATH.FILTERED_MAP}/:serviceName?/:application?` },
          { path: `${APP_PATH.SCATTER_FULL_SCREEN}/:serviceName?/:application?` },
          { path: `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/:serviceName?/:application?` },
          { path: `${APP_PATH.HEATMAP_FULL_SCREEN}/:serviceName?/:application?` },
          { path: `${APP_PATH.HEATMAP_FULL_SCREEN_REALTIME}/:serviceName?/:application?` },
          { path: `${APP_PATH.ERROR_ANALYSIS}/:serviceName?/:application?` },
          { path: `${APP_PATH.URL_STATISTIC}/:serviceName?/:application?` },
          { path: `${APP_PATH.SYSTEM_METRIC}/:serviceName?/:hostGroup?` },
          { path: `${APP_PATH.TRANSACTION_LIST}/:serviceName?/:application?` },
          { path: `${APP_PATH.TRANSACTION_DETAIL}/:serviceName?/:application?` },
          { path: `${APP_PATH.INSPECTOR}/:serviceName?/:application?` },
          { path: `${APP_PATH.THREAD_DUMP}/:serviceName?/:application?` },
          { path: `${APP_PATH.OPEN_TELEMETRY_METRIC}/:serviceName?/:application?` },
          {
            children: [
              { path: `${APP_PATH.CONFIG_ALARM}/:serviceName?` },
              { path: APP_PATH.CONFIG_GENERAL },
            ],
          },
          { path: '*' },
        ],
      },
    ],
  },
];
