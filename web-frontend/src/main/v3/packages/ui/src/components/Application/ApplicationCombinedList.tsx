import { Popover, PopoverClose, PopoverContent, PopoverTrigger, Separator } from '../ui';
import { ApplicationItem, ApplicationList, ApplicationVirtualList } from './ApplicationList';
import { APP_SETTING_KEYS, ApplicationType } from '@pinpoint-fe/constants';
import { LuStar, LuStarOff } from 'react-icons/lu';
import { cn } from '../../lib/utils';
import { getServerIconPath } from '@pinpoint-fe/utils';
import { Toaster } from '../ui/toaster';
import { useToast } from '../../lib/use-toast';
import { VirtualSearchList } from '../VirtualList';
import { useLocalStorage } from '@pinpoint-fe/hooks';
import { RxCaretSort } from 'react-icons/rx';

export interface ApplicationCombinedListProps {
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

export const ApplicationCombinedList = ({
  triggerClassName,
  contentClassName,
  open,
  disabled,
  selectedApplication,
  selectPlaceHolder = 'Select your application.',
  addFavoriteMessage = 'Added to favorites.',
  removeFavoriteMessage = 'Removed from favorites.',
  onClickApplication,
}: ApplicationCombinedListProps) => {
  const { toast } = useToast();
  const [favoriteList, setFavoriteList] = useLocalStorage<ApplicationType[]>(
    APP_SETTING_KEYS.FAVORLIITE_APPLICATION_LIST,
    [],
  );

  const handleClickItem = (application: ApplicationType) => {
    onClickApplication?.(application);
  };

  const handleClickFavorite = (e: React.MouseEvent, application: ApplicationType) => {
    e.stopPropagation();

    const isExist = favoriteList.find((favoriteApp: ApplicationType) => {
      return favoriteApp.applicationName === application.applicationName;
    });

    if (isExist) {
      setFavoriteList(
        favoriteList.filter(
          (favoriteApp: ApplicationType) =>
            favoriteApp.applicationName !== application.applicationName,
        ),
      );
      toast({
        description: (
          <div className="flex items-center gap-2 text-xs">
            <LuStarOff />
            {removeFavoriteMessage}
          </div>
        ),
        variant: 'small',
      });
    } else {
      setFavoriteList(
        [...favoriteList, application].sort((a, b) => {
          if (a.applicationName && b.applicationName) {
            return a.applicationName.localeCompare(b.applicationName);
          }
          return 1;
        }),
      );
      toast({
        description: (
          <div className="flex items-center gap-2 text-xs">
            <LuStar className="fill-emerald-400 stroke-emerald-400" />
            {addFavoriteMessage}
          </div>
        ),
        variant: 'small',
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
            <div className="flex items-center gap-2 overflow-hidden">
              <img
                width={24}
                height="auto"
                alt={'application image'}
                src={getServerIconPath(selectedApplication)}
              />
              <div className="truncate">{selectedApplication.applicationName}</div>
            </div>
          ) : (
            selectPlaceHolder
          )}
          <RxCaretSort className="w-4 h-4 ml-auto opacity-50" />
        </div>
      </PopoverTrigger>
      <PopoverContent className={cn('w-80 p-0 text-sm !z-[2001]', contentClassName)}>
        <VirtualSearchList
          inputClassName="focus-visible:ring-0 border-none shadow-none"
          placeHolder="Input application name"
        >
          {(props) => {
            return (
              <div>
                <Separator />
                <div className="p-2 text-xs font-semibold">Favorite List</div>
                <div className="h-48">
                  <ApplicationVirtualList
                    itemAs={PopoverClose}
                    list={favoriteList}
                    filterKeyword={props?.filterKeyword}
                    onClickItem={handleClickItem}
                    itemChild={(app) => {
                      return (
                        <>
                          <img
                            width={20}
                            height="auto"
                            alt={'application image'}
                            src={getServerIconPath(app)}
                          />
                          <div className="truncate">{app.applicationName}</div>
                          <div
                            className="flex-none w-6 h-6 ml-auto cursor-pointer"
                            onClick={(e) => handleClickFavorite(e, app)}
                          >
                            <LuStar className=" fill-emerald-400 stroke-emerald-400" />
                          </div>
                        </>
                      );
                    }}
                  />
                </div>
                <Separator />
                <div className="p-2 text-xs font-semibold">Application List</div>
                <ApplicationList
                  {...props}
                  onClickItem={handleClickItem}
                  itemAs={PopoverClose}
                  itemChild={(application) => {
                    const isFavorite = favoriteList.find(
                      (favorite) => favorite.applicationName === application.applicationName,
                    );
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
