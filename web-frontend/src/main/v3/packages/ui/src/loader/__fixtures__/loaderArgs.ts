import type { LoaderFunctionArgs } from 'react-router';

/**
 * 로더 테스트가 넘기는 인자 스텁.
 *
 * 로더는 전부 `params`와 `request`만 destructure한다. react-router v7.18이
 * `LoaderFunctionArgs`에 더한 `url`·`pattern`은 런타임이 채워 주는 값이라 어느
 * 로더도 읽지 않으므로, 여기서는 형태만 맞춘다.
 *
 * 파싱되지 않는 url도 그대로 받는다. 로더가 잘못된 url을 어떻게 처리하는지 확인하는
 * 테스트가 일부러 그런 값을 넘기기 때문이다 — 그 경우 로더가 `request.url`로 직접
 * `new URL`을 해서 던지는 것이 확인 대상이다.
 */
export const makeArgs = (url: string, params: Record<string, string> = {}) => {
  let parsedUrl: URL | undefined;

  try {
    parsedUrl = new URL(url);
  } catch {
    // 위 주석 참고. 스텁이 대신 던지면 정작 로더가 실행되지 않는다.
  }

  return {
    params,
    request: { url } as Request,
    url: parsedUrl,
    pattern: '',
    context: {},
  } as unknown as LoaderFunctionArgs;
};
