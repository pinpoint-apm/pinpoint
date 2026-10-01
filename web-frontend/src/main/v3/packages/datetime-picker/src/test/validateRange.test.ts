import { normalizeValidateResult } from '../utils/validateRange';
import type { ValidateRangeResult } from '../types';

describe('normalizeValidateResult', () => {
  it.each([
    ['true', true, { valid: true, message: undefined }],
    ['false', false, { valid: false, message: undefined }],
    ['빈 문자열', '', { valid: false, message: undefined }],
    ['문구 문자열', '최대 2일까지', { valid: false, message: '최대 2일까지' }],
    ['{ valid: true }', { valid: true }, { valid: true, message: undefined }],
    ['{ valid: false }', { valid: false }, { valid: false, message: undefined }],
    [
      '{ valid: false, message }',
      { valid: false, message: '범위 초과' },
      { valid: false, message: '범위 초과' },
    ],
  ])('%s를 정규화한다', (_label, input, expected) => {
    const result = normalizeValidateResult(input as ValidateRangeResult);
    expect(result.valid).toBe(expected.valid);
    expect(result.message).toBe(expected.message);
  });

  it('valid일 때는 message를 버린다 (경고 문구 채널이 아니다)', () => {
    expect(normalizeValidateResult({ valid: true, message: '무시됨' })).toEqual({
      valid: true,
      message: undefined,
    });
  });

  it.each([
    ['undefined', undefined],
    ['null', null],
  ])('%s는 기존 falsy 동작대로 무효로 본다', (_label, input) => {
    expect(normalizeValidateResult(input as unknown as ValidateRangeResult)).toEqual({
      valid: false,
    });
  });
});
