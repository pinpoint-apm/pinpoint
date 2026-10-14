import fs from 'fs';
import path from 'path';

/**
 * 소스 전체를 훑어 금지된 표기를 찾는 테스트들(`tailwindSpacingScale`, `tailwindZIndexScale`)이
 * 함께 쓴다. 파일을 고르는 규칙과 주석을 빼는 규칙이 테스트마다 갈리면 한쪽만 놓치는 표기가 생긴다.
 */

/** `dir` 아래의 스타일·코드 파일. `exclude`(호출한 테스트 자신 등)는 뺀다. */
export const collectSourceFiles = (dir: string, exclude: string[] = []): string[] =>
  fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      return collectSourceFiles(full, exclude);
    }
    if (exclude.includes(full)) {
      return [];
    }
    return /\.(tsx?|css|scss)$/.test(entry.name) ? [full] : [];
  });

/**
 * 주석은 사용이 아니다. 규칙의 유래를 설명하는 주석에 위반 예시를 적어 둘 수 있어야 한다.
 * 줄 번호를 유지하기 위해 주석 내용만 지우고 줄바꿈은 남긴다.
 */
export const stripComments = (source: string) =>
  source
    .replace(/\/\*[\s\S]*?\*\//g, (block) => block.replace(/[^\n]/g, ' '))
    .replace(/(^|[^:])\/\/[^\n]*/g, (_match, prefix) => prefix);

/** 주석을 뺀 파일 내용을 줄 단위로. */
export const readSourceLines = (file: string) =>
  stripComments(fs.readFileSync(file, 'utf8')).split('\n');
