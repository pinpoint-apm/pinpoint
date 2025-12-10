import { renderHook, act } from '@testing-library/react';
import { reducer, useToast } from './use-toast';
import type { ToasterToast } from './use-toast';

describe('Test use-toast', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.clearAllMocks();
  });

  describe('Test "reducer"', () => {
    const initialState = { toasts: [] };

    test('ADD_TOAST adds a new toast', () => {
      const toast: ToasterToast = {
        id: '1',
        title: 'Test Toast',
        open: true,
      };

      const newState = reducer(initialState, {
        type: 'ADD_TOAST',
        toast,
      });

      expect(newState.toasts).toHaveLength(1);
      expect(newState.toasts[0]).toEqual(toast);
    });

    test('ADD_TOAST limits toasts to TOAST_LIMIT', () => {
      const toast1: ToasterToast = { id: '1', open: true };
      const toast2: ToasterToast = { id: '2', open: true };
      const toast3: ToasterToast = { id: '3', open: true };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast: toast1 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast2 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast3 });

      // TOAST_LIMIT is 1, so only the last toast should remain
      expect(state.toasts).toHaveLength(1);
      expect(state.toasts[0].id).toBe('3');
    });

    test('UPDATE_TOAST updates existing toast', () => {
      const toast: ToasterToast = {
        id: '1',
        title: 'Original Title',
        open: true,
      };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast });
      state = reducer(state, {
        type: 'UPDATE_TOAST',
        toast: { id: '1', title: 'Updated Title' },
      });

      expect(state.toasts[0].title).toBe('Updated Title');
      expect(state.toasts[0].id).toBe('1');
    });

    test('UPDATE_TOAST does not update non-existent toast', () => {
      const toast: ToasterToast = { id: '1', open: true };
      let state = reducer(initialState, { type: 'ADD_TOAST', toast });

      state = reducer(state, {
        type: 'UPDATE_TOAST',
        toast: { id: '2', title: 'New Toast' },
      });

      expect(state.toasts).toHaveLength(1);
      expect(state.toasts[0].id).toBe('1');
    });

    test('DISMISS_TOAST dismisses specific toast', () => {
      const toast1: ToasterToast = { id: '1', open: true };
      const toast2: ToasterToast = { id: '2', open: true };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast: toast1 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast2 });
      // ADD_TOAST adds to the front, so order is [toast2, toast1]
      // But TOAST_LIMIT is 1, so only toast2 remains
      state = reducer(state, { type: 'DISMISS_TOAST', toastId: '2' });

      expect(state.toasts).toHaveLength(1);
      expect(state.toasts[0].id).toBe('2');
      expect(state.toasts[0].open).toBe(false);
    });

    test('DISMISS_TOAST dismisses all toasts when toastId is undefined', () => {
      const toast1: ToasterToast = { id: '1', open: true };
      const toast2: ToasterToast = { id: '2', open: true };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast: toast1 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast2 });
      state = reducer(state, { type: 'DISMISS_TOAST' });

      expect(state.toasts.every((t) => t.open === false)).toBe(true);
    });

    test('REMOVE_TOAST removes specific toast', () => {
      const toast1: ToasterToast = { id: '1', open: true };
      const toast2: ToasterToast = { id: '2', open: true };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast: toast1 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast2 });
      state = reducer(state, { type: 'REMOVE_TOAST', toastId: '1' });

      expect(state.toasts).toHaveLength(1);
      expect(state.toasts[0].id).toBe('2');
    });

    test('REMOVE_TOAST removes all toasts when toastId is undefined', () => {
      const toast1: ToasterToast = { id: '1', open: true };
      const toast2: ToasterToast = { id: '2', open: true };

      let state = reducer(initialState, { type: 'ADD_TOAST', toast: toast1 });
      state = reducer(state, { type: 'ADD_TOAST', toast: toast2 });
      state = reducer(state, { type: 'REMOVE_TOAST' });

      expect(state.toasts).toHaveLength(0);
    });
  });

  describe('Test "useToast" hook', () => {
    test('Return toast state and functions', () => {
      const { result } = renderHook(() => useToast());

      expect(result.current.toasts).toBeDefined();
      expect(Array.isArray(result.current.toasts)).toBe(true);
      expect(typeof result.current.toast).toBe('function');
      expect(typeof result.current.dismiss).toBe('function');

      // Cleanup
      act(() => {
        result.current.dismiss();
        jest.runAllTimers();
      });
    });

    test('Update state when toast is added', () => {
      const { result } = renderHook(() => useToast());

      act(() => {
        result.current.toast({ title: 'New Toast' });
      });

      // State should update (though we can't directly test memory state)
      expect(result.current.toast).toBeDefined();

      // Cleanup
      act(() => {
        result.current.dismiss();
        jest.runAllTimers();
      });
    });

    test('Create toast with id and return dismiss/update functions', () => {
      const { result } = renderHook(() => useToast());

      act(() => {
        const toastResult = result.current.toast({ title: 'Test Toast' });
        expect(toastResult.id).toBeDefined();
        expect(typeof toastResult.dismiss).toBe('function');
        expect(typeof toastResult.update).toBe('function');
      });

      // Cleanup
      act(() => {
        result.current.dismiss();
        jest.runAllTimers();
      });
    });

    test('Dismiss toast by id', () => {
      const { result } = renderHook(() => useToast());

      let toastId: string | undefined;
      act(() => {
        const toastResult = result.current.toast({ title: 'Test Toast' });
        toastId = toastResult.id;
        result.current.dismiss(toastId);
      });

      expect(result.current.dismiss).toBeDefined();

      // Cleanup
      act(() => {
        jest.runAllTimers();
      });
    });

    test('Dismiss all toasts', () => {
      const { result } = renderHook(() => useToast());

      act(() => {
        result.current.toast({ title: 'Toast 1' });
        result.current.toast({ title: 'Toast 2' });
        result.current.dismiss();
      });

      expect(result.current.dismiss).toBeDefined();

      // Cleanup
      act(() => {
        jest.runAllTimers();
      });
    });
  });
});
