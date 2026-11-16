import { ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { ERROR_MESSAGE_URL_MAX_LENGTH, findErrorDetailFocusIndex } from './errorAnalysis';

const card = (
  exceptionDepth: number,
  errorClassName: string,
  errorMessage = '',
): ErrorAnalysisTransactionInfo.ErrorData =>
  ({ exceptionDepth, errorClassName, errorMessage }) as ErrorAnalysisTransactionInfo.ErrorData;

// NestedServletException wrapping IllegalArgumentException, as the issue (#14203) describes.
const chain = [
  card(0, 'org.springframework.web.util.NestedServletException', 'Request processing failed'),
  card(1, 'java.lang.IllegalArgumentException', 'bad id'),
];

describe('findErrorDetailFocusIndex', () => {
  test('should return -1 when there is no focus', () => {
    expect(findErrorDetailFocusIndex(chain, undefined)).toBe(-1);
    expect(findErrorDetailFocusIndex(chain, {})).toBe(-1);
  });

  test('should return -1 when there are no cards', () => {
    expect(findErrorDetailFocusIndex([], { errorClassName: 'IllegalArgumentException' })).toBe(-1);
  });

  test('should find the card of the given depth', () => {
    expect(findErrorDetailFocusIndex(chain, { exceptionDepth: 1 })).toBe(1);
  });

  test('should not depend on the order the cards come in', () => {
    expect(findErrorDetailFocusIndex([...chain].reverse(), { exceptionDepth: 1 })).toBe(0);
  });

  test('should fall back to the class name when the depth points to another class', () => {
    expect(
      findErrorDetailFocusIndex(chain, {
        exceptionDepth: 0,
        errorClassName: 'java.lang.IllegalArgumentException',
      }),
    ).toBe(1);
  });

  test('should match the simple class name the call tree shows', () => {
    expect(
      findErrorDetailFocusIndex(chain, {
        errorClassName: 'IllegalArgumentException',
        errorMessage: 'bad id',
      }),
    ).toBe(1);
  });

  test('should return -1 when no card has the class', () => {
    expect(findErrorDetailFocusIndex(chain, { errorClassName: 'ERROR' })).toBe(-1);
  });

  test('should tell apart a class repeated in the chain by its message', () => {
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'outer'),
      card(1, 'java.lang.RuntimeException', 'inner'),
    ];

    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'inner',
      }),
    ).toBe(1);
  });

  test('should pick the outermost card when the class and message cannot tell them apart', () => {
    const same = [
      card(1, 'java.lang.RuntimeException', 'same'),
      card(0, 'java.lang.RuntimeException', 'same'),
    ];

    expect(
      findErrorDetailFocusIndex(same, { errorClassName: 'RuntimeException', errorMessage: 'same' }),
    ).toBe(1);
  });

  test('should fall back to the class when no message matches', () => {
    expect(
      findErrorDetailFocusIndex(chain, {
        errorClassName: 'IllegalArgumentException',
        errorMessage: 'a different message',
      }),
    ).toBe(1);
  });

  test('should match messages the agent abbreviated to different lengths', () => {
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'outer failure'),
      card(1, 'java.lang.RuntimeException', 'inner failure with a long...(120)'),
    ];

    // The span event cut the same message shorter; the link cut it again for the URL.
    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'inner failure...(120)',
      }),
    ).toBe(1);
  });

  test('should treat a missing message as matching any message', () => {
    const withNull = [
      card(0, 'java.lang.RuntimeException', null as unknown as string),
      card(1, 'java.lang.IllegalStateException'),
    ];

    expect(findErrorDetailFocusIndex(withNull, { errorClassName: 'RuntimeException' })).toBe(0);
  });

  test('should prefer an exact message over a message it starts with', () => {
    // Clicking 'bad id' matches 'bad' by prefix too; the exact card has to win (PR #14619 review).
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'bad'),
      card(1, 'java.lang.RuntimeException', 'bad id'),
    ];

    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'bad id',
      }),
    ).toBe(1);
    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'bad',
      }),
    ).toBe(0);
  });

  test('should not let an empty message match every message by prefix', () => {
    // An empty string is a prefix of anything, so it would otherwise pull in the outer card.
    const repeated = [
      card(0, 'java.lang.RuntimeException', ''),
      card(1, 'java.lang.RuntimeException', 'inner failure with a long message'),
    ];

    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'inner failure...(33)',
      }),
    ).toBe(1);
    expect(
      findErrorDetailFocusIndex(repeated, { errorClassName: 'RuntimeException', errorMessage: '' }),
    ).toBe(0);
  });

  test('should not take a complete short message for the start of a longer one', () => {
    // The link cuts the clicked cause to ERROR_MESSAGE_URL_MAX_LENGTH. The outer 'bad' is a
    // complete message, not a cut one, so it must not match the cause it happens to start
    // (PR #14619 review).
    const longCause = `bad ${'x'.repeat(300)}`;
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'bad'),
      card(1, 'java.lang.RuntimeException', longCause),
    ];
    const focus = {
      errorClassName: 'RuntimeException',
      errorMessage: longCause.slice(0, ERROR_MESSAGE_URL_MAX_LENGTH),
    };

    expect(findErrorDetailFocusIndex(repeated, focus)).toBe(1);

    // The same holds when the exception trace abbreviated the cause.
    const abbreviated = [
      card(0, 'java.lang.RuntimeException', 'bad'),
      card(1, 'java.lang.RuntimeException', `${longCause.slice(0, 256)}...(304)`),
    ];
    expect(findErrorDetailFocusIndex(abbreviated, focus)).toBe(1);
  });

  test('should not take a complete short clicked message for the start of a longer card', () => {
    // 'bad' was not cut on the way, so it names a different exception than 'bad id'.
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'bad id'),
      card(1, 'java.lang.RuntimeException', 'bad request'),
      card(2, 'java.lang.RuntimeException', 'bad'),
    ];

    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'bad',
      }),
    ).toBe(2);
    expect(
      findErrorDetailFocusIndex(repeated.slice(0, 2), {
        errorClassName: 'RuntimeException',
        errorMessage: 'bad',
      }),
    ).toBe(0);
  });

  test('should still match a card the agent abbreviated shorter than the clicked message', () => {
    const repeated = [
      card(0, 'java.lang.RuntimeException', 'outer'),
      card(1, 'java.lang.RuntimeException', 'inner failure...(500)'),
    ];

    expect(
      findErrorDetailFocusIndex(repeated, {
        errorClassName: 'RuntimeException',
        errorMessage: 'inner failure with the rest of a long message',
      }),
    ).toBe(1);
  });
});
