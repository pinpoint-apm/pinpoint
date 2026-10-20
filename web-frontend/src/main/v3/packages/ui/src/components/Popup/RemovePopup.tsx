import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogClose,
  Input,
  Label,
} from '../ui';
import { DialogProps } from '@radix-ui/react-dialog';

export interface RemovePopupProps {
  popupTrigger: React.ReactNode;
  popupTitle?: string;
  popupDesc?: string;
  popupContents?: React.ReactNode;
  onOpenChange?: DialogProps['onOpenChange'];
  onClickRemove?: () => void;
  /**
   * 지정하면 이 문구를 그대로 입력해야 삭제 버튼이 활성화된다.
   * 되돌리기 어려운 삭제에 사용한다.
   */
  confirmText?: string;
  confirmLabel?: React.ReactNode;
  removeButtonLabel?: React.ReactNode;
}

export const RemovePopup = ({
  popupTrigger,
  popupTitle,
  popupDesc,
  popupContents,
  onOpenChange,
  onClickRemove,
  confirmText,
  confirmLabel,
  removeButtonLabel,
}: RemovePopupProps) => {
  const { t } = useTranslation();
  const confirmInputId = React.useId();
  const [confirmInput, setConfirmInput] = React.useState('');
  const isConfirmRequired = confirmText !== undefined;
  const isConfirmed = !isConfirmRequired || confirmInput === confirmText;

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      setConfirmInput('');
    }
    onOpenChange?.(open);
  };

  return (
    <Dialog onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>{popupTrigger}</DialogTrigger>
      <DialogContent onClick={(e) => e.stopPropagation()}>
        <DialogHeader>
          <DialogTitle className="text-base">{popupTitle}</DialogTitle>
          <DialogDescription className="text-sm">{popupDesc}</DialogDescription>
        </DialogHeader>
        {popupContents}
        {isConfirmRequired && (
          <div className="flex flex-col gap-2">
            <Label htmlFor={confirmInputId} className="text-sm font-normal">
              {confirmLabel}
            </Label>
            <Input
              id={confirmInputId}
              value={confirmInput}
              onChange={(e) => setConfirmInput(e.target.value)}
              autoComplete="off"
              spellCheck={false}
            />
          </div>
        )}
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline" size="sm">
              {t('COMMON.CANCEL')}
            </Button>
          </DialogClose>
          <DialogClose asChild>
            <Button variant="destructive" size="sm" onClick={onClickRemove} disabled={!isConfirmed}>
              {removeButtonLabel ?? t('COMMON.REMOVE')}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
