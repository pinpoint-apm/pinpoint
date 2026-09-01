import { render, screen, act } from '@testing-library/react';
import i18n from 'i18next';
import { initReactI18next, I18nextProvider } from 'react-i18next';
import { en, ko } from '../../constants/locales';
import { HelpPopover } from './HelpPopover';

// Radix Popover 는 열릴 때 ResizeObserver 를 쓰는데 jsdom 에 없다.
(globalThis as unknown as { ResizeObserver: unknown }).ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
};

// apps/web/src/i18n.ts 와 같은 설정으로 실제 로케일 리소스를 올린다.
// HelpPopover 가 검증 대상으로 삼는 것이 번역 값 안의 태그를 엘리먼트로 바꾸는 동작이라,
// 리소스를 모킹하면 검증할 것이 남지 않는다.
beforeAll(async () => {
  await i18n.use(initReactI18next).init({
    resources: { en: { translation: en }, ko: { translation: ko } },
    lng: 'en',
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
  });
});

const setLanguage = async (lng: string) => {
  await act(async () => {
    await i18n.changeLanguage(lng);
  });
};

beforeEach(() => setLanguage('en'));

// popover 내용은 Portal 로 body 에 붙으므로, 다음 렌더 전에 반드시 unmount 해야
// 앞선 렌더의 내용을 다시 읽는 일이 없다.
const openHelp = async (helpKey: string) => {
  const { unmount } = render(
    <I18nextProvider i18n={i18n}>
      <HelpPopover helpKey={helpKey} />
    </I18nextProvider>,
  );
  await act(async () => {
    screen.getByRole('button').click();
  });
  const contents = document.querySelectorAll('[data-radix-popper-content-wrapper]');
  if (contents.length !== 1) {
    throw new Error(`expected 1 popover for ${helpKey}, found ${contents.length}`);
  }
  return { content: contents[0] as HTMLElement, unmount };
};

describe('HelpPopover', () => {
  it('reads the help content object out of the translation resource', () => {
    expect(i18n.t('HELP_VIEWER.APDEX_SCORE', { returnObjects: true })).toMatchObject({
      TITLE: 'Apdex Score',
      CATEGORY: [{ TITLE: 'Score' }],
    });
  });

  // '<' 하나를 글자로 내보내려고 components 맵에 Lt 를 두고 있다. 그 맵의 항목은
  // 반드시 실제 엘리먼트여야 한다 — 문자열을 넣어 두면 react-i18next 가 cloneElement 를
  // 걸면서 type 이 undefined 인 엘리먼트를 만들어 렌더가 통째로 터진다.
  it('renders <Lt/> as a literal "<" character', async () => {
    const { content } = await openHelp('HELP_VIEWER.APDEX_SCORE');

    expect(content.textContent).toContain('< 0.5');
    expect(content.textContent).not.toContain('<Lt');
  });

  it('turns a named component tag in the translation into that component', async () => {
    const { content } = await openHelp('HELP_VIEWER.SCATTER');

    // '<FaCircle color="#2EB089" size="15"/>' → svg. props 도 함께 넘어가야 한다.
    const icon = content.querySelector('svg[color="#2EB089"]');
    expect(icon).not.toBeNull();
    expect(icon?.getAttribute('width')).toBe('15');
    expect(content.textContent).not.toContain('FaCircle');
  });

  it('keeps basic html nodes in the translation as real elements', async () => {
    const { content } = await openHelp('HELP_VIEWER.NAVBAR');

    expect(content.querySelector('br')).not.toBeNull();
    expect(content.textContent).not.toContain('<br');
  });

  it('renders every help key in both locales without leaving a raw tag in the text', async () => {
    const helpKeys = Object.keys(en.HELP_VIEWER).flatMap((key) =>
      key === 'INSPECTOR'
        ? Object.keys(en.HELP_VIEWER.INSPECTOR).map((sub) => `HELP_VIEWER.INSPECTOR.${sub}`)
        : [`HELP_VIEWER.${key}`],
    );
    expect(helpKeys).toContain('HELP_VIEWER.APDEX_SCORE');

    const rawTagLeftOver: string[] = [];
    for (const lng of ['en', 'ko']) {
      await setLanguage(lng);
      for (const helpKey of helpKeys) {
        const { content, unmount } = await openHelp(helpKey);
        if (/<[A-Za-z]/.test(content.textContent ?? '')) {
          rawTagLeftOver.push(`${lng} ${helpKey}: ${content.textContent}`);
        }
        unmount();
      }
    }

    expect(rawTagLeftOver).toEqual([]);
  });
});
