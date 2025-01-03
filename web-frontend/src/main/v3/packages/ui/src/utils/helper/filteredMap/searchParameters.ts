import { FilteredMapType as FilteredMap } from '@pinpoint-fe/ui/constants';
import { isEmpty } from '../../object';

export const parseFilterStateFromQueryString = (queryString: string): FilteredMap.FilterState[] => {
  const parsedFilters: FilteredMap.SearchParameters[] = queryString
    ? JSON.parse(queryString)
    : [{ ie: null }];
  return parsedFilters?.map((filter) => ({
    fromApplication: filter.fa,
    fromServiceType: filter.fst,
    toApplication: filter.ta,
    toServiceType: filter.tst,
    transactionResult: filter.ie,
    // if single node
    applicationName: filter.a,
    serviceType: filter.st,
    agentName: filter.an,
    // settings by user
    responseFrom: filter.rf,
    responseTo: filter.rt,
    url: filter.url,
    fromAgentName: filter.fan,
    toAgentName: filter.tan,
  }));
};

export const getFilteredMapQueryString = ({
  filterStates,
  hint,
}: {
  filterStates: Parameters<typeof getFilterQueryParameter>[0];
  hint: Parameters<typeof getHintQueryParameter>[0];
}) => {
  const filterQueryString = JSON.stringify(getFilterQueryParameter(filterStates));
  const hintQueryString = JSON.stringify(getHintQueryParameter(hint));
  // return
  return encodeURI(`&filter=${filterQueryString}&hint=${hintQueryString}`);
};

export const getFilterQueryParameter = (filterStates: FilteredMap.FilterState[]) => {
  const queryParameters = filterStates.map((filter) => ({
    fa: filter.fromApplication,
    fst: filter.fromServiceType,
    ta: filter.toApplication,
    tst: filter.toServiceType,
    ie: filter.transactionResult,
    // if single node
    a: filter.applicationName,
    st: filter.serviceType,
    an: filter.agentName,
    // settings by user
    rf: filter.responseFrom,
    rt: filter.responseTo,
    url: filter.url,
    fan: filter.fromAgentName,
    tan: filter.toAgentName,
  }));

  return queryParameters;
};

// export const getFilterQueryString = (...args: Parameters<typeof getFilterQueryParameter>) => {
//   return JSON.stringify(getFilterQueryParameter(...args));
// };

export const getHintQueryParameter = ({
  currHint = {},
  addedHint,
}: {
  currHint?: FilteredMap.Hint;
  addedHint: FilteredMap.FilterTargetRpcList;
}): FilteredMap.Hint => {
  if (addedHint) {
    if (isEmpty(currHint)) {
      return makeHintUrlFormat(addedHint);
    } else {
      const currHintOnServerFormat = makeServerFormat(currHint);
      const mergedFormat = mergeHint(currHintOnServerFormat, addedHint);

      return makeHintUrlFormat(mergedFormat);
    }
  } else {
    return currHint;
  }
};

// export const getHintQueryString = (...args: Parameters<typeof getHintQueryParameter>) => {
//   return JSON.stringify(getHintQueryParameter(...args));
// };

/**
 * addedHint: {
 *   app1: [{rpc: dasd, rpcServiceTypeCode: 1234}, {rpc: asdsaf, rpcServiceTypeCode: 5678}]
 *   app2: [{rpc: dasd, rpcServiceTypeCode: 1234}, {rpc: asdsaf, rpcServiceTypeCode: 5678}]
 * }
 *
 * return format
 * {
 *   app1: [dsad, 1234, asdsaf, 5678],
 *   app2: [dsad, 1234, asdsaf, 5678]
 * }
 * */
/* eslint-disable */
const makeHintUrlFormat = (addedHint: FilteredMap.FilterTargetRpcList): FilteredMap.Hint => {
  return Object.entries(addedHint).reduce(
    (acc: FilteredMap.Hint, [key, value]: [string, { [key: string]: any }[]]) => {
      return {
        ...acc,
        [key]: value.map((obj: { [key: string]: any }) => Object.values(obj)).flat(),
      };
    },
    {} as FilteredMap.Hint,
  );
};

/**
 * urlHint: {
 *   app1: [dasd, 1234, asdsaf, 5678],
 *   app2: [dasd, 1234, asdsaf, 5678]
 * }
 *
 * return format
 * {
 *   app1: [{rpc: dasd, rpcCode: 1234}, {rpc: asdsaf, rpcCode: 5678}]
 *   app2: [{rpc: dasd, rpcCode: 1234}, {rpc: asdsaf, rpcCode: 5678}]
 * }
 */
/* eslint-disable */
const makeServerFormat = (urlHint: FilteredMap.Hint): FilteredMap.FilterTargetRpcList => {
  return Object.entries(urlHint).reduce(
    (acc: FilteredMap.FilterTargetRpcList, [key, value]: [string, any[]]) => {
      return {
        ...acc,
        [key]: value.reduce((acc2: { [key: string]: any }[], curr: any, i: number) => {
          return i % 2
            ? ((acc2[acc2.length - 1].rpcServiceTypeCode = curr), acc2)
            : [...acc2, { rpc: curr }];
        }, []),
      };
    },
    {} as FilteredMap.FilterTargetRpcList,
  );
};
/* eslint-disable */
const mergeHint = (
  currHint: FilteredMap.FilterTargetRpcList,
  addedHint: FilteredMap.FilterTargetRpcList,
): FilteredMap.FilterTargetRpcList => {
  const mergedKeys = [...new Set<string>([...Object.keys(currHint), ...Object.keys(addedHint)])];
  const uniqueKeys = mergedKeys.filter(
    (key: string) => !(currHint.hasOwnProperty(key) && addedHint.hasOwnProperty(key)),
  );

  return mergedKeys.reduce((acc: FilteredMap.FilterTargetRpcList, k: string) => {
    const key = k as keyof FilteredMap.FilterTargetRpcList;
    return {
      ...acc,
      [key]: uniqueKeys.includes(key)
        ? currHint[key] || addedHint[key]
        : [...(currHint[key] as any), ...(addedHint[key] as any)].filter(
            (
              { rpc: rpc1, rpcServiceTypeCode: rpcCode1 },
              i: number,
              arr: { [key: string]: any }[],
            ) => {
              return !arr
                .slice(0, i)
                .some(
                  ({ rpc: rpc2, rpcServiceCode: rpcCode2 }) =>
                    rpc1 === rpc2 && rpcCode1 === rpcCode2,
                );
            },
          ),
    };
  }, {} as FilteredMap.FilterTargetRpcList);
};
