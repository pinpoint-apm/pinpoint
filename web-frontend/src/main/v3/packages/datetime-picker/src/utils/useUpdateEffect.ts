import React from 'react';

/**
 * Runs an effect on updates only, skipping the mount run.
 *
 * Replaces `usehooks-ts`' `useUpdateEffect`, which that library removed in v3 ("don't use this
 * hook, it's an anti-pattern"). The behaviour is the same as the hook that was removed.
 *
 * `packages/ui` has its own copy of this. The duplication is deliberate: `ui` depends on
 * `datetime-picker`, not the other way round, so this package cannot import from it and the
 * monorepo has no lower-level package to share it from.
 *
 * The removal has a point: reacting to a state change in an effect is usually worse than doing
 * the work in the event handler that caused it. Prefer that for new code.
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
