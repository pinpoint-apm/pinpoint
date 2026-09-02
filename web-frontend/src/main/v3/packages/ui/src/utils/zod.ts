import * as z from 'zod';

/**
 * 숫자 입력 필드용 스키마를 감싼다. `<Input type="number">` 는 값을 문자열로 내놓고
 * `defaultValues` 는 숫자를 주므로, 입력으로는 둘 다 받고 검증 결과만 number 로 좁힌다.
 *
 * `z.coerce.number()` 를 그대로 쓰면 zod 4 에서 **입력 타입이 `unknown`** 이 된다(무엇이든
 * 숫자로 바꿔보기 때문). 그러면 `field.value` 도 `unknown` 이 되어 `<Input {...field} />` 처럼
 * 그대로 넘길 수 없다. 입력을 여기서 문자열/숫자로 좁혀 두면 폼 쪽에 캐스팅이 필요 없다.
 *
 * 빈 문자열은 `0` 이 되고, 숫자가 아닌 문자열은 `NaN` 이 되어 뒤의 제약에서 걸린다.
 * `z.coerce.number()` 와 같은 동작이다.
 *
 * @example
 * z.object({ yMax: numberFromInput(z.number().min(1)) })
 */
export const numberFromInput = (schema: z.ZodNumber) =>
  z.union([z.string(), z.number()]).transform(Number).pipe(schema);
