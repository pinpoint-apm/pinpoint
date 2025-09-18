import React from 'react';
import { Popover, PopoverClose, PopoverContent, PopoverTrigger, Separator } from '../ui';
import { ApplicationItem, ApplicationList, ApplicationVirtualList } from './ApplicationList';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { LuStar, LuStarOff } from 'react-icons/lu';
import { cn } from '../../lib/utils';
import { Toaster } from '../ui/toaster';
import { ListItemSkeleton, VirtualSearchList } from '../VirtualList';
import { RxCaretSort } from 'react-icons/rx';
import { ServerIcon } from './ServerIcon';
import { t } from 'i18next';
import { useToast } from '@pinpoint-fe/ui/src/lib';

export interface ApplicationCombinedListForCommonProps {
  favoriteList?: ApplicationType[];
  isFavoriteListLoading?: boolean;
  onClickFavorite?: (newFavoriteList: ApplicationType[]) => void;
  triggerClassName?: string;
  contentClassName?: string;
  open?: boolean;
  disabled?: boolean;
  selectedApplication?: ApplicationType | null;
  selectPlaceHolder?: string;
  addFavoriteMessage?: string;
  removeFavoriteMessage?: string;
  onClickApplication?: (application: ApplicationType) => void;
}

export const ApplicationCombinedListForCommon = ({
  favoriteList,
  isFavoriteListLoading,
  onClickFavorite,
  triggerClassName,
  contentClassName,
  open,
  disabled,
  selectedApplication,
  onClickApplication,
}: ApplicationCombinedListForCommonProps) => {
  const [isOpen, setIsOpen] = React.useState(open);
  const popoverContentRef = React.useRef<HTMLDivElement>(null);

  const [filterKeyword, setFilterKeyword] = React.useState('');
  const prevFilterKeywordRef = React.useRef(filterKeyword);

  const [filteredLists, setFilteredLists] = React.useState({
    favoriteList: [],
    applicationList: [],
  });

  // 마우스 움직임 여부를 확인하기 위한 상태
  const [isMouseMove, setIsMouseMove] = React.useState(false);
  // 마우스 이동으로 focus한 아이템의 정보를 저장하기 위한 상태
  const [mouseEnterInfo, setMouseEnterInfo] = React.useState<{
    id: 'favoriteList' | 'applicationList';
    index: number;
  }>({
    id: 'favoriteList',
    index: 0,
  });

  // 키보드 방향키로 이동하여 focus한 아이템의 정보를 저장하기 위한 상태
  const [focusInfo, setFocusInfo] = React.useState<{
    id: 'favoriteList' | 'applicationList'; // 각 favoriteList, applicationList virtualList를 구분하기 위한 값
    index: number;
  }>({
    id: 'favoriteList',
    index: 0,
  });

  const { toast } = useToast();
  const isFavoriteApplication = (application: ApplicationType) => {
    return favoriteList?.some((favoriteApp: ApplicationType) => {
      return (
        favoriteApp.applicationName === application.applicationName &&
        favoriteApp?.serviceType === application?.serviceType
      );
    });
  };

  const handleClickItem = (application: ApplicationType) => {
    onClickApplication?.(application);
  };

  const handleClickFavorite = async (
    e: React.MouseEvent,
    application: ApplicationType,
    option?: { disableToast: boolean },
  ) => {
    e.stopPropagation();

    const isExist = isFavoriteApplication(application);
    const newFavoriteList = isExist
      ? favoriteList?.filter(
          (app: ApplicationType) =>
            !(
              app.applicationName === application.applicationName &&
              app?.serviceType === application?.serviceType
            ),
        )
      : [...(favoriteList || []), application].sort((a, b) => {
          if (a.applicationName && b.applicationName) {
            return a.applicationName.localeCompare(b.applicationName);
          }
          return 1;
        });

    try {
      await onClickFavorite?.(newFavoriteList || []);

      !option?.disableToast &&
        toast({
          description: (
            <div className="flex items-center gap-1 text-xs">
              {isExist ? <LuStarOff /> : <LuStar className="fill-emerald-400 stroke-emerald-400" />}
              {isExist ? 'Removed from favorites.' : 'Added to favorites.'}
            </div>
          ),
          variant: 'small',
        });
    } catch (error) {
      !option?.disableToast &&
        toast({
          title: (error as Error).message || 'Failed to update favorites',
          description: 'Please try again',
          variant: 'destructive',
        });
    } finally {
      popoverContentRef.current?.focus();
    }
  };

  // isOpen이 변경되면 focusInfo를 초기화
  React.useEffect(() => {
    setIsMouseMove(false);

    if (favoriteList && favoriteList.length > 0) {
      setFocusInfo({ id: 'favoriteList', index: 0 });
    } else {
      setFocusInfo({ id: 'applicationList', index: 0 });
    }
  }, [isOpen, favoriteList]);

  React.useEffect(() => {
    // filterKeyword가 변경되었을 경우 focusInfo를 초기화
    if (prevFilterKeywordRef.current !== filterKeyword) {
      setIsMouseMove(false);
      if (filteredLists['favoriteList']?.length) {
        setFocusInfo({ id: 'favoriteList', index: 0 });
      } else if (filteredLists['applicationList']?.length) {
        setFocusInfo({ id: 'applicationList', index: 0 });
      }
      prevFilterKeywordRef.current = filterKeyword;
      return;
    }
    // filterKeyword가 변경되고 -> filteredLists가 변경되기 때문에 dependency array에 filteredLists?.favoriteList를 넣어줘야 함
  }, [filteredLists?.favoriteList]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    const favoriteListLength = filteredLists['favoriteList']?.length || 0;
    const applicationListLength = filteredLists['applicationList']?.length || 0;

    if (isMouseMove) {
      if (favoriteListLength > 0) {
        setFocusInfo({ id: 'favoriteList', index: 0 });
      } else {
        setFocusInfo({ id: 'applicationList', index: 0 });
      }
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      setFocusInfo((prev) => {
        const nowInfo = isMouseMove ? mouseEnterInfo : prev;

        // favoriteList 끝에 도달했을 경우
        if (nowInfo?.id === 'favoriteList' && nowInfo?.index >= favoriteListLength - 1) {
          if (applicationListLength > 0) {
            return { id: 'applicationList', index: 0 };
          } else {
            return { id: 'favoriteList', index: 0 };
          }
        }

        // applicationList 끝에 도달했을 경우
        if (nowInfo?.id === 'applicationList' && nowInfo?.index >= applicationListLength - 1) {
          if (favoriteListLength > 0) {
            return { id: 'favoriteList', index: 0 };
          } else {
            return { id: 'applicationList', index: 0 };
          }
        }

        return { id: nowInfo?.id, index: nowInfo?.index + 1 }; // 현재 list 내에서 다음 아이템으로 이동
      });
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setFocusInfo((prev) => {
        const nowInfo = isMouseMove ? mouseEnterInfo : prev;

        // applicationList의 처음에 도달했을 경우
        if (nowInfo?.id === 'applicationList' && nowInfo?.index <= 0) {
          if (favoriteListLength > 0) {
            return { id: 'favoriteList', index: favoriteListLength - 1 };
          } else {
            return { id: 'applicationList', index: applicationListLength - 1 };
          }
        }

        // favoriteList의 처음에 도달했을 경우
        if (nowInfo?.id === 'favoriteList' && nowInfo?.index <= 0) {
          if (applicationListLength > 0) {
            return { id: 'applicationList', index: applicationListLength - 1 };
          } else {
            return { id: 'favoriteList', index: favoriteListLength - 1 };
          }
        }

        return { id: nowInfo?.id, index: nowInfo?.index - 1 }; // 현재 list 내에서 이전 아이템으로 이동
      });
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const nowInfo = isMouseMove ? mouseEnterInfo : focusInfo;

      const clickedItem = nowInfo
        ? filteredLists[nowInfo?.id as 'favoriteList' | 'applicationList']?.[nowInfo?.index]
        : null;

      if (clickedItem) {
        handleClickItem(clickedItem as ApplicationType);
        setIsOpen(false);
      }
    }

    setIsMouseMove(false);
  };

  function getFilteredList(filteredList: any, id: 'favoriteList' | 'applicationList') {
    setFilteredLists((prev) => {
      const newLists = { ...prev };
      newLists[id] = filteredList;
      return newLists;
    });
  }

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
      <PopoverTrigger
        className={cn('w-80 min-w-80 border-b-1 text-sm disabled:opacity-50', triggerClassName)}
        disabled={disabled}
      >
        <div className="flex items-center w-full p-1 pt-2">
          {selectedApplication ? (
            <div className="flex items-center flex-1 gap-2 overflow-hidden group/applist-input">
              <ServerIcon className="w-6" application={selectedApplication} />
              <div className="truncate">{selectedApplication.applicationName}</div>
              <div
                className="flex-none hidden w-5 h-5 ml-auto cursor-pointer group-hover/applist-input:block"
                onClick={(e) => handleClickFavorite(e, selectedApplication, { disableToast: true })}
              >
                <LuStar
                  className={cn('opacity-50 pb-0.5', {
                    'fill-emerald-400 stroke-emerald-400 opacity-70':
                      isFavoriteApplication(selectedApplication),
                  })}
                />
              </div>
            </div>
          ) : (
            t('APP_SELECT.SELECT_YOUR_APP')
          )}
          <RxCaretSort className="w-4 h-4 ml-auto opacity-50" />
        </div>
      </PopoverTrigger>
      <PopoverContent
        className={cn('w-80 p-0 text-sm !z-[2001]', contentClassName)}
        onKeyDown={handleKeyDown}
        onMouseMove={() => {
          setIsMouseMove(true);
        }}
        ref={popoverContentRef}
      >
        <VirtualSearchList
          inputClassName="focus-visible:ring-0 border-none shadow-none"
          placeHolder={t('APP_SELECT.INPUT_APP_NAME_PLACE_HOLDER')}
          onChangeFilterKeyword={setFilterKeyword}
        >
          {(props) => {
            return (
              <div>
                <Separator />
                <div className="p-2 text-xs font-semibold">Favorite List</div>
                <div className="h-48">
                  {isFavoriteListLoading ? (
                    <ListItemSkeleton skeletonOption={{ viewBoxHeight: 192 }} />
                  ) : (
                    <ApplicationVirtualList
                      focusIndex={
                        isMouseMove
                          ? undefined
                          : focusInfo?.id === 'favoriteList'
                            ? focusInfo?.index
                            : -1
                      }
                      getFilteredList={(filteredList) => {
                        getFilteredList(filteredList, 'favoriteList');
                      }}
                      itemAs={PopoverClose}
                      list={favoriteList}
                      filterKeyword={props?.filterKeyword}
                      onClickItem={handleClickItem}
                      onMouseEnter={(idx, item) => {
                        setMouseEnterInfo({ id: 'favoriteList', index: idx });
                      }}
                      itemChild={(application) => {
                        return (
                          <>
                            <ApplicationItem {...application} />
                            <div
                              className="flex-none w-6 h-6 ml-auto cursor-pointer"
                              onClick={(e) => handleClickFavorite(e, application)}
                            >
                              <LuStar className=" fill-emerald-400 stroke-emerald-400" />
                            </div>
                          </>
                        );
                      }}
                    />
                  )}
                </div>
                <Separator />
                <div className="p-2 text-xs font-semibold">Application List</div>
                <ApplicationList
                  {...props}
                  focusIndex={
                    isMouseMove
                      ? undefined
                      : focusInfo?.id === 'applicationList'
                        ? focusInfo?.index
                        : -1
                  }
                  getFilteredList={(filteredList) =>
                    getFilteredList(filteredList, 'applicationList')
                  }
                  onClickItem={handleClickItem}
                  onMouseEnter={(idx, item) => {
                    setMouseEnterInfo({ id: 'applicationList', index: idx });
                  }}
                  itemAs={PopoverClose}
                  itemChild={(application) => {
                    const isFavorite = isFavoriteApplication(application);
                    return (
                      <>
                        <ApplicationItem {...application} />
                        <div
                          className={cn('ml-auto h-6 w-6 cursor-pointer flex-none', {
                            '[&>svg]:hover:fill-emerald-400 [&>svg]:hover:stroke-emerald-400':
                              !isFavorite,
                          })}
                          onClick={(e) => handleClickFavorite(e, application)}
                        >
                          <LuStar
                            className={cn({
                              'fill-emerald-400 stroke-emerald-400': isFavorite,
                            })}
                          />
                        </div>
                      </>
                    );
                  }}
                />
              </div>
            );
          }}
        </VirtualSearchList>
        <Toaster duration={1000} />
      </PopoverContent>
    </Popover>
  );
};
