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
    }
  };

  return (
    <Popover defaultOpen={open}>
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
      <PopoverContent className={cn('w-80 p-0 text-sm !z-[2001]', contentClassName)}>
        <VirtualSearchList
          inputClassName="focus-visible:ring-0 border-none shadow-none"
          placeHolder={t('APP_SELECT.INPUT_APP_NAME_PLACE_HOLDER')}
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
                      itemAs={PopoverClose}
                      list={favoriteList}
                      filterKeyword={props?.filterKeyword}
                      onClickItem={handleClickItem}
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
                  onClickItem={handleClickItem}
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
