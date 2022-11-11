import '@pinpoint-fe/ui/dist/index.css';

import { ReactElement, ReactNode } from 'react'
import { NextPage } from 'next';
import type { AppContext, AppProps } from 'next/app'
import App from 'next/app';
import { IconContext } from 'react-icons';
// import { RootThemeProvider } from '@pinpoint-fe/ui/styles/theme';
// import themeLight from '@pinpoint-fe/ui/styles/theme-light';
import { SWRConfig } from 'swr';
import { GlobalStyle } from '@pinpoint-fe/ui';
import { Provider as JotaiProvider } from 'jotai';
import { getInitialAtoms } from '../atoms/getInitialAtoms';
import Head from 'next/head';

export type NextPageWithLayout<P = {}, IP = P> = NextPage<P, IP> & {
  getLayout?: (page: ReactElement) => ReactNode
}

type AppPropsWithLayout<P = {}> = AppProps<P> & {
  Component: NextPageWithLayout<P>
}

const AppRoot = ({ Component, pageProps }: AppPropsWithLayout) => {
  const getLayout = Component.getLayout ?? ((page) => page);
  // const initialAtom = getInitialAtoms(pageProps);
  // const themeColor = themeLight;
  return (
    <>
      <Head>
        <title>PINPOINT</title>
        <meta httpEquiv="content-type" content="text/html;charset=utf-8" />
        <meta name="author" content="Naver" />
        <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi" />
        <meta httpEquiv="X-UA-Compatible" content="IE=edge,chrome=1" />
      </Head>
      {GlobalStyle}
      <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
        {/* <SessionProvider session={session}> */}
        {/* <RootThemeProvider theme={themeLight}> */}
        <SWRConfig value={{
          suspense: true,
          revalidateOnFocus: false,
          fetcher: (url, params) => {
            const queryParamString = new URLSearchParams(params)?.toString();
            const urlWithQueryParams = queryParamString ? `${url}?${queryParamString}` : url;

            return fetch(`${urlWithQueryParams}`).then(res => res.json());
          }
        }}>
          {/* <JotaiProvider initialValues={initialAtom}> */}
            <div className="content">
              {getLayout(<Component {...pageProps} />)}
            </div>
          {/* </JotaiProvider> */}
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
