import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { Provider as JotaiProvider, getDefaultStore } from 'jotai';
import { IconContext } from 'react-icons';
// import { ReactToastContainer, queryClient } from '@pinpoint-fe/ui';
import { ReactToastContainer } from '@pinpoint-fe/ui/src/components';
import { queryClient, installServiceNameFetchInterceptor } from '@pinpoint-fe/ui/src/hooks';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import router from './routes';
import { I18nextProvider } from 'react-i18next';
import i18n from './i18n';
import { QueryClientProvider } from '@tanstack/react-query';

// 인터셉터(getDefaultStore)와 React 트리(JotaiProvider)가 동일한 store를 공유해야
// configurationAtom/selectedServiceAtom 값을 인터셉터가 읽을 수 있다.
const jotaiStore = getDefaultStore();

// configuration의 experimental.enableServiceMap이 켜져 있을 때, 모든 /api 요청 헤더에
// 현재 선택된 service를 주입한다. 렌더링/최초 fetch 이전에 한 번 설치해야 한다.
// configuration은 부트스트랩 이후 비동기로 로드되므로, 매 요청 시 store에서 최신값을
// 읽도록 getter를 주입한다.
installServiceNameFetchInterceptor(() => jotaiStore.get(configurationAtom));

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.Fragment>
    <I18nextProvider i18n={i18n}>
      <QueryClientProvider client={queryClient}>
        <JotaiProvider store={jotaiStore}>
          <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
            <React.Suspense fallback={null}>
              <RouterProvider router={router} />
            </React.Suspense>
            <ReactToastContainer />
          </IconContext.Provider>
        </JotaiProvider>
      </QueryClientProvider>
    </I18nextProvider>
  </React.Fragment>,
);
