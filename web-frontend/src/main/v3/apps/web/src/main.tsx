import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { Provider as JotaiProvider } from 'jotai';
import { IconContext } from 'react-icons';
// import { ReactToastContainer, queryClient } from '@pinpoint-fe/ui';
import { ReactToastContainer } from '@pinpoint-fe/ui/components';
import { queryClient } from '@pinpoint-fe/ui/hooks';
import router from './routes';
import { I18nextProvider } from 'react-i18next';
import i18n from './i18n';
import { QueryClientProvider } from '@tanstack/react-query';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.Fragment>
    <I18nextProvider i18n={i18n}>
      <QueryClientProvider client={queryClient}>
        <JotaiProvider>
          <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
            <RouterProvider router={router} />
            <ReactToastContainer />
          </IconContext.Provider>
        </JotaiProvider>
      </QueryClientProvider>
    </I18nextProvider>
  </React.Fragment>,
);
