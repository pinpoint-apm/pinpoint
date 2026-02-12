/**
 * 서버 ProblemDetail (RFC 7807) + CustomExceptionHandler 커스텀 필드 형식.
 * 서버 응답과 1:1 일치 (message 등 호환 필드 없음).
 * @see web/src/main/java/com/navercorp/pinpoint/web/problem/CustomExceptionHandler.java
 */
export type ErrorResponse = {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  /** CustomExceptionHandler.setProperty */
  method?: string;
  parameters?: Record<string, string[]>;
  hostname?: string;
  /** 서버 스택 트레이스 (문자열 배열) */
  trace?: string[];
};

export type ErrorDetailResponse = ErrorResponse & { url?: string };

/**
 * Error 호환용 타입. 서버 ErrorResponse + message, url (throw/표시 시 사용).
 * ErrorBoundary, ErrorToast, ErrorDetailDialog 등에서 Error | ErrorResponse 를
 * 받을 때 message를 함께 쓰려면 이 타입으로 단언하거나 사용.
 */
export type ErrorLike = ErrorResponse & { message?: string; url?: string };
