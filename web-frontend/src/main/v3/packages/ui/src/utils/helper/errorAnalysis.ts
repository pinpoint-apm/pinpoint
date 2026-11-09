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

const isSameMessage = (a?: string | null, b?: string | null) => {
  const left = normalizeMessage(a);
  const right = normalizeMessage(b);

  return left.startsWith(right) || right.startsWith(left);
};

/**
 * 상세 카드 중 `focus`가 가리키는 카드의 위치. 못 찾으면 -1(맨 위에서 시작한다).
 *
 * 1. `exceptionDepth`를 알면 그 카드다. 클래스 이름도 왔다면 맞는지 함께 본다 — 다르면 다른 chain의
 *    정보이므로 믿지 않고 아래 규칙으로 넘어간다.
 * 2. 클래스 이름이 같은 카드를 찾는다. 같은 클래스가 chain에 여러 번 나오면 메시지로 좁힌다.
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

  const sameClass = data.map((d, index) => ({ d, index })).filter(({ d }) => isSameClass(d));
  const sameMessage = sameClass.filter(({ d }) => isSameMessage(d.errorMessage, errorMessage));
  const candidates = sameMessage.length > 0 ? sameMessage : sameClass;

  if (candidates.length === 0) {
    return -1;
  }

  return candidates.reduce((outermost, curr) =>
    curr.d.exceptionDepth < outermost.d.exceptionDepth ? curr : outermost,
  ).index;
};
