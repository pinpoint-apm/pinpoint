export const hasParentWithId = (element: HTMLElement | null, parentId: string): boolean => {
  if (!element) {
    return false;
  }
  if (element.id === parentId) {
    return true;
  }
  return hasParentWithId(element.parentElement, parentId);
};
