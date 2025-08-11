import { useAtomValue } from 'jotai';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import {
  BASE_PATH,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { getFilteredMapQueryString, getFilteredMapPath } from '@pinpoint-fe/ui/src/utils';

export function useFilterWizardOnClickApply<
  T extends GetServerMap.LinkData | FilteredMap.LinkData,
>({
  from,
  to,
  parsedHint,
}: {
  from: string;
  to: string;
  parsedHint?: FilteredMap.Hint; // filteredMap에서만 존재
}) {
  const serverMapData = useAtomValue(serverMapDataAtom);

  return (filterStates: FilteredMap.FilterState[]) => {
    const filterState = filterStates[filterStates.length - 1];
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let addedHint = {} as any;
    let soureIsWas;

    if (!filterState.applicationName) {
      const link = (serverMapData?.applicationMapData.linkDataArray as T[]).find(
        (l) =>
          l.key ===
          `${filterState.fromApplication}^${filterState.fromServiceType}~${filterState.toApplication}^${filterState.toServiceType}`,
      );
      if (link) {
        soureIsWas = link.sourceInfo.isWas;
        addedHint =
          link.sourceInfo.isWas && link.targetInfo.isWas
            ? {
                [link.targetInfo.applicationName]: link.filter?.outRpcList,
              }
            : {};
      }
    }

    window.open(
      `${BASE_PATH}${getFilteredMapPath(filterState, soureIsWas)}?from=${from}&to=${to}${getFilteredMapQueryString(
        {
          filterStates,
          hint: {
            currHint: parsedHint || {},
            addedHint,
          },
        },
      )}`,
      '_blank',
    );
  };
}
