import fs from 'fs';
import path from 'path';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore -- 런타임과 공유하는 색 정의(JS)
import { themeColors } from '../../tailwind.colors.js';
import { zIndexLayers } from '../constants/zIndexLayers';
import { stripComments } from './__fixtures__/sourceFiles';

/**
 * 유틸리티 클래스용 테마는 CSS(`src/theme.css`)에, JS 에서 읽는 값은 `tailwind.colors.js`·
 * `constants/zIndexLayers.ts` 에 있다. Tailwind 4 의 CSS 테마는 JS 로 읽을 수 없어 같은 값을 두 번
 * 적는다. 한쪽만 고치면 `bg-status-fail` 과 차트의 같은 색이, `z-popup` 과 인라인 `zIndex.popup` 이
 * 조용히 갈라진다 — 여기서 맞춰 본다.
 */
const themeCss = stripComments(fs.readFileSync(path.resolve(__dirname, '../theme.css'), 'utf8'));

const readThemeVariables = (namespace: string) =>
  Object.fromEntries(
    [...themeCss.matchAll(new RegExp(`--${namespace}-([\\w-]+):\\s*([^;]+);`, 'g'))].map(
      ([, name, value]) => [name, value.trim()],
    ),
  );

/** `{ primary: { DEFAULT, foreground } }` → `{ primary, 'primary-foreground' }` (Tailwind 의 이름 규칙) */
const flattenColors = (colors: Record<string, string | Record<string, string>>) =>
  Object.fromEntries(
    Object.entries(colors).flatMap(([name, value]) =>
      typeof value === 'string'
        ? [[name, value]]
        : Object.entries(value).map(([shade, v]) => [
            shade === 'DEFAULT' ? name : `${name}-${shade}`,
            v,
          ]),
    ),
  );

describe('Tailwind 테마(theme.css)와 JS 값', () => {
  it('색이 tailwind.colors.js 와 같다', () => {
    expect(readThemeVariables('color')).toEqual(flattenColors(themeColors));
  });

  it('층이 zIndexLayers 와 같다', () => {
    expect(readThemeVariables('z-index')).toEqual(zIndexLayers);
  });
});
