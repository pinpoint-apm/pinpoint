import { ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';

/**
 * Error Detail을 열 때 "어느 예외를 눌러서 왔는가".
 *
 * 한 chain은 상세에서 여러 카드(바깥 예외 → cause 순, `exceptionDepth` 0부터)로 그려지는데, 어느
 * 화면에서 열든 같은 chain 전체를 조회한다. 그래서 이 값이 없으면 상세는 늘 맨 위(가장 바깥 예외)에서
 * 시작한다(#14203).
 *
 * 들어오는 곳에 따라 아는 것이 다르다:
 * - Error Analysis 목록: 행이 곧 chain의 한 카드라 `exceptionDepth`를 정확히 안다.
 * - Call Tree: 행은 예외를 기록한 span event이고 `errorKey`(chain)만 실려 있다. 화면에 보이는
 *   클래스 이름(패키지 없는 이름)과 메시지만 알 수 있다.
 */
export interface ErrorDetailFocus {
  exceptionDepth?: number;
  /** 패키지 포함/미포함 모두 받는다. Call Tree는 패키지 없는 이름을 보여준다. */
  errorClassName?: string;
  errorMessage?: string;
}

const getSimpleClassName = (className: string) => className.slice(className.lastIndexOf('.') + 1);

/**
 * agent는 긴 메시지를 `앞부분...(원래 길이)`로 자른다(`StringUtils.abbreviate`). 자르는 길이는 span
 * event와 exception trace가 서로 달라 한쪽만 잘려 있을 수 있으므로 꼬리표를 떼고 앞부분만 비교한다.
 */
const ABBREVIATED_SUFFIX = /\.\.\.\(\d+\)$/;

const normalizeMessage = (message?: string | null) =>
  (message ?? '').replace(ABBREVIATED_SUFFIX, '');

const isExactMessage = (a?: string | null, b?: string | null) =>
  normalizeMessage(a) === normalizeMessage(b);

/**
 * Call Tree 링크가 URL에 싣는 메시지의 최대 길이. agent와 달리 꼬리표 없이 자르므로, 이 길이에 닿은
 * 메시지는 잘렸을 수 있다고 본다.
 */
export const ERROR_MESSAGE_URL_MAX_LENGTH = 200;

/**
 * 앞부분이 같으면 같은 메시지로 본다 — 단 **짧은 쪽이 실제로 잘린 경우에만**. 잘리지 않은 짧은 메시지는
 * 그 자체로 완전한 메시지라, 그것으로 시작하는 더 긴 메시지와는 다른 예외다. 그렇게 보지 않으면 바깥
 * 'bad'와 원인 'bad xxx…'(URL에서 잘림) 중 원인을 눌러도 둘 다 걸려 바깥이 골라진다(PR #14619 리뷰).
 *
 * - 카드(exception trace): agent가 자르면 꼬리표가 붙는다.
 * - 누른 줄(Call Tree): agent 꼬리표, 또는 URL에 실으며 `ERROR_MESSAGE_URL_MAX_LENGTH`로 자른 것.
 *
 * 빈 메시지는 모든 메시지의 앞부분이라 짝을 가리는 데 쓸 수 없으므로 같다고 보지 않는다(빈 것끼리는
 * exact가 맡는다).
 */
const isPrefixMessage = (cardMessage?: string | null, focusMessage?: string | null) => {
  const card = normalizeMessage(cardMessage);
  const focus = normalizeMessage(focusMessage);
  if (!card || !focus || card === focus) {
    return false;
  }
  if (card.length < focus.length) {
    const isCardCut = ABBREVIATED_SUFFIX.test(cardMessage ?? '');
    return isCardCut && focus.startsWith(card);
  }
  const isFocusCut =
    ABBREVIATED_SUFFIX.test(focusMessage ?? '') ||
    (focusMessage ?? '').length >= ERROR_MESSAGE_URL_MAX_LENGTH;
  return isFocusCut && card.startsWith(focus);
};

/**
 * 상세 카드 중 `focus`가 가리키는 카드의 위치. 못 찾으면 -1(맨 위에서 시작한다).
 *
 * 1. `exceptionDepth`를 알면 그 카드다. 클래스 이름도 왔다면 맞는지 함께 본다 — 다르면 다른 chain의
 *    정보이므로 믿지 않고 아래 규칙으로 넘어간다.
 * 2. 클래스 이름이 같은 카드를 찾는다. 같은 클래스가 chain에 여러 번 나오면 메시지로 좁힌다
 *    (정확히 같은 것 → 앞부분이 같은 것 순).
 *    그래도 여럿이면(클래스·메시지가 모두 같으면) 가장 바깥 카드다 — 화면에서 서로 구별되지 않는다.
 *    같은 예외를 여러 메서드가 다시 던진 경우에도 Call Tree에는 행이 여럿 생기지만 상세 카드는 하나다.
 */
export const findErrorDetailFocusIndex = (
  data: ErrorAnalysisTransactionInfo.ErrorData[],
  focus?: ErrorDetailFocus,
): number => {
  if (!focus) {
    return -1;
  }

  const { exceptionDepth, errorClassName, errorMessage } = focus;
  const focusSimpleName = errorClassName ? getSimpleClassName(errorClassName) : undefined;
  const isSameClass = (d: ErrorAnalysisTransactionInfo.ErrorData) =>
    getSimpleClassName(d.errorClassName) === focusSimpleName;

  if (typeof exceptionDepth === 'number') {
    const index = data.findIndex(
      (d) => d.exceptionDepth === exceptionDepth && (!focusSimpleName || isSameClass(d)),
    );
    if (index !== -1) {
      return index;
    }
  }

  if (!focusSimpleName) {
    return -1;
  }

  // 메시지는 정확히 같은 것 → 앞부분이 같은 것 → 클래스만 같은 것 순으로 좁힌다. 앞부분 비교를 먼저
  // 하면 'bad'(바깥)와 'bad id'(cause) 중 'bad id'를 눌러도 둘 다 걸려 바깥 카드가 골라진다.
  const sameClass = data.map((d, index) => ({ d, index })).filter(({ d }) => isSameClass(d));
  const exactMessage = sameClass.filter(({ d }) => isExactMessage(d.errorMessage, errorMessage));
  const prefixMessage = sameClass.filter(({ d }) => isPrefixMessage(d.errorMessage, errorMessage));
  const candidates = [exactMessage, prefixMessage, sameClass].find((c) => c.length > 0) ?? [];

  if (candidates.length === 0) {
    return -1;
  }

  return candidates.reduce((outermost, curr) =>
    curr.d.exceptionDepth < outermost.d.exceptionDepth ? curr : outermost,
  ).index;
};
