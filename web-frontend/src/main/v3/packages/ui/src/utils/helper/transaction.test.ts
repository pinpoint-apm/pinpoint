import {
  getTranscationListQueryString,
  getTransactionDetailQueryString,
  getTransactionTableUniqueKey,
  getTransactionDetailPathByTransactionId,
} from './transaction';

// Mock route helper
jest.mock('./route', () => ({
  getTransactionDetailPath: jest.fn(() => '/transaction/detail'),
}));

describe('Test transaction helper utils', () => {
  describe('Test "getTranscationListQueryString"', () => {
    test('Generate query string with all parameters', () => {
      const queryParam = {
        x1: 100.5,
        x2: 200.7,
        y1: 50.3,
        y2: 150.9,
        checkedLegends: ['success', 'failed'],
        agentId: 'agent-123',
      };
      const result = getTranscationListQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.x1).toBe(100);
      expect(decoded.x2).toBe(201);
      expect(decoded.y1).toBe(150); // Math.floor(150.9) = 150
      expect(decoded.y2).toBe(51); // Math.ceil(50.3) = 51
      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.dotStatus).toEqual(['success', 'failed']);
    });

    test('Handle y2 <= 0 case', () => {
      const queryParam = {
        x1: 100,
        x2: 200,
        y1: 50,
        y2: -10,
        checkedLegends: ['success'],
      };
      const result = getTranscationListQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.y1).toBe(0);
    });

    test('Handle missing agentId', () => {
      const queryParam = {
        x1: 100,
        x2: 200,
        y1: 50,
        y2: 150,
        checkedLegends: ['success'],
      };
      const result = getTranscationListQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.agentId).toBe('');
    });

    test('Handle empty checkedLegends', () => {
      const queryParam = {
        x1: 100,
        x2: 200,
        y1: 50,
        y2: 150,
        checkedLegends: [],
      };
      const result = getTranscationListQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.dotStatus).toEqual([]);
    });
  });

  describe('Test "getTransactionDetailQueryString"', () => {
    test('Generate query string with all parameters', () => {
      const queryParam = {
        agentId: 'agent-123',
        spanId: 'span-456',
        traceId: 'trace-789',
        focusTimestamp: 1234567890,
      };
      const result = getTransactionDetailQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.spanId).toBe('span-456');
      expect(decoded.traceId).toBe('trace-789');
      expect(decoded.focusTimestamp).toBe(1234567890);
    });

    test('Handle missing optional parameters', () => {
      const queryParam = {
        agentId: 'agent-123',
        spanId: '',
        traceId: 'trace-789',
        focusTimestamp: 0,
      };
      const result = getTransactionDetailQueryString(queryParam);
      const decoded = JSON.parse(decodeURI(result.split('=')[1]));

      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.spanId).toBe('');
      expect(decoded.traceId).toBe('trace-789');
      expect(decoded.focusTimestamp).toBe(0);
    });
  });

  describe('Test "getTransactionTableUniqueKey"', () => {
    test('Generate unique key with all fields', () => {
      const transaction = {
        traceId: 'trace-123',
        spanId: 'span-456',
        application: 'app-name',
      };
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('trace-123span-456app-name');
    });

    test('Generate unique key with path instead of application', () => {
      const transaction = {
        traceId: 'trace-123',
        spanId: 'span-456',
        path: '/api/endpoint',
      };
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('trace-123span-456/api/endpoint');
    });

    test('Handle missing traceId', () => {
      const transaction = {
        spanId: 'span-456',
        application: 'app-name',
      };
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('undefinedspan-456app-name');
    });

    test('Handle missing spanId', () => {
      const transaction = {
        traceId: 'trace-123',
        application: 'app-name',
      };
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('trace-123undefinedapp-name');
    });

    test('Handle missing application and path', () => {
      const transaction = {
        traceId: 'trace-123',
        spanId: 'span-456',
      };
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('trace-123span-456undefined');
    });

    test('Handle empty object', () => {
      const transaction = {};
      const result = getTransactionTableUniqueKey(transaction);
      expect(result).toBe('undefinedundefinedundefined');
    });
  });

  describe('Test "getTransactionDetailPathByTransactionId"', () => {
    test('Generate path from transaction ID', () => {
      const transactionId = 'agent-123^trace-456';
      const result = getTransactionDetailPathByTransactionId(transactionId);

      expect(result).toContain('/transaction/detail');
      expect(result).toContain('transactionInfo=');
      const queryString = result.split('?')[1];
      const decoded = JSON.parse(decodeURI(queryString.split('=')[1]));

      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.spanId).toBe('-1');
      expect(decoded.traceId).toBe('agent-123^trace-456');
      expect(decoded.focusTimestamp).toBe(0);
    });

    test('Handle transaction ID with whitespace', () => {
      const transactionId = '  agent-123^trace-456  ';
      const result = getTransactionDetailPathByTransactionId(transactionId);

      const queryString = result.split('?')[1];
      const decoded = JSON.parse(decodeURI(queryString.split('=')[1]));

      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.traceId).toBe('agent-123^trace-456');
    });

    test('Handle transaction ID without caret separator', () => {
      const transactionId = 'agent-123';
      const result = getTransactionDetailPathByTransactionId(transactionId);

      const queryString = result.split('?')[1];
      const decoded = JSON.parse(decodeURI(queryString.split('=')[1]));

      expect(decoded.agentId).toBe('agent-123');
      expect(decoded.traceId).toBe('agent-123');
    });
  });
});
