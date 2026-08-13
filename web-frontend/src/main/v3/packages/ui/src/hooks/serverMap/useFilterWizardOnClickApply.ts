import { useAtomValue } from 'jotai';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import {
  BASE_PATH,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import {
  getFilteredMapQueryString,
  getFilteredMapPath,
  getFilterTargetApplication,
  findLinkOfApplications,
} from '@pinpoint-fe/ui/src/utils';

export function useFilterWizardOnClickApply<
  T extends GetServerMap.LinkData | FilteredMap.LinkData,
>({
  from,
  to,
  parsedHint,
  serviceName,
}: {
  from: string;
  to: string;
  parsedHint?: FilteredMap.Hint; // filteredMap에서만 존재
  /**
   * filteredMap 경로에 실을 service 이름. servicemap 계열 화면에서만 주어진다.
   * (filteredMap은 새 탭으로 열리므로 어떤 service를 보던 중이었는지 URL에 남아야 한다.)
   */
  serviceName?: string;
}) {
  const serverMapData = useAtomValue(serverMapDataAtom);

  return (filterStates: FilteredMap.FilterState[]) => {
    const filterState = filterStates[filterStates.length - 1];
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let addedHint = {} as any;
    let soureIsWas;

    // 링크에 걸린 필터다. 출발지가 WAS인지에 따라 기준 application(경로에 실릴 것)과 hint가
    // 갈리므로 map에서 그 링크를 찾아야 한다.
    //
    // 링크를 못 찾으면 soureIsWas가 undefined로 남아 **도착지**가 기준이 되고 hint도 비어,
    // 같은 링크인데 servermap과 servicemap의 URL이 달라진다. link key는 형식이 map API마다
    // 다르므로(servermap 2단, servicemap 3단) 형식에 무관한 매처로 찾는다.
    if (!filterState?.applicationName) {
      const link = findLinkOfApplications(
        serverMapData?.applicationMapData?.linkDataArray as T[],
        {
          applicationName: filterState?.fromApplication,
          serviceType: filterState?.fromServiceType,
        },
        { applicationName: filterState?.toApplication, serviceType: filterState?.toServiceType },
      );

      if (link) {
        soureIsWas = link?.sourceInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER;
        addedHint =
          soureIsWas && link?.targetInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER
            ? {
                [link?.targetInfo?.applicationName]: link?.filter?.outRpcList,
              }
            : {};
      }
    }

    // 기준 application이 없으면 filteredMap은 조회 자체를 못 한다. 빈 화면을 새 탭으로 열어
    // 보여주는 대신 아무 것도 하지 않는다. (servicemap의 service group 노드·링크)
    if (!getFilterTargetApplication(filterState, soureIsWas)) {
      return;
    }

    window.open(
      `${BASE_PATH}${getFilteredMapPath(filterState, soureIsWas, serviceName)}?from=${from}&to=${to}${getFilteredMapQueryString(
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
