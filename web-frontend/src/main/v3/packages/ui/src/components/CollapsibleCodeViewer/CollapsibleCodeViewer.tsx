import React from 'react';
import { Collapsible, CollapsibleTrigger, CollapsibleContent } from '../ui';
import { ClipboardCopyButton } from '../Button';
import { HighLightCode, HighLightCodeProps } from '../HighLightCode';
import { RxChevronDown, RxChevronRight } from 'react-icons/rx';

export interface CollapsibleCodeViewerProps {
  code: string;
  language: HighLightCodeProps['language'];
  title?: string;
  defaultOpen?: boolean;
}

export const CollapsibleCodeViewer = ({
  title,
  code,
  language,
  defaultOpen = true,
}: CollapsibleCodeViewerProps) => {
  const [isOpen, setIsOpen] = React.useState(defaultOpen);

  return (
    <Collapsible open={isOpen} onOpenChange={setIsOpen} className="border rounded">
      <CollapsibleTrigger asChild>
        <div className="flex px-4 py-2 rounded cursor-pointer">
          <div className="flex items-center gap-1">
            {isOpen ? <RxChevronDown /> : <RxChevronRight />}
            <span className="text-sm font-semibold">{title}</span>{' '}
          </div>
          <ClipboardCopyButton
            copyValue={code}
            containerClassName="flex items-center ml-auto"
            btnClassName="border-none shadow-none w-8 h-8 text-muted-foreground"
          />
        </div>
      </CollapsibleTrigger>
      <CollapsibleContent className="px-4 pb-4">
        <HighLightCode language={language} code={code} className="p-2 text-xs" />
      </CollapsibleContent>
    </Collapsible>
  );
};
