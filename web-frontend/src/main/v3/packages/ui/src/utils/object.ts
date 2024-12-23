export const getSortedKeys = (obj: unknown) => {
  return Object.keys(obj || {}).sort();
};

export const getDifferentKeys = (a: unknown, b: unknown) => {
  const aKeys = getSortedKeys(a);
  const bKeys = getSortedKeys(b);

  return bKeys.filter((key) => !aKeys.includes(key));
};

export const isSameObjectKeys = (a: unknown, b: unknown) => {
  const aKeys = getSortedKeys(a);
  const bKeys = getSortedKeys(b);

  if (!a || !b || aKeys.length !== bKeys.length) {
    return false;
  }

  return !aKeys.some((key, i) => key !== bKeys[i]);
};

export const isEmpty = (obj?: unknown | (string | number)[]) => {
  return (
    obj === null ||
    obj === undefined ||
    (Array.isArray(obj) ? obj.length === 0 : Object.keys(obj).length === 0)
  );
};

export const getMergedKeys = (...args: (unknown | undefined)[]) => {
  return args.reduce<string[]>((acc, curr) => {
    return [...new Set([...acc, ...Object.keys(curr || {})])];
  }, []);
};
