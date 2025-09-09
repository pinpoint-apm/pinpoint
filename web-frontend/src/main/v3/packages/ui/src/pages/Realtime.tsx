import React from 'react';
import { useAtom } from 'jotai';
import { useNavigate } from 'react-router-dom';
import { getServerMapPath, getServerImagePath, getRealtimePath } from '@pinpoint-fe/ui/src/utils';
import { serverMapCurrentTargetAtom } from '@pinpoint-fe/ui/src/atoms';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  Configuration,
  DatetimePicker,
  DatetimePickerChangeHandler,
  HelpPopover,
  MainHeader,
  Realtime,
} from '@pinpoint-fe/ui';
import { PiTreeStructureDuotone } from 'react-icons/pi';

export interface RealtimePageProps {
  configuration?: Configuration & Record<string, string>;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const RealtimePage = ({
  ApplicationList = ApplicationCombinedList,
  configuration,
}: RealtimePageProps) => {
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
        <ApplicationList
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
      {serverMapCurrentTarget && <Realtime configuration={configuration} />}
    </div>
  );
};
