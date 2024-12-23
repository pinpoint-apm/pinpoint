import React from 'react';
import { useAtom } from 'jotai';
import { useNavigate } from 'react-router-dom';
import { getServerMapPath, getServerImagePath, getRealtimePath } from '@pinpoint-fe/utils';
import { serverMapCurrentTargetAtom } from '@pinpoint-fe/ui/atoms';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/hooks';
import {
  ApplicationCombinedList,
  DatetimePicker,
  DatetimePickerChangeHandler,
  HelpPopover,
  MainHeader,
  Realtime,
  withInitialFetch,
} from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';
import { PiTreeStructureDuotone } from 'react-icons/pi';

export interface RealtimePageProps {}

export const RealtimePage = ({}: RealtimePageProps) => {
  const navigate = useNavigate();
  const { application, searchParameters } = useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  // const [setCurrentServer] = useAtom(curr entServerAtom);
  // const sizes = useAtomValue(chartsBoardSizesAtom);

  React.useEffect(() => {
    if (application) {
      setServerMapCurrentTarget({
        ...application,
        imgPath: getServerImagePath(application),
        type: 'node',
      });
    } else {
      setServerMapCurrentTarget(undefined);
    }
  }, [application?.applicationName, application?.serviceType]);

  // const handleClickNode = ({ label, type, imgPath }: MergedNode) => {
  //   setServerMapCurrentTarget({
  //     applicationName: label,
  //     serviceType: type,
  //     imgPath: imgPath!,
  //   });
  //   setCurrentServer(undefined);
  //   setScatterData(undefined);
  // };

  const handleChangeDateRagePicker = React.useCallback(
    (({ isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePath(application!)}`);
      } else {
        navigate(`${getServerMapPath(application!)}`);
      }
    }) as DatetimePickerChangeHandler,
    [application],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <div className="flex items-center gap-1">
              Servermap
              <HelpPopover helpKey="HELP_VIEWER.SERVER_MAP" />
            </div>
          </div>
        }
      >
        <ApplicationCombinedList
          selectedApplication={application}
          onClickApplication={(application) => navigate(getServerMapPath(application))}
        />
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              isRealtime
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
            />
          )}
        </div>
      </MainHeader>
      {serverMapCurrentTarget && <Realtime />}
    </div>
  );
};

export default withInitialFetch((props: RealtimePageProps) =>
  getLayoutWithSideNavigation(<RealtimePage {...props} />),
);
