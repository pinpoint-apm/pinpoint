import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { Provider as JotaiProvider } from 'jotai';
import { IconContext } from 'react-icons';
import { SidebarProvider, ToastContainer, defaultToastContainerProps } from '@pinpoint-fe/ui';
import router from './routes';
import { I18nextProvider } from 'react-i18next';
import i18n from './i18n';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@pinpoint-fe/hooks';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.Fragment>
    <I18nextProvider i18n={i18n}>
      <QueryClientProvider client={queryClient}>
        <JotaiProvider>
          <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
            <SidebarProvider>
              <RouterProvider router={router} />
              <ToastContainer className="text-sm" {...defaultToastContainerProps} />
            </SidebarProvider>
          </IconContext.Provider>
        </JotaiProvider>
      </QueryClientProvider>
    </I18nextProvider>
  </React.Fragment>,
);
