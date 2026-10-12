import { render, waitFor } from '@testing-library/react';
import { HighLightCode } from './HighLightCode';

// 결과를 dangerouslySetInnerHTML 로 넣으므로, 코드 안의 태그가 요소로 만들어지면 그대로 실행될 수 있다.
const XSS_CODE = '<img src="x" onerror="window.__xss = true">';

describe('HighLightCode', () => {
  let consoleError: jest.SpyInstance;

  beforeEach(() => {
    consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleError.mockRestore();
  });

  // highlight.js 는 따로 불러오므로 첫 렌더에는 아직 하이라이트 결과가 없다. 그 사이에 원문을
  // 그대로 넣으면 하이라이트가 끝나기 전에 코드 안의 태그가 요소로 만들어진다.
  test('should escape the code when it renders before highlight.js has loaded', () => {
    const { container } = render(<HighLightCode code={XSS_CODE} />);

    expect(container.querySelector('img')).toBeNull();
    expect(container.textContent).toContain(XSS_CODE);
  });

  test('should escape the code when it renders before highlight.js has loaded in wrap mode', () => {
    const { container } = render(<HighLightCode code={XSS_CODE} wrap />);

    expect(container.querySelector('img')).toBeNull();
    expect(container.textContent).toContain(XSS_CODE);
  });

  test('should render the highlighted code when highlight.js has loaded', async () => {
    const { container } = render(<HighLightCode language="java" code="public class A {}" />);

    await waitFor(() => {
      expect(container.querySelector('.hljs-keyword')).not.toBeNull();
    });
    expect(container.textContent).toContain('public class A {}');
  });

  test('should keep the code escaped when it has been highlighted', async () => {
    const { container } = render(<HighLightCode language="json" code={XSS_CODE} />);

    await waitFor(() => {
      expect(container.querySelector('code')?.innerHTML).not.toBe('');
    });
    expect(container.querySelector('img')).toBeNull();
    expect(container.textContent).toContain(XSS_CODE);
  });

  // 등록하지 않은 언어면 hljs.highlight 가 던진다. 그때는 하이라이트 없이 escape 된 원문을 보여준다.
  test('should fall back to the escaped code when highlighting throws', async () => {
    const { container } = render(
      <HighLightCode language={'unregistered' as unknown as 'text'} code={XSS_CODE} />,
    );

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Highlight Error', expect.any(Error));
    });
    expect(container.querySelector('img')).toBeNull();
    expect(container.textContent).toContain(XSS_CODE);
  });

  test('should highlight the new code when the code changes', async () => {
    const { container, rerender } = render(<HighLightCode language="java" code="int first;" />);
    rerender(<HighLightCode language="java" code="return second;" />);

    await waitFor(() => {
      expect(container.querySelector('.hljs-keyword')?.textContent).toBe('return');
    });
    expect(container.querySelector('.hljs-type')).toBeNull();
    expect(container.textContent).toBe('return second;');
  });
});
