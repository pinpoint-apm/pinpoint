import { render, waitFor } from '@testing-library/react';
import { HighLightCode } from './HighLightCode';

// 배포로 청크 이름이 바뀐 뒤 예전 탭에서 열면 highlight.js 청크를 받지 못한다. 그 상황을 흉내 낸다.
// 모듈 하나를 실패로 바꾸므로 정상 동작을 보는 HighLightCode.test.tsx 와 파일을 나눈다.
jest.mock('./hljs', () => {
  throw new Error('Failed to fetch dynamically imported module');
});

const XSS_CODE = '<img src="x" onerror="window.__xss = true">';

describe('HighLightCode when highlight.js fails to load', () => {
  let consoleError: jest.SpyInstance;

  beforeEach(() => {
    consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleError.mockRestore();
  });

  test('should show the escaped code when highlight.js fails to load', async () => {
    const { container } = render(<HighLightCode language="java" code={XSS_CODE} />);

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Highlight Error', expect.any(Error));
    });
    expect(container.querySelector('img')).toBeNull();
    expect(container.textContent).toContain(XSS_CODE);
  });
});
