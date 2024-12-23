import { convertParamsToQueryString } from './string';

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
});
