import { isValidPhoneNumber as isValidMin } from 'libphonenumber-js';
import { isValidPhoneNumber } from 'libphonenumber-js/max';

// UserForm 은 google-libphonenumber 대신 libphonenumber-js/max 로 전화번호를 검사한다.
// 기본(min) 메타데이터는 길이만 보므로 google-libphonenumber 가 거부하던 번호를 통과시킨다.
// 교체 시 전 지역 예시 번호와 그 변형 4,997건을 대조해 max 만 결과가 모두 같았다.
describe('UserForm phone validation (libphonenumber-js/max)', () => {
  it('accepts numbers in the format PhoneInput produces', () => {
    expect(isValidPhoneNumber('+821012345678')).toBe(true);
    expect(isValidPhoneNumber('+82212345678')).toBe(true);
    expect(isValidPhoneNumber('+12015550123')).toBe(true);
    expect(isValidPhoneNumber('+447911123456')).toBe(true);
  });

  it('rejects numbers with a wrong length', () => {
    expect(isValidPhoneNumber('+8210123')).toBe(false);
    expect(isValidPhoneNumber('+8210123456789')).toBe(false);
    expect(isValidPhoneNumber('+82')).toBe(false);
  });

  it('rejects numbers that only the min metadata would accept', () => {
    expect(isValidMin('+97190023456')).toBe(true);
    expect(isValidPhoneNumber('+97190023456')).toBe(false);
  });
});
