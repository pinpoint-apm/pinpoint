import React from 'react';

/**
 * Runs an effect on updates only, skipping the mount run.
 *
 * Replaces `usehooks-ts`' `useUpdateEffect`, which that library deprecated ("don't use this hook,
 * it's an anti-pattern") — importing it now raises a TS deprecation on every call site. The
 * behaviour is the same, so existing callers can migrate by swapping the import.
 *
 * The deprecation has a point: reacting to a state change in an effect is usually worse than
 * doing the work in the event handler that caused it. Prefer that for new code. This hook is for
 * the cases where an effect genuinely has to run after another effect that writes the same state,
 * which an event handler cannot express.
 */
export const useUpdateEffect = (effect: React.EffectCallback, deps?: React.DependencyList) => {
  const isMounted = React.useRef(false);

  React.useEffect(() => {
    return () => {
      // Reset for StrictMode's double-invoked mount, so the first *real* update is not skipped.
      isMounted.current = false;
    };
  }, []);

  React.useEffect(() => {
    if (!isMounted.current) {
      isMounted.current = true;
      return;
    }
    return effect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
};
