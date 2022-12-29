export const getDevicePicelRatio = () => {
  const dpr = window?.devicePixelRatio || 2;

  return dpr;
}