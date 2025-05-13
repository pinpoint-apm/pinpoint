import React from 'react';
import { Popover, PopoverContent, PopoverTrigger } from '../../components/ui/popover';
import { PiQuestionFill, PiInfo } from 'react-icons/pi';
import {
  FaCircle,
  FaPlus,
  FaDownload,
  FaExpandArrowsAlt,
  FaArrowAltCircleDown,
  FaExclamationCircle,
  FaTimesCircle,
  FaAngry,
  FaFrown,
  FaMeh,
  FaSmile,
  FaSmileBeam,
  FaCalendarAlt,
  FaClock,
  FaRegHandPointer,
} from 'react-icons/fa';
import { BsGearFill } from 'react-icons/bs';
import * as PopoverPrimitive from '@radix-ui/react-popover';
import { Trans, useTranslation } from 'react-i18next';
import { Separator } from '../../components/ui/separator';
import { BiSolidServer } from 'react-icons/bi';
import { PiHardDriveFill } from 'react-icons/pi';
import HeatmapVisualMapIcon from './HeatmapVisualMapIcon';

export type HelpContent = {
  TITLE?: string;
  DESC?: string;
  CATEGORY?: {
    TITLE?: string;
    DESC?: string;
    ITEMS?: {
      NAME?: string;
      DESC?: string;
    }[];
  }[];
};

const components = {
  FaCircle: <FaCircle />,
  FaPlus: <FaPlus />,
  FaDownload: <FaDownload />,
  FaExpandArrowsAlt: <FaExpandArrowsAlt />,
  BsGearFill: <BsGearFill />,
  BiSolidServer: <BiSolidServer />,
  PiHardDriveFill: <PiHardDriveFill />,
  FaArrowAltCircleDown: <FaArrowAltCircleDown />,
  FaTimesCircle: <FaTimesCircle />,
  FaExclamationCircle: <FaExclamationCircle />,
  FaSmileBeam: <FaSmileBeam />,
  FaSmile: <FaSmile />,
  FaMeh: <FaMeh />,
  FaFrown: <FaFrown />,
  FaAngry: <FaAngry />,
  FaClock: <FaClock />,
  FaCalendarAlt: <FaCalendarAlt />,
  Lt: <>{'<'}</>,
  FaRegHandPointer: <FaRegHandPointer />,
  HeatmapVisualMapIcon: <HeatmapVisualMapIcon />,
};

export const HelpPopover = ({
  helpKey,
  prevContent,
}: {
  helpKey: string;
  prevContent?: React.ReactNode;
}) => {
  const { t } = useTranslation();

  let helpContent: HelpContent;
  try {
    helpContent = (helpKey ? t(helpKey, { returnObjects: true }) || {} : {}) as HelpContent;
  } catch (err) {
    return;
  }

  function renderContent() {
    return (
      <>
        <div className="max-w-[500px]">
          <div className="text-[13px]">
            {helpContent?.DESC && <Trans i18nKey={`${helpKey}.DESC`} />}
          </div>
          {helpContent?.CATEGORY?.map((category, i) => {
            return (
              <div key={i}>
                <Separator className="my-3" />
                <div className="flex items-baseline mt-1 text-[13px] gap-2.5">
                  <h4 className="w-1/5 mb-2 text-sm font-semibold text-center min-w-20">
                    <Trans i18nKey={`${helpKey}.CATEGORY.${i}.TITLE`} />
                  </h4>
                  {helpContent?.CATEGORY?.[i]?.DESC && (
                    <div className="w-4/5 text-xs">
                      <Trans i18nKey={`${helpKey}.CATEGORY.${i}.DESC`} />
                    </div>
                  )}
                </div>
                <div className="flex flex-col gap-3">
                  {category?.ITEMS?.map((item, j) => {
                    return (
                      <div key={j} className="flex items-baseline mt-1 text-[13px] gap-2.5">
                        <div className="w-1/5 text-center min-w-20">
                          {item?.NAME ? (
                            <Trans
                              i18nKey={`${helpKey}.CATEGORY.${i}.ITEMS.${j}.NAME`}
                              className="text-xs text-primary"
                              components={components}
                              size={12}
                            />
                          ) : (
                            <PiInfo />
                          )}
                        </div>
                        <div className="w-4/5 text-xs">
                          <Trans
                            i18nKey={`${helpKey}.CATEGORY.${i}.ITEMS.${j}.DESC`}
                            components={components}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      </>
    );
  }

  return (
    <Popover modal={true}>
      <PopoverTrigger className="flex items-center">
        <PiQuestionFill className="fill-gray-400" fontSize={'large'} />
      </PopoverTrigger>
      <PopoverPrimitive.Portal>
        <PopoverContent className="z-[9999] w-auto overflow-auto max-h-[80vh]">
          {helpContent?.TITLE && (
            <h2 className="mb-1 text-base font-semibold text-primary">
              <Trans i18nKey={`${helpKey}.TITLE`} />
            </h2>
          )}
          {prevContent}
          {renderContent()}
        </PopoverContent>
      </PopoverPrimitive.Portal>
    </Popover>
  );
};
