import React from 'react';
import { formatInTimeZone } from 'date-fns-tz';
import { setZonedTimeOfDay } from '../utils/date';
import { cn } from '../utils/style';
import ClockIcon from '../assets/clock.svg?react';

interface TimeBadgeProps {
  value: Date | null | undefined;
  timeZone: string;
  onChange: (next: Date) => void;
  /** aria-label 접두사 (예: 'from' | 'to') */
  label?: string;
  className?: string;
}

type Segment = 'h' | 'm' | 's';

const MAX: Record<Segment, number> = { h: 23, m: 59, s: 59 };
const FORMAT: Record<Segment, string> = { h: 'HH', m: 'mm', s: 'ss' };
const ORDER: Segment[] = ['h', 'm', 's'];

const pad = (n: number) => String(n).padStart(2, '0');
const clamp = (n: number, max: number) => Math.max(0, Math.min(max, n));
const wrap = (n: number, max: number) => (n < 0 ? max : n > max ? 0 : n);

export const TimeBadge = ({ value, timeZone, onChange, label, className }: TimeBadgeProps) => {
  const read = React.useCallback(
    (seg: Segment) => (value ? formatInTimeZone(value, timeZone, FORMAT[seg]) : ''),
    [value, timeZone],
  );

  const initial = { h: read('h'), m: read('m'), s: read('s') };
  const [segs, setSegs] = React.useState<Record<Segment, string>>(initial);
  // 핸들러들이 항상 최신 세그먼트를 읽도록 ref 미러 유지 (auto-advance blur의 stale 클로저 방지)
  const segsRef = React.useRef(segs);
  const focusedRef = React.useRef(false);
  const inputRefs = React.useRef<Record<Segment, HTMLInputElement | null>>({
    h: null,
    m: null,
    s: null,
  });

  // 외부에서 value가 바뀌면(달력 선택 등) 편집 중이 아닐 때만 동기화
  React.useEffect(() => {
    if (!focusedRef.current) {
      const synced = { h: read('h'), m: read('m'), s: read('s') };
      segsRef.current = synced;
      setSegs(synced);
    }
  }, [read]);

  const commit = (next: Record<Segment, string>) => {
    const base = value ?? new Date();
    onChange(
      setZonedTimeOfDay(base, timeZone, {
        h: clamp(Number(next.h) || 0, MAX.h),
        m: clamp(Number(next.m) || 0, MAX.m),
        s: clamp(Number(next.s) || 0, MAX.s),
      }),
    );
  };

  const setSegment = (seg: Segment, raw: string, shouldCommit: boolean) => {
    const next = { ...segsRef.current, [seg]: raw };
    segsRef.current = next;
    setSegs(next);
    if (shouldCommit) commit(next);
  };

  const handleChange = (seg: Segment) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const digits = e.target.value.replace(/\D/g, '').slice(0, 2);
    // 2자리 완성 또는 첫 자리가 명백히 최대값을 넘길 수 없는 경우 커밋
    const complete = digits.length === 2;
    setSegment(seg, digits, complete);
    if (complete) {
      const idx = ORDER.indexOf(seg);
      inputRefs.current[ORDER[idx + 1]]?.focus();
    }
  };

  const step = (seg: Segment, delta: number) => {
    const current = Number(segsRef.current[seg]) || 0;
    const nextVal = wrap(current + delta, MAX[seg]);
    setSegment(seg, pad(nextVal), true);
  };

  const handleKeyDown = (seg: Segment) => (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      step(seg, 1);
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      step(seg, -1);
    }
  };

  const handleWheel = (seg: Segment) => (e: React.WheelEvent<HTMLInputElement>) => {
    if (document.activeElement !== e.currentTarget) return;
    e.preventDefault();
    step(seg, e.deltaY < 0 ? 1 : -1);
  };

  const handleBlur = (seg: Segment) => (e: React.FocusEvent<HTMLInputElement>) => {
    focusedRef.current = false;
    const raw = e.currentTarget.value || segsRef.current[seg];
    const padded = raw === '' ? '' : pad(clamp(Number(raw) || 0, MAX[seg]));
    setSegment(seg, padded, padded !== '');
  };

  return (
    <div className={cn('rich-datetime-picker__badge', className)}>
      <ClockIcon className="rich-datetime-picker__badge-icon" />
      {ORDER.map((seg, i) => (
        <React.Fragment key={seg}>
          {i > 0 && <span className="rich-datetime-picker__badge-sep">:</span>}
          <input
            ref={(el) => {
              inputRefs.current[seg] = el;
            }}
            type="text"
            inputMode="numeric"
            aria-label={`${label ? `${label} ` : ''}${seg === 'h' ? 'hours' : seg === 'm' ? 'minutes' : 'seconds'}`}
            className="rich-datetime-picker__badge-input"
            placeholder="--"
            value={segs[seg]}
            onFocus={(e) => {
              focusedRef.current = true;
              e.currentTarget.select();
            }}
            onChange={handleChange(seg)}
            onKeyDown={handleKeyDown(seg)}
            onWheel={handleWheel(seg)}
            onBlur={handleBlur(seg)}
          />
        </React.Fragment>
      ))}
    </div>
  );
};
