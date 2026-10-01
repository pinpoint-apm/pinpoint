import './tailwind.css';
import './datetimePicker.scss';
import React from 'react';
import { isValid } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { useUpdateEffect } from '../utils/useUpdateEffect';
import { SEAM_TOKEN } from '../constants/patterns';
import { DateRange, PickerError } from '../types';
import { getLocale } from '../utils/locale';
import { getUiText } from '../utils/uiText';
import { DatePanel } from './DatePanel';
import { withPortalPanelContainer } from './hoc/withPortalPanelContainer';
import AppContext from './context/appContext';
import { cn } from '../utils/style';
import { RichDatetimePickerProps } from './types';
export type { RichDatetimePickerProps };
import { useRichDatetimePicker } from './hooks/useRichDatetimePicker';
import { useDateInput } from './hooks/useDateInput';
import { DateTimeTrigger } from './DateTimeTrigger';

const DatePanelWithPortalContainer = withPortalPanelContainer(DatePanel);

export const RichDatetimePicker = ({
  className = '',
  disable,
  inputClassName = '',
  triggerClassName = '',
  panelClassName = '',
  renderIcon,
  datePickerClassName = '',
  startDate,
  endDate,
  minDate,
  maxDate,
  children,
  seamToken = SEAM_TOKEN,
  localeKey = 'en',
  timeZone,
  dateFormat = 'MMM do, hh:mm a',
  defaultOpen,
  onChange,
  getPanelContainer,
  validateDatePickerRange = () => true,
  displayedInput,
  // deprecated: 하위 호환을 위해 수용하지만 동작하지 않으며 아래에서 경고만 남긴다
  formatTag,
  customTimeViewSlideDirection,
  ...props
}: RichDatetimePickerProps) => {
  const tz = timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone;

  // deprecated prop 사용 시 1회 경고 (동작에는 영향 없음)
  React.useEffect(() => {
    if (formatTag !== undefined) {
      console.warn(
        '[RichDatetimePicker] `formatTag` is deprecated and no longer has any effect; the duration tag was removed in the 2-column redesign.',
      );
    }
    if (customTimeViewSlideDirection !== undefined) {
      console.warn(
        '[RichDatetimePicker] `customTimeViewSlideDirection` is deprecated and no longer has any effect; the slide-out was replaced by the "?" custom-time tooltip.',
      );
    }
    // 마운트 시 1회만 검사
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 날짜 포맷팅 함수 (useDateInput에서 사용)
  const getFormattedDate = React.useCallback(
    (fromDate: Date, toDate: Date) => {
      const locale = getLocale(localeKey);
      const zonedFrom = formatInTimeZone(fromDate, tz, dateFormat, { locale });
      const zonedTo = formatInTimeZone(toDate, tz, dateFormat, { locale });
      return `${zonedFrom} ${seamToken} ${zonedTo}`;
    },
    [tz, dateFormat, localeKey, seamToken],
  );

  // 메인 상태 관리 훅
  const {
    locale,
    from,
    to,
    draftFrom,
    draftTo,
    open,
    setOpen,
    openPanel,
    appContext,
    containerRef,
    triggerRef,
    displayInputRef,
    handleDraftDatePickerChange,
    handleTimeBadgeChange,
    handleApply,
    handleCancel,
    handlePanelChange,
    runConsumerValidation,
    rangeError,
    setRangeError,
  } = useRichDatetimePicker({
    startDate,
    endDate,
    localeKey,
    timeZone,
    seamToken,
    defaultOpen,
    getPanelContainer,
    minDate,
    maxDate,
    validateDatePickerRange,
    onChange,
    getFormattedDate,
  });

  // 입력 검증 및 포맷팅 훅
  const {
    isValidInput,
    inputError,
    dateInput,
    displayInput,
    clearInputError,
    setDateInput,
    setDisplayInput,
    handleChangeInput,
    handleKeyDownInput,
  } = useDateInput({
    from,
    to,
    locale,
    dateFormat,
    timeZone: tz,
    seamToken: appContext.seamToken,
    minDate,
    maxDate,
    validateRange: runConsumerValidation,
  });

  // displayedInput prop 변경 시 동기화
  useUpdateEffect(() => {
    setDisplayInput(displayedInput);
  }, [displayedInput, setDisplayInput]);

  // 날짜 변경 시 입력 필드 업데이트
  React.useEffect(() => {
    if (from && to) {
      setDateInput(getFormattedDate(from, to));
      if (isValid(from) && isValid(to)) {
        clearInputError();
      }
    }
  }, [from, to, locale, dateFormat, tz, getFormattedDate, setDateInput, clearInputError]);

  // 입력이 바뀌면 직전 범위 검증 실패 문구는 무효다
  const handleInputChange = React.useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setRangeError(null);
      handleChangeInput(e);
    },
    [handleChangeInput, setRangeError],
  );

  // Enter 키 입력 처리
  const handleKeyDown = React.useCallback(
    (e: React.KeyboardEvent) => {
      // rangeError는 이미 handleInputChange가 키 입력마다 비웠고, 커밋되면 패널이 닫힌 뒤
      // openPanel이 다시 비운다. 여기서 또 지울 필요 없다.
      const shouldClose = handleKeyDownInput(e, (dates, text) => {
        onChange?.(dates, text);
        setDisplayInput(text);
      });
      if (shouldClose) {
        setOpen(false);
      }
    },
    [handleKeyDownInput, onChange, setDisplayInput, setOpen],
  );

  // 패널 변경 핸들러 (입력 검증 상태도 업데이트)
  const handlePanelChangeWithValidation = React.useCallback(
    (dates: DateRange, text: string, timeUnit?: string) => {
      clearInputError();
      // 커밋된 경우에만 트리거에 라벨 텍스트를 노출한다
      if (handlePanelChange(dates, text, timeUnit)) {
        setDisplayInput(text);
      }
    },
    [handlePanelChange, clearInputError, setDisplayInput],
  );

  // Apply(절대 범위 확정) 시에는 라벨을 지워 날짜 범위 텍스트가 보이도록 한다.
  // 검증에 걸려 커밋되지 않았으면 기존 라벨을 그대로 둔다.
  const handleApplyWithDisplay = React.useCallback(() => {
    // Apply는 텍스트 입력 경로를 포기한다는 의사표시다. 여기서 입력 오류를 비우지 않으면
    // 입력 오류가 항상 우선하므로 범위 검증 문구가 영영 보이지 않는다.
    clearInputError();
    if (handleApply()) {
      setDisplayInput(undefined);
    }
  }, [handleApply, clearInputError, setDisplayInput]);

  // 입력 오류가 범위 오류보다 우선한다. 기본 문구는 state에 담지 않고 렌더 중 해소해
  // 로케일이 바뀌면 문구도 따라가게 한다.
  const t = React.useMemo(() => getUiText(localeKey), [localeKey]);
  const rawError = inputError ?? rangeError;
  const error = React.useMemo<PickerError | null>(
    () => (rawError ? { ...rawError, message: rawError.message ?? t.errors[rawError.code] } : null),
    [rawError, t],
  );

  // children은 패널 본문을 통째로 대체하므로 메시지 노드가 렌더되지 않는다.
  // 판정 기준은 DatePanel의 분기(`children ? ... : 기본 본문`)와 **같아야** 한다 —
  // `children != null`로 보면 `{cond && <X/>}`의 false에서 둘이 갈라진다.
  const hasCustomBody = Boolean(children);
  const reactId = React.useId();
  const errorId = `${reactId}error`;
  // 메시지 노드가 실제로 렌더될 때만 참조한다.
  const describedBy = open && error && !hasCustomBody ? errorId : undefined;

  const contextValue = React.useMemo(() => ({ appContext }), [appContext]);

  return (
    <AppContext.Provider value={contextValue}>
      <div className={cn('rich-datetime-picker rdp:relative', className)} ref={containerRef}>
        <DateTimeTrigger
          open={open}
          disable={disable}
          isValidInput={isValidInput}
          triggerClassName={triggerClassName}
          inputClassName={inputClassName}
          renderIcon={renderIcon}
          dateInput={dateInput}
          displayInput={displayInput}
          displayInputRef={displayInputRef}
          onOpen={openPanel}
          onInputChange={handleInputChange}
          onInputKeyDown={handleKeyDown}
          describedBy={describedBy}
        />
        {open && (
          <DatePanelWithPortalContainer
            open={open}
            className={panelClassName}
            datePickerClassName={datePickerClassName}
            startDate={draftFrom}
            endDate={draftTo}
            locale={locale}
            dateFormat={dateFormat}
            onChange={handlePanelChangeWithValidation}
            onChangeDatePicker={handleDraftDatePickerChange}
            onTimeBadgeChange={handleTimeBadgeChange}
            onApply={handleApplyWithDisplay}
            onClose={handleCancel}
            getPanelContainer={getPanelContainer}
            onClickOutside={handleCancel}
            triggerRef={triggerRef}
            minDate={minDate}
            maxDate={maxDate}
            error={error}
            errorId={errorId}
            {...props}
          >
            {children}
          </DatePanelWithPortalContainer>
        )}
      </div>
    </AppContext.Provider>
  );
};
