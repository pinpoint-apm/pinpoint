import React from 'react';
import { useCopyToClipboard } from 'usehooks-ts';
import { FaRegCopy } from 'react-icons/fa';
import { MdOutlineDone } from 'react-icons/md';
import { Button } from '../ui';
import { cn } from '../../lib';
import { toast } from '../Toast';

export interface ClipboardCopyButtonProps {
  copyValue: string;
  btnClassName?: string;
  containerClassName?: string;
  children?: React.ReactNode;
  hoverable?: boolean;
  onClickCopy?: () => void;
}

export const ClipboardCopyButton = ({
  copyValue,
  onClickCopy,
  containerClassName,
  btnClassName,
  hoverable,
  children,
}: ClipboardCopyButtonProps) => {
  const [, copy] = useCopyToClipboard();
  const [hover, setHover] = React.useState(false);
  const [copied, setCopied] = React.useState(false);

  const handleClickCopy = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      setCopied(await copy(copyValue));
    } catch (error) {
      toast.error('Copy failed', {
        autoClose: 2000,
      });
    }
    setTimeout(() => setCopied(false), 2000);
    onClickCopy?.();
  };

  return (
    <span
      className={cn(containerClassName, { 'cursor-pointer': hoverable })}
      onClick={handleClickCopy}
      onMouseEnter={() => hoverable && setHover(true)}
      onMouseLeave={() => hoverable && setHover(false)}
    >
      {children}
      <Button
        variant="outline"
        size="icon"
        className={cn(btnClassName, {
          invisible: hoverable,
          visible: hover,
          'cursor-default': copied,
        })}
        onClick={handleClickCopy}
      >
        {copied ? <MdOutlineDone className="w-3 h-3" /> : <FaRegCopy className="w-3 h-3" />}
      </Button>
    </span>
  );
};
