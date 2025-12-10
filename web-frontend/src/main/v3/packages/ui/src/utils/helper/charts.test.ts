import { getMaxTickValue, hasWindow, getEllipsisText, getFormat, getTooltipStr } from './charts';

describe('Test charts helper utils', () => {
  describe('Test "getMaxTickValue"', () => {
    test('Two Date Ranges are Greater Than a Given Value', () => {
      const input = [[108729, 18, 4, 0, 6]];

      const result = getMaxTickValue(input);
      expect(result).toEqual(200000);
    });

    test('Two Date Ranges are Greater Than a Given Value', () => {
      const input = [[169, 0, 0, 0, 0]];

      const result = getMaxTickValue(input);
      expect(result).toEqual(300);
    });

    test('Handle startIndex and endIndex parameters', () => {
      const input = [
        [100, 200, 300],
        [400, 500, 600],
        [700, 800, 900],
      ];
      const result = getMaxTickValue(input, 1, 2);
      expect(result).toBeGreaterThan(0);
    });
  });

  describe('Test "hasWindow"', () => {
    test('Return true in browser environment', () => {
      const result = hasWindow();
      expect(result).toBe(true);
    });
  });

  describe('Test "getEllipsisText"', () => {
    test('Return ellipsis text when text length exceeds maxTextLength', () => {
      const text = 'This is a very long text that exceeds the maximum length';
      const maxTextLength = 20;
      const result = getEllipsisText({ text, maxTextLength });
      expect(result).toBe('This is a very lo...');
      expect(result.length).toBe(maxTextLength);
    });

    test('Return original text when text length is less than maxTextLength', () => {
      const text = 'Short text';
      const maxTextLength = 20;
      const result = getEllipsisText({ text, maxTextLength });
      expect(result).toBe('Short text');
    });

    test('Return ellipsis text when text length equals maxTextLength', () => {
      const text = 'Exactly twenty chars';
      const maxTextLength = 20;
      const result = getEllipsisText({ text, maxTextLength });
      expect(result).toBe('Exactly twenty ch...');
    });

    test('Handle empty string', () => {
      const text = '';
      const maxTextLength = 10;
      const result = getEllipsisText({ text, maxTextLength });
      expect(result).toBe('');
    });

    test('Handle very short maxTextLength', () => {
      const text = 'Hello';
      const maxTextLength = 3;
      const result = getEllipsisText({ text, maxTextLength });
      // When maxTextLength is 3, substring(0, 0) + "..." = "..."
      expect(result).toBe('...');
    });
  });

  describe('Test "getFormat"', () => {
    test('Return bytes formatter for BYTES unit', () => {
      const formatter = getFormat('bytes');
      // abbreviateNumber may return "1.02K" for 1024
      expect(formatter(1024)).toMatch(/^1\.?\d*K$/);
      expect(formatter(1048576)).toMatch(/^1\.?\d*M$/);
    });

    test('Return bytes formatter for BYTE unit', () => {
      const formatter = getFormat('byte');
      // abbreviateNumber may return "1.02K" for 1024
      expect(formatter(1024)).toMatch(/^1\.?\d*K$/);
    });

    test('Return count formatter for COUNT unit', () => {
      const formatter = getFormat('count');
      expect(formatter(123456)).toBe('123,456');
    });

    test('Return percent formatter for PERCENT unit', () => {
      const formatter = getFormat('percent');
      expect(formatter(50)).toBe('50%');
      expect(formatter(50.123)).toBe('50.12%');
    });

    test('Return time formatter for TIME unit', () => {
      const formatter = getFormat('time');
      expect(formatter(1000)).toBe('1sec');
      expect(formatter(5000)).toBe('5sec');
    });

    test('Return default formatter for unknown unit', () => {
      const formatter = getFormat('unknown');
      expect(formatter(123)).toBe('123');
    });
  });

  describe('Test "getTooltipStr"', () => {
    test('Generate tooltip HTML string with title and contents', () => {
      const title = 'Test Title';
      const contentsData = [
        { id: 'series1', value: '100', color: '#ff0000' },
        { id: 'series2', value: '200', color: '#00ff00' },
      ];
      const result = getTooltipStr(title, contentsData);

      expect(result).toContain('<table class="bb-tooltip">');
      expect(result).toContain('<th colspan="2">Test Title</th>');
      expect(result).toContain('series1');
      expect(result).toContain('series2');
      expect(result).toContain('100');
      expect(result).toContain('200');
      expect(result).toContain('#ff0000');
      expect(result).toContain('#00ff00');
    });

    test('Handle empty contents data', () => {
      const title = 'Empty Title';
      const contentsData: unknown[] = [];
      const result = getTooltipStr(title, contentsData);

      expect(result).toContain('<table class="bb-tooltip">');
      expect(result).toContain('<th colspan="2">Empty Title</th>');
      expect(result).toContain('<tbody>');
    });

    test('Handle multiple contents', () => {
      const title = 'Multiple Series';
      const contentsData = [
        { id: 'a', value: '1', color: '#000000' },
        { id: 'b', value: '2', color: '#111111' },
        { id: 'c', value: '3', color: '#222222' },
      ];
      const result = getTooltipStr(title, contentsData);

      expect(result).toContain('bb-tooltip-name-a');
      expect(result).toContain('bb-tooltip-name-b');
      expect(result).toContain('bb-tooltip-name-c');
    });
  });
});
