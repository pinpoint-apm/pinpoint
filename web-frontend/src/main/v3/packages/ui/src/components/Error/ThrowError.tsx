export const ThrowError = ({ error }: { error: Error }) => {
  throw new Error(error.message || 'An error occurred');
};
