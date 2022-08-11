import '@pinpoint-fe/ui/dist/index.css';

import { ReactElement, ReactNode, useEffect } from 'react'
import { NextPage } from 'next';
import Script from 'next/script'
import type { AppContext, AppProps } from 'next/app'
import App from 'next/app';
import { IconContext } from 'react-icons';

// import { SessionProvider } from "next-auth/react"

// import { RootThemeProvider } from '@pinpoint-fe/ui/styles/theme';
// import themeLight from '@pinpoint-fe/ui/styles/theme-light';
import { SWRConfig } from 'swr';
import { GlobalStyle } from '@pinpoint-fe/ui';

export type NextPageWithLayout<P = {}, IP = P> = NextPage<P,IP> & {
  getLayout?: (page: ReactElement) => ReactNode
}

type AppPropsWithLayout<P = {}> = AppProps<P> & {
  Component: NextPageWithLayout<P>
}

const AppRoot =  ({ Component, pageProps: { session, pageProps} }: AppPropsWithLayout) => {
  const getLayout = Component.getLayout ?? ((page) => page);
  // const themeColor = themeLight;

  return (
    <>
      {GlobalStyle}
      <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
        {/* <SessionProvider session={session}> */}
        {/* <RootThemeProvider theme={themeLight}> */}
          <SWRConfig value={{
            suspense: true,
            fetcher: (...args: any) => fetch(args as any).then(res => res.json())
          }}>
            <div className="content">
              {getLayout(<Component {...pageProps} />)}
            </div>
          </SWRConfig>
        {/* </RootThemeProvider> */}
        {/* </SessionProvider> */}
      </IconContext.Provider>
    </>
  )
}

// AppRoot.getInitialProps = async (appContext: AppContext) => {
//   const { router } = appContext;
//   const locale = router.locale!; // 'ko' or 'en'
//   const appProps = await App.getInitialProps(appContext);
//   global.__localeId__ = locale;
  
//   return { ...appProps };
// };

export default AppRoot
