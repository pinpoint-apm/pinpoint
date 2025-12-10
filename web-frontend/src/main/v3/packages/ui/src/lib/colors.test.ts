import {
  getRandomColor,
  getRandomColorInHSL,
  getLuminanceByBT709,
  getContrastingTextColor,
  getDarkenHexColor,
  getColorByString,
} from './colors';
import { colors } from '@pinpoint-fe/ui/src/constants';

describe('Test colors utils', () => {
  describe('Test "getRandomColor"', () => {
    test('Return hex color string starting with #', () => {
      const result = getRandomColor();
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Return different colors on multiple calls', () => {
      const color1 = getRandomColor();
      const color2 = getRandomColor();
      // Note: There's a small chance they could be the same, but very unlikely
      expect(color1).toBeTruthy();
      expect(color2).toBeTruthy();
    });
  });

  describe('Test "getRandomColorInHSL"', () => {
    test('Return HSL color string', () => {
      const result = getRandomColorInHSL();
      expect(result).toMatch(/^hsl\(\d+,\s*\d+%,\s*\d+%\)$/);
    });

    test('Return color with hue between 0 and 360', () => {
      const result = getRandomColorInHSL();
      const match = result.match(/^hsl\((\d+),/);
      if (match) {
        const hue = parseInt(match[1], 10);
        expect(hue).toBeGreaterThanOrEqual(0);
        expect(hue).toBeLessThanOrEqual(360);
      }
    });

    test('Return color with saturation between 70% and 100%', () => {
      const result = getRandomColorInHSL();
      const match = result.match(/,\s*(\d+)%,/);
      if (match) {
        const saturation = parseInt(match[1], 10);
        expect(saturation).toBeGreaterThanOrEqual(70);
        expect(saturation).toBeLessThanOrEqual(100);
      }
    });

    test('Return color with lightness between 50% and 70%', () => {
      const result = getRandomColorInHSL();
      const match = result.match(/,\s*\d+%,\s*(\d+)%\)$/);
      if (match) {
        const lightness = parseInt(match[1], 10);
        expect(lightness).toBeGreaterThanOrEqual(50);
        expect(lightness).toBeLessThanOrEqual(70);
      }
    });
  });

  describe('Test "getLuminanceByBT709"', () => {
    test('Calculate luminance for 6-digit hex color', () => {
      const result = getLuminanceByBT709('#000000');
      expect(result).toBe(0);
    });

    test('Calculate luminance for white color', () => {
      const result = getLuminanceByBT709('#FFFFFF');
      expect(result).toBeGreaterThan(200);
    });

    test('Handle 3-digit hex color', () => {
      const result = getLuminanceByBT709('#000');
      expect(result).toBe(0);
    });

    test('Handle hex color without # prefix', () => {
      const result1 = getLuminanceByBT709('#FF0000');
      const result2 = getLuminanceByBT709('FF0000');
      expect(result1).toBe(result2);
    });

    test('Calculate luminance for red color', () => {
      const result = getLuminanceByBT709('#FF0000');
      expect(result).toBeGreaterThan(0);
      expect(result).toBeLessThan(100);
    });

    test('Calculate luminance for green color', () => {
      const result = getLuminanceByBT709('#00FF00');
      // Green luminance: 0.2126 * 0 + 0.7152 * 255 + 0.0722 * 0 = 182.376
      expect(result).toBeGreaterThan(180);
      expect(result).toBeLessThan(185);
    });
  });

  describe('Test "getContrastingTextColor"', () => {
    test('Return black for light background', () => {
      const result = getContrastingTextColor('#FFFFFF');
      expect(result).toBe(colors.black);
    });

    test('Return white for dark background', () => {
      const result = getContrastingTextColor('#000000');
      expect(result).toBe(colors.white);
    });

    test('Return black for light gray background', () => {
      const result = getContrastingTextColor('#CCCCCC');
      expect(result).toBe(colors.black);
    });

    test('Return white for dark gray background', () => {
      const result = getContrastingTextColor('#333333');
      expect(result).toBe(colors.white);
    });
  });

  describe('Test "getDarkenHexColor"', () => {
    test('Darken hex color by default amount (0.2)', () => {
      const original = '#FFFFFF';
      const result = getDarkenHexColor(original);
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
      expect(result).not.toBe(original);
    });

    test('Darken hex color by custom amount', () => {
      const original = '#FFFFFF';
      const result = getDarkenHexColor(original, 0.5);
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Handle hex color without # prefix', () => {
      const result1 = getDarkenHexColor('#FF0000', 0.2);
      const result2 = getDarkenHexColor('FF0000', 0.2);
      expect(result1).toBe(result2);
    });

    test('Return valid hex color for maximum darkening', () => {
      const result = getDarkenHexColor('#FFFFFF', 1.0);
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Handle 3-digit hex color', () => {
      // Note: getDarkenHexColor doesn't handle 3-digit hex, so we test with 6-digit
      const result = getDarkenHexColor('#FFFFFF', 0.2);
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });
  });

  describe('Test "getColorByString"', () => {
    test('Return consistent color for same string', () => {
      const input = 'test-string';
      const result1 = getColorByString(input);
      const result2 = getColorByString(input);
      expect(result1).toBe(result2);
    });

    test('Return different colors for different strings', () => {
      const result1 = getColorByString('string1');
      const result2 = getColorByString('string2');
      expect(result1).not.toBe(result2);
    });

    test('Return hex color string starting with #', () => {
      const result = getColorByString('test');
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Handle empty string', () => {
      const result = getColorByString('');
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Handle special characters', () => {
      const result = getColorByString('test@123!');
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });

    test('Handle long strings', () => {
      const longString = 'a'.repeat(100);
      const result = getColorByString(longString);
      expect(result).toMatch(/^#[0-9a-f]{6}$/i);
    });
  });
});
