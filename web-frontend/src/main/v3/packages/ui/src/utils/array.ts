export function insertIf<T>(condition: boolean | (() => boolean), element: () => T[]): T[] {
  return (typeof condition === 'function' ? condition() : condition) ? element() : [];
}
