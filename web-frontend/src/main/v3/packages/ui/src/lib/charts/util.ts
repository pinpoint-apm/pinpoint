// Some tags cannot be used as a CSS variable name because it contains ; :
// To use tags as CSS variable, we should converte it.
export function toCssVariable(input: string) {
  return input.replace(/[^a-zA-Z0-9]/g, (match) => {
    return `_${match.charCodeAt(0).toString(16)}`;
  });
}
