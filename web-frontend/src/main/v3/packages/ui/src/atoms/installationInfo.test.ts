import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import {
  installationApplicationNameAtom,
  installationAgentIdAtom,
} from './installationInfo';

describe('Test installationInfo atoms', () => {
  describe('Test "installationApplicationNameAtom"', () => {
    test('should initialize with empty string', () => {
      const { result } = renderHook(() => useAtom(installationApplicationNameAtom));
      expect(result.current[0]).toBe('');
    });

    test('should update with application name', () => {
      const { result } = renderHook(() => useAtom(installationApplicationNameAtom));

      act(() => {
        result.current[1]('MyApplication');
      });

      expect(result.current[0]).toBe('MyApplication');
    });

    test('should update with different application name', () => {
      const { result } = renderHook(() => useAtom(installationApplicationNameAtom));

      act(() => {
        result.current[1]('App1');
        result.current[1]('App2');
      });

      expect(result.current[0]).toBe('App2');
    });

    test('should reset to empty string', () => {
      const { result } = renderHook(() => useAtom(installationApplicationNameAtom));

      act(() => {
        result.current[1]('MyApplication');
        result.current[1]('');
      });

      expect(result.current[0]).toBe('');
    });
  });

  describe('Test "installationAgentIdAtom"', () => {
    test('should initialize with empty string', () => {
      const { result } = renderHook(() => useAtom(installationAgentIdAtom));
      expect(result.current[0]).toBe('');
    });

    test('should update with agent id', () => {
      const { result } = renderHook(() => useAtom(installationAgentIdAtom));

      act(() => {
        result.current[1]('agent-123');
      });

      expect(result.current[0]).toBe('agent-123');
    });

    test('should update with different agent id', () => {
      const { result } = renderHook(() => useAtom(installationAgentIdAtom));

      act(() => {
        result.current[1]('agent-123');
        result.current[1]('agent-456');
      });

      expect(result.current[0]).toBe('agent-456');
    });

    test('should reset to empty string', () => {
      const { result } = renderHook(() => useAtom(installationAgentIdAtom));

      act(() => {
        result.current[1]('agent-123');
        result.current[1]('');
      });

      expect(result.current[0]).toBe('');
    });
  });
});

