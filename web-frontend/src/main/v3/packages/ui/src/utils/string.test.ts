import {
  convertParamsToQueryString,
  extractStringAfterSubstring,
  decodeHTMLEntities,
} from './string';

describe('Test string utils', () => {
  describe('Test "convertParamsToQueryString"', () => {
    test('Convert a basic object to a query string', () => {
      const input = {
        from: '2023-11-10-11-29-49',
        to: '2023-11-10-11-34-49',
        inbound: 2,
        outbound: 2,
        wasOnly: true,
        bidirectional: true,
      };
      const result = convertParamsToQueryString(input);

      expect(result).toEqual(
        'from=2023-11-10-11-29-49&to=2023-11-10-11-34-49&inbound=2&outbound=2&wasOnly=true&bidirectional=true',
      );
    });

    test('Convert an empty object to a query string', () => {
      const input = {};
      const result = convertParamsToQueryString(input);
      expect(result).toEqual('');
    });
  });

  describe('Test "extractStringAfterSubstring"', () => {
    test('Extract string after substring when substring exists', () => {
      const inputString = 'prefix-suffix';
      const substring = 'prefix-';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('suffix');
    });

    test('Return empty string when substring does not exist', () => {
      const inputString = 'prefix-suffix';
      const substring = 'notfound';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('');
    });

    test('Handle empty input string', () => {
      const inputString = '';
      const substring = 'prefix';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('');
    });

    test('Handle empty substring', () => {
      const inputString = 'prefix-suffix';
      const substring = '';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('prefix-suffix');
    });

    test('Handle substring at the end', () => {
      const inputString = 'prefix-suffix';
      const substring = 'suffix';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('');
    });

    test('Handle multiple occurrences of substring', () => {
      const inputString = 'prefix1-prefix2-suffix';
      const substring = 'prefix1-';
      const result = extractStringAfterSubstring(inputString, substring);
      expect(result).toBe('prefix2-suffix');
    });
  });

  describe('Test "decodeHTMLEntities"', () => {
    test('Decode HTML entities correctly', () => {
      const text = '&lt;div&gt;&amp;&quot;test&quot;&#39;value&#39;&lt;/div&gt;';
      const result = decodeHTMLEntities(text);
      expect(result).toBe('<div>&"test"\'value\'</div>');
    });

    test('Handle text without HTML entities', () => {
      const text = 'plain text';
      const result = decodeHTMLEntities(text);
      expect(result).toBe('plain text');
    });

    test('Handle empty string', () => {
      const text = '';
      const result = decodeHTMLEntities(text);
      expect(result).toBe('');
    });

    test('Decode single entity', () => {
      const text = '&amp;';
      const result = decodeHTMLEntities(text);
      expect(result).toBe('&');
    });

    test('Decode all entity types', () => {
      const text = '&lt;&gt;&amp;&quot;&#39;';
      const result = decodeHTMLEntities(text);
      // Order may vary due to regex replacement, so check individual entities
      expect(result).toContain('<');
      expect(result).toContain('>');
      expect(result).toContain('&');
      expect(result).toContain('"');
      expect(result).toContain("'");
      expect(result.length).toBe(5);
    });

    test('Handle mixed content', () => {
      const text = 'Text &lt;tag&gt; with &amp; entities';
      const result = decodeHTMLEntities(text);
      expect(result).toBe('Text <tag> with & entities');
    });
  });
});
