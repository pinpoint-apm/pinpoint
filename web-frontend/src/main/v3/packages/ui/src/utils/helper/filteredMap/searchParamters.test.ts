import { parseFilterStateFromQueryString, getFilteredMapQueryString } from './searchParameters';
import { FilteredMapType as FilteredMap } from '@pinpoint-fe/ui/constants';

describe('Test FilteredMap helper utils', () => {
  describe('Test "parseFilterStateFromQueryString"', () => {
    test('Parse queryString to FilteredMap.SearchParameters[]', () => {
      const input =
        '[{"fa":"","fst":"","ta":"","tst":"","ie":null,"a":"applicationName","st":"serviceType","an":"","rf":0,"rt":"max","url":"","fan":"","tan":""}]';
      const result = parseFilterStateFromQueryString(input);
      expect(result).toEqual([
        {
          fromApplication: '',
          fromServiceType: '',
          toApplication: '',
          toServiceType: '',
          transactionResult: null,
          applicationName: 'applicationName',
          serviceType: 'serviceType',
          agentName: '',
          responseFrom: 0,
          responseTo: 'max',
          url: '',
          fromAgentName: '',
          toAgentName: '',
        },
      ]);
    });

    test('Parse queryString to FilteredMap.SearchParameters[]', () => {
      const input =
        '[{"fa":"","fst":"","ta":"","tst":"","ie":null,"a":"applicationName","st":"serviceType","an":"","rf":0,"rt":"max","url":"","fan":"","tan":""},{"fa":"applicationName","fst":"serviceType","ta":"toApplication","tst":"toServiceType","ie":null,"a":"","st":"","an":"","rf":0,"rt":"max","url":"","fan":"","tan":""}]';
      const result = parseFilterStateFromQueryString(input);
      expect(result).toEqual([
        {
          fromApplication: '',
          fromServiceType: '',
          toApplication: '',
          toServiceType: '',
          transactionResult: null,
          applicationName: 'applicationName',
          serviceType: 'serviceType',
          agentName: '',
          responseFrom: 0,
          responseTo: 'max',
          url: '',
          fromAgentName: '',
          toAgentName: '',
        },
        {
          fromApplication: 'applicationName',
          fromServiceType: 'serviceType',
          toApplication: 'toApplication',
          toServiceType: 'toServiceType',
          transactionResult: null,
          applicationName: '',
          serviceType: '',
          agentName: '',
          responseFrom: 0,
          responseTo: 'max',
          url: '',
          fromAgentName: '',
          toAgentName: '',
        },
      ]);
    });
  });

  describe('Test "getFilteredMapQueryString"', () => {
    test('Abbreviate Numbers with Units Array (e.g., ["ms", "sec"]', () => {
      const filterState = {
        filterStates: [
          {
            fromApplication: '',
            fromServiceType: '',
            toApplication: '',
            toServiceType: '',
            transactionResult: null,
            applicationName: 'applicationName',
            serviceType: 'serviceType',
            agentName: '',
            responseFrom: 0,
            responseTo: 'max',
            url: '',
            fromAgentName: '',
            toAgentName: '',
            agents: ['agent'],
          },
        ],
        hint: {
          addedHint: {},
        },
      };
      const result = getFilteredMapQueryString(filterState);
      expect(result).toEqual(
        '&filter=%5B%7B%22fa%22:%22%22,%22fst%22:%22%22,%22ta%22:%22%22,%22tst%22:%22%22,%22ie%22:null,%22a%22:%22applicationName%22,%22st%22:%22serviceType%22,%22an%22:%22%22,%22rf%22:0,%22rt%22:%22max%22,%22url%22:%22%22,%22fan%22:%22%22,%22tan%22:%22%22%7D%5D&hint=%7B%7D',
      );
      const filterState2 = {
        filterStates: [
          {
            fromApplication: 'app1',
            fromServiceType: 'NODE',
            toApplication: 'app2',
            toServiceType: 'VERTX',
            transactionResult: null,
            applicationName: '',
            serviceType: '',
            agentName: '',
            responseFrom: 0,
            responseTo: 'max',
            url: '',
            fromAgentName: '',
            toAgentName: '',
          },
          {
            fromApplication: '',
            fromServiceType: '',
            toApplication: '',
            toServiceType: '',
            transactionResult: null,
            applicationName: 'app3',
            serviceType: 'SPRING_BOOT',
            agentName: '',
            responseFrom: 0,
            responseTo: 'max',
            url: '',
            fromAgentName: '',
            toAgentName: '',
          },
          {
            fromApplication: 'app2',
            fromServiceType: 'VERTX',
            toApplication: 'app3',
            toServiceType: 'SPRING_BOOT',
            transactionResult: null,
            applicationName: '',
            serviceType: '',
            agentName: '',
            responseFrom: 0,
            responseTo: 'max',
            url: '',
            fromAgentName: '',
            toAgentName: '',
          },
        ],
        hint: {
          currHint: {
            app2: [],
          } as FilteredMap.Hint,
          addedHint: {
            app3: [],
          } as FilteredMap.FilterTargetRpcList,
        },
      };
      const result2 = getFilteredMapQueryString(filterState2);
      expect(result2).toEqual(
        '&filter=%5B%7B%22fa%22:%22app1%22,%22fst%22:%22NODE%22,%22ta%22:%22app2%22,%22tst%22:%22VERTX%22,%22ie%22:null,%22a%22:%22%22,%22st%22:%22%22,%22an%22:%22%22,%22rf%22:0,%22rt%22:%22max%22,%22url%22:%22%22,%22fan%22:%22%22,%22tan%22:%22%22%7D,%7B%22fa%22:%22%22,%22fst%22:%22%22,%22ta%22:%22%22,%22tst%22:%22%22,%22ie%22:null,%22a%22:%22app3%22,%22st%22:%22SPRING_BOOT%22,%22an%22:%22%22,%22rf%22:0,%22rt%22:%22max%22,%22url%22:%22%22,%22fan%22:%22%22,%22tan%22:%22%22%7D,%7B%22fa%22:%22app2%22,%22fst%22:%22VERTX%22,%22ta%22:%22app3%22,%22tst%22:%22SPRING_BOOT%22,%22ie%22:null,%22a%22:%22%22,%22st%22:%22%22,%22an%22:%22%22,%22rf%22:0,%22rt%22:%22max%22,%22url%22:%22%22,%22fan%22:%22%22,%22tan%22:%22%22%7D%5D&hint=%7B%22app2%22:%5B%5D,%22app3%22:%5B%5D%7D',
      );
    });
  });
});
