import { renderHook, act } from '@testing-library/react';
import { useAgentListSortBy, AGENT_LIST_SORT_BY } from './useAgentListSortBy';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

describe('useAgentListSortBy', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('returns default sort (STARTTIME_DESC) when localStorage is empty', () => {
    const { result } = renderHook(() => useAgentListSortBy());
    expect(result.current[0]).toBe(AGENT_LIST_SORT_BY.STARTTIME_DESC);
  });

  test('returns custom default when provided', () => {
    const { result } = renderHook(() => useAgentListSortBy(AGENT_LIST_SORT_BY.NAME_ASC));
    expect(result.current[0]).toBe(AGENT_LIST_SORT_BY.NAME_ASC);
  });

  test('returns stored valid sort value from localStorage', () => {
    localStorage.setItem(
      APP_SETTING_KEYS.AGENT_LIST_SORT,
      JSON.stringify(AGENT_LIST_SORT_BY.STARTTIME_ASC),
    );
    const { result } = renderHook(() => useAgentListSortBy());
    expect(result.current[0]).toBe(AGENT_LIST_SORT_BY.STARTTIME_ASC);
  });

  test('falls back to default when stored value is not a valid enum member', () => {
    localStorage.setItem(APP_SETTING_KEYS.AGENT_LIST_SORT, JSON.stringify('INVALID_VALUE'));
    const { result } = renderHook(() => useAgentListSortBy());
    expect(result.current[0]).toBe(AGENT_LIST_SORT_BY.STARTTIME_DESC);
  });

  test('updates sort value when setter is called with a valid value', () => {
    const { result } = renderHook(() => useAgentListSortBy());

    act(() => {
      result.current[1](AGENT_LIST_SORT_BY.NAME_DESC);
    });

    expect(result.current[0]).toBe(AGENT_LIST_SORT_BY.NAME_DESC);
  });

  test('all AGENT_LIST_SORT_BY enum values are accepted as valid', () => {
    Object.values(AGENT_LIST_SORT_BY).forEach((value) => {
      localStorage.setItem(APP_SETTING_KEYS.AGENT_LIST_SORT, JSON.stringify(value));
      const { result } = renderHook(() => useAgentListSortBy());
      expect(result.current[0]).toBe(value);
    });
  });
});
