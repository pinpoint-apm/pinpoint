export const safeParse = (value: string | null) => {
  if (value === null || value === undefined || value === 'undefined') {
    return undefined;
  }
  try {
    return JSON.parse(value);
  } catch {
    return undefined;
  }
};
