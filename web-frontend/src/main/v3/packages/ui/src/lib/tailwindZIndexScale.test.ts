import path from 'path';
import { zIndex } from '../constants/theme';
import { cn } from './utils';
import { collectSourceFiles, readSourceLines } from './__fixtures__/sourceFiles';

/**
 * 층(z-index)은 `constants/zIndexLayers.ts` 의 이름으로 쓴다. 예전에는 기준 없이 숫자를 하나씩 올려
 * 서로 이기려 했다(1099 → 1110, 1000 → 1001, 2000 → 2001!, 5000 → 5001, 9999, 10000).
 * 새 숫자가 다시 들어오지 않도록 여기서 막는다.
 *
 * 컴포넌트 안에서만 순서를 정하는 작은 숫자(`z-1`, `z-10`, `z-20`)는 허용한다. 화면 틀의 층은
 * 그보다 위(30부터)에 있으므로, 이 범위의 숫자로는 화면 틀의 층을 넘지 못한다.
 */
const MAX_LOCAL_Z_INDEX = 20;

const PATTERNS = [
  // 유틸리티 클래스: `z-<숫자>` (음수, `!` 가 붙은 것 포함)
  /(?<![\w-])z-(\d+)(?![\w./%[-])/g,
  // 인라인 스타일: `zIndex: <숫자>`, `style.zIndex = '<숫자>'`
  /zIndex\s*[:=]\s*['"]?(\d+)/g,
  // CSS: `z-index: <숫자>`
  /z-index\s*:\s*(\d+)/g,
];

// 임의값은 크기와 상관없이 막는다 — 층 표를 우회하는 통로다.
const ARBITRARY = /(?<![\w-])z-\[[^\]]+\]/g;

const SOURCE_ROOT = path.resolve(__dirname, '..');
// 층을 겨루는 것은 이 패키지만이 아니다. 차트 패키지와 앱의 요소도 같은 화면에 함께 쌓인다.
const SOURCE_ROOTS = [
  SOURCE_ROOT,
  path.resolve(__dirname, '../../../scatter-chart/src'),
  path.resolve(__dirname, '../../../server-map/src'),
  path.resolve(__dirname, '../../../../apps/web/src'),
];

describe('층(z-index)은 층 표의 이름으로 쓴다', () => {
  it('컴포넌트 안의 작은 숫자보다 큰 z-index 를 숫자로 쓰지 않는다', () => {
    const offenders: string[] = [];

    // 이 테스트 자신은 뺀다 — 주석에 위반 예시를 적어 두었다.
    const files = SOURCE_ROOTS.flatMap((root) => collectSourceFiles(root, [__filename]));

    for (const file of files) {
      const lines = readSourceLines(file);

      lines.forEach((line, index) => {
        const where = `${path.relative(path.resolve(SOURCE_ROOT, '../../..'), file)}:${index + 1}`;

        for (const pattern of PATTERNS) {
          for (const match of line.matchAll(pattern)) {
            if (Number(match[1]) > MAX_LOCAL_Z_INDEX) {
              offenders.push(`${match[0]}  ${where}`);
            }
          }
        }
        for (const match of line.matchAll(ARBITRARY)) {
          offenders.push(`${match[0]}  ${where}`);
        }
      });
    }

    expect(offenders).toEqual([]);
  });

  it('화면 틀의 층은 아래에서 위로 이 순서다', () => {
    const frame = [
      zIndex['chart-overlay'],
      zIndex['map-menu'],
      zIndex.drawer,
      zIndex['chart-board'],
      zIndex['resize-handle'],
      zIndex.header,
    ];

    expect(frame).toEqual([...frame].sort((a, b) => a - b));
    expect(new Set(frame).size).toBe(frame.length);
    expect(zIndex['chart-overlay']).toBeGreaterThan(MAX_LOCAL_Z_INDEX);
  });

  it('떠 있는 것(팝업·알림)은 화면 틀의 어떤 층보다도 위다', () => {
    expect(zIndex.popup).toBeGreaterThan(zIndex.header);
    expect(zIndex.toast).toBeGreaterThan(zIndex.popup);
  });

  it('cn 은 이름 붙은 층끼리의 충돌을 뒤의 것으로 정리한다', () => {
    expect(cn('z-popup', 'z-toast')).toBe('z-toast');
    expect(cn('z-10', 'z-popup')).toBe('z-popup');
  });
});
