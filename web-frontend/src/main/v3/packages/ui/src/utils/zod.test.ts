import * as z from 'zod';
import { numberFromInput } from './zod';

describe('numberFromInput', () => {
  const schema = numberFromInput(z.number().min(1));

  it('숫자 입력창이 내놓는 문자열을 숫자로 바꾼다', () => {
    expect(schema.parse('150')).toBe(150);
  });

  it('defaultValues 가 주는 숫자도 그대로 받는다', () => {
    expect(schema.parse(150)).toBe(150);
  });

  it('제약은 변환 뒤에 걸린다', () => {
    expect(schema.safeParse('0').success).toBe(false);
    expect(schema.safeParse('1').success).toBe(true);
  });

  // z.coerce.number() 와 같은 동작: 빈 문자열은 0 이 되어 하한에서 걸린다.
  it('빈 문자열은 0 으로 보고 제약에서 거른다', () => {
    expect(schema.safeParse('').success).toBe(false);
  });

  it('숫자가 아닌 문자열은 거른다', () => {
    expect(schema.safeParse('abc').success).toBe(false);
  });

  it('optional 을 이어 붙일 수 있다', () => {
    const optional = numberFromInput(z.number().min(1)).optional();
    expect(optional.safeParse(undefined).success).toBe(true);
    expect(optional.parse('7')).toBe(7);
  });

  it('입력 타입이 문자열과 숫자로 좁혀져 있다', () => {
    // 이 별칭이 unknown 이 되면 `<Input {...field} />` 가 타입 에러가 난다.
    type Input = z.input<typeof schema>;
    const asString: Input = '1';
    const asNumber: Input = 1;
    expect([asString, asNumber]).toEqual(['1', 1]);
  });
});
