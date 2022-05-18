import { ReactElement, ReactNode, useEffect } from 'react'
import { NextPage } from 'next';
import type { AppContext, AppProps } from 'next/app'
import Script from 'next/script'
// import { IconContext } from "react-icons";
import App from 'next/app';
// import { SessionProvider } from "next-auth/react"

import { GlobalStyle } from '@pinpoint-fe/common/components/Styled/GlobalStyle';
// import { AppHeader } from '@/components/AppHeader/AppHeader';

export type NextPageWithLayout<P = {}, IP = P> = NextPage<P,IP> & {
  getLayout?: (page: ReactElement) => ReactNode
}

type AppPropsWithLayout<P = {}> = AppProps<P> & {
  Component: NextPageWithLayout<P>
}

const AppRoot =  ({ Component, pageProps: { session, pageProps} }: AppPropsWithLayout) => {
  const getLayout = Component.getLayout ?? ((page) => page);

  return (
    <>
      {GlobalStyle}
      {/* <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}> */}
        {/* <SessionProvider session={session}> */}
          <div className="content">
            <Component {...pageProps} />
            {/* {getLayout(<Component {...pageProps} />)} */}
          </div>
        {/* </SessionProvider> */}
      {/* </IconContext.Provider> */}
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
