import React from 'react';
import { Popover, PopoverContent, PopoverTrigger } from '../../components/ui/popover';
import { MdHelp, MdOutlineInfo } from 'react-icons/md';
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
} from 'react-icons/fa';
import { BsGearFill } from 'react-icons/bs';
import * as PopoverPrimitive from '@radix-ui/react-popover';
import { Trans, useTranslation } from 'react-i18next';
import { Separator } from '../../components/ui/separator';
import { BiSolidServer } from 'react-icons/bi';
import { PiHardDriveFill } from 'react-icons/pi';

export const HelpPopover = ({
  open,
  onOpenChange,
  title,
  content,
}: {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
  title?: React.ReactNode;
  content?: React.ReactNode;
}) => {
  return (
    <Popover modal={true} open={open} onOpenChange={onOpenChange}>
      <PopoverTrigger>
        <MdHelp className="fill-primary" />
      </PopoverTrigger>
      <PopoverPrimitive.Portal>
        <PopoverContent className="z-[9999] w-auto overflow-scroll max-h-[80vh]">
          {title && <h2 className="mb-1 text-base font-semibold text-primary">{title}</h2>}
          {content}
        </PopoverContent>
      </PopoverPrimitive.Portal>
    </Popover>
  );
};

export type HelpContent = {
  TITLE?: string;
  DESC?: string;
  CATEGORY?: {
    TITLE?: string;
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
  Lt: <>{'<'}</>,
};

export function renderHelpPopoverContent(key: string) {
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const { t } = useTranslation();

  let helpContent: HelpContent;
  try {
    helpContent = t(key, { returnObjects: true }) as HelpContent;
  } catch (err) {
    return {
      title: '',
      content: '',
    };
  }

  return {
    title: helpContent?.TITLE && <Trans i18nKey={`${key}.TITLE`} />,
    content: (
      <div className="max-w-[500px]">
        <div className="text-[13px]">{helpContent?.DESC && <Trans i18nKey={`${key}.DESC`} />}</div>
        {helpContent?.CATEGORY?.map((category, i) => {
          return (
            <div key={i}>
              <Separator className="my-3" />
              <h4 className="w-1/5 mb-2 text-sm font-semibold text-center min-w-20">
                <Trans i18nKey={`${key}.CATEGORY.${i}.TITLE`} />
              </h4>
              <div className="flex flex-col gap-3">
                {category?.ITEMS?.map((item, j) => {
                  return (
                    <div key={j} className="flex items-baseline mt-1 text-[13px] gap-2.5">
                      <div className="w-1/5 text-center min-w-20">
                        {item?.NAME ? (
                          <Trans
                            i18nKey={`${key}.CATEGORY.${i}.ITEMS.${j}.NAME`}
                            className="text-xs text-primary"
                            components={components}
                            size={12}
                          />
                        ) : (
                          <MdOutlineInfo />
                        )}
                      </div>
                      <div className="w-4/5 text-xs">
                        <Trans
                          i18nKey={`${key}.CATEGORY.${i}.ITEMS.${j}.DESC`}
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
    ),
  };
}
