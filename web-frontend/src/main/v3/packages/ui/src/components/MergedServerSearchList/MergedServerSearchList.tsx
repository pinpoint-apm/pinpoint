import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/constants';
import { VirtualList, VirtualSearchList } from '../VirtualList';

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
                  return (
                    <>
                      <div className="flex items-center justify-between text-xs">
                        <div className="truncate">{item.applicationName}</div>
                        <span>{item.totalCount}</span>
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
