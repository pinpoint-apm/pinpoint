import fs from 'fs';
import path from 'path';

/**
 * Tailwind 4 는 간격 스케일이 **동적**이다. 3 까지는 스케일에 없는 값(`h-100`, `pt-25`)이 그냥
 * 무효라 아무 일도 하지 않았는데, 4 에서는 `0.25rem × 숫자` 로 계산되어 갑자기 실제 값이 된다.
 *
 * 실제로 그렇게 두 곳이 깨졌다 — Server-map 질의 옵션 버튼이 400px 로 늘어나고(`h-100`),
 * 권한 없음 안내에 100px 상단 여백이 생겼다(`pt-25`). 둘 다 타입 검사·린트·빌드를 모두
 * 통과하므로, 새로 들어오는 것을 여기서 막는다.
 *
 * 아래 목록은 "이 숫자는 의도한 값"이라는 화이트리스트다. 새 값을 쓰려면 설정의 spacing 에
 * 추가하거나(의미 있는 이름) 이 목록에 근거와 함께 넣는다.
 */

// Tailwind 기본 스케일에 있던 값들.
const TAILWIND_SCALE = [
  '0',
  '0.5',
  '1',
  '1.5',
  '2',
  '2.5',
  '3',
  '3.5',
  '4',
  '5',
  '6',
  '7',
  '8',
  '9',
  '10',
  '11',
  '12',
  '14',
  '16',
  '20',
  '24',
  '28',
  '32',
  '36',
  '40',
  '44',
  '48',
  '52',
  '56',
  '60',
  '64',
  '72',
  '80',
  '96',
];

// 우리 설정(packages/ui, datetime-picker)의 spacing 추가값.
const PROJECT_SCALE = ['90', '160', '7.5', '12.5', '13', '70', '85'];

// v4 마이그레이션에서 임의값(`w-[26rem]` 등)을 스케일 표기로 바꾼 것들. 값은 그대로다.
const MIGRATED_FROM_ARBITRARY = ['104', '112', '128', '15'];

const ALLOWED = new Set([...TAILWIND_SCALE, ...PROJECT_SCALE, ...MIGRATED_FROM_ARBITRARY]);

const SPACING_UTILITIES = [
  'p',
  'px',
  'py',
  'pt',
  'pr',
  'pb',
  'pl',
  'm',
  'mx',
  'my',
  'mt',
  'mr',
  'mb',
  'ml',
  'w',
  'h',
  'size',
  'min-w',
  'min-h',
  'max-w',
  'max-h',
  'gap',
  'gap-x',
  'gap-y',
  'space-x',
  'space-y',
  'inset',
  'top',
  'right',
  'bottom',
  'left',
];

const PATTERN = new RegExp(
  `(?<![\\w-])(${SPACING_UTILITIES.join('|')})-(\\d+(?:\\.\\d+)?)(?![\\w./%[-])`,
  'g',
);

const SOURCE_ROOT = path.resolve(__dirname, '..');

const collectFiles = (dir: string): string[] =>
  fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      return collectFiles(full);
    }
    // 이 테스트 자신은 뺀다 — 주석에 위반 예시(`h-100`, `pt-25`)를 적어 두었다.
    if (full === __filename) {
      return [];
    }
    return /\.(tsx?|css|scss)$/.test(entry.name) ? [full] : [];
  });

/**
 * 주석은 사용이 아니다. 이 규칙의 유래를 설명하는 주석에 위반 예시를 적어 둘 수 있어야 한다.
 * 줄 수를 유지하기 위해 주석 내용만 지우고 줄바꿈은 남긴다.
 */
const stripComments = (source: string) =>
  source
    .replace(/\/\*[\s\S]*?\*\//g, (block) => block.replace(/[^\n]/g, ' '))
    .replace(/(^|[^:])\/\/[^\n]*/g, (_match, prefix) => prefix);

describe('간격 유틸리티는 스케일에 있는 값만 쓴다', () => {
  it('스케일에 없는 숫자를 쓰지 않는다', () => {
    const offenders: string[] = [];

    for (const file of collectFiles(SOURCE_ROOT)) {
      const lines = stripComments(fs.readFileSync(file, 'utf8')).split('\n');

      lines.forEach((line, index) => {
        for (const match of line.matchAll(PATTERN)) {
          const [, utility, value] = match;

          if (!ALLOWED.has(value)) {
            offenders.push(`${utility}-${value}  ${path.relative(SOURCE_ROOT, file)}:${index + 1}`);
          }
        }
      });
    }

    expect(offenders).toEqual([]);
  });
});
