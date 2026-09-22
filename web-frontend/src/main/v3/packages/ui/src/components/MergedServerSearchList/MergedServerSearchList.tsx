import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { VirtualList, VirtualSearchList } from '../VirtualList';
import { getTimeSeriesApdexInfo } from '@pinpoint-fe/ui/src/utils';
import { colorMap, getApdexGrade } from '@pinpoint-fe/server-map';
import { ApdexScoreValue } from '../ApdexScore/ApdexScoreValue';

export interface MergedServerSearchListProps {
  list?: (GetServerMap.NodeData | FilteredMap.NodeData)[];
  onClickItem?: (nodeData: GetServerMap.NodeData | FilteredMap.NodeData) => void;
}

export const MergedServerSearchList = ({ list = [], onClickItem }: MergedServerSearchListProps) => {
  const handleClickItem: MergedServerSearchListProps['onClickItem'] = (nodeData) => {
    onClickItem?.(nodeData);
  };

  return list.length > 0 ? (
    <div className="text-xs px-4 py-2.5 border-b">
      <VirtualSearchList
        inputContainerClassName="h-8 border rounded mb-2"
        inputClassName="focus-visible:ring-0 border-none shadow-none text-xs  h-full"
        placeHolder={'input query'}
      >
        {(props) => {
          return (
            <div className="h-56">
              <VirtualList
                list={list}
                filterKey="applicationName"
                filterKeyword={props.filterKeyword}
                itemClassName="block cursor-pointer"
                onClickItem={(item) => handleClickItem(item)}
                itemChild={(item) => {
                  const MAX_CHART_WIDTH = 96;
                  const timeSeriesApdexInfo = getTimeSeriesApdexInfo(item);
                  return (
                    <>
                      <div className="flex items-center justify-between text-xs">
                        <div className="truncate">{item.applicationName}</div>
                        <div className="flex items-center gap-2">
                          {/* ChartsBoard·service group 목록과 같은 규칙으로 보여준다. 여기만
                              반올림하면 0.937이 이 목록에서는 0.94, 다른 화면에서는 0.93이 된다. */}
                          <ApdexScoreValue apdex={item.apdex} />
                          <div className={'w-auto flex justify-between'}>
                            {timeSeriesApdexInfo.map((score, index) => {
                              const grade = getApdexGrade(score);
                              const color = colorMap[grade] || '#cccccc';
                              return (
                                <div
                                  key={index}
                                  style={{
                                    width: `${MAX_CHART_WIDTH / timeSeriesApdexInfo.length || 0}px`,
                                    backgroundColor: color,
                                    height: '6px',
                                  }}
                                />
                              );
                            })}
                          </div>
                          <span className="min-w-10 text-end">{item.totalCount}</span>
                        </div>
                      </div>
                    </>
                  );
                }}
              />
            </div>
          );
        }}
      </VirtualSearchList>
      <div className="flex items-center justify-between pr-1 mt-3">
        <div>{list.length} application</div>
        <div>{list.reduce((acc, curr) => curr.totalCount + acc, 0)}</div>
      </div>
    </div>
  ) : null;
};
