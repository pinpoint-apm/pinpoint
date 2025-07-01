import { formatBytes } from './size'; // Replace with your actual file path

describe('formatBytes', () => {
  it('should return "0 Bytes" when input is 0', () => {
    expect(formatBytes(0)).toBe('0 Bytes');
  });

  it('should return "1 KB" for 1024 bytes', () => {
    expect(formatBytes(1024)).toBe('1 KB');
  });

  it('should return "1 MB" for 1048576 bytes (1024 * 1024)', () => {
    expect(formatBytes(1024 * 1024)).toBe('1 MB');
  });

  it('should return "1 GB" for 1073741824 bytes (1024 * 1024 * 1024)', () => {
    expect(formatBytes(1024 * 1024 * 1024)).toBe('1 GB');
  });

  it('should return "1 TB" for 1024^4 bytes', () => {
    expect(formatBytes(1024 ** 4)).toBe('1 TB');
  });

  it('should respect the decimals parameter for rounding', () => {
    expect(formatBytes(1500, 0)).toBe('1 KB'); // No decimals
    expect(formatBytes(1500, 2)).toBe('1.46 KB'); // 2 decimal places
    expect(formatBytes(1500, 4)).toBe('1.4648 KB'); // 4 decimal places
  });
});
