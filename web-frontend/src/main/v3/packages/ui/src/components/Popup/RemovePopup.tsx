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
} from '../ui';

export interface RemovePopupProps {
  popupTrigger: React.ReactNode;
  popupTitle?: string;
  popupDesc?: string;
  popupContents?: React.ReactNode;
  onClickRemove?: () => void;
}

export const RemovePopup = ({
  popupTrigger,
  popupTitle,
  popupDesc,
  popupContents,
  onClickRemove,
}: RemovePopupProps) => {
  const { t } = useTranslation();

  return (
    <Dialog>
      <DialogTrigger asChild>{popupTrigger}</DialogTrigger>
      <DialogContent onClick={(e) => e.stopPropagation()}>
        <DialogHeader>
          <DialogTitle className="text-base">{popupTitle}</DialogTitle>
          <DialogDescription className="text-sm">{popupDesc}</DialogDescription>
        </DialogHeader>
        {popupContents}
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline" size="sm">
              {t('COMMON.CANCEL')}
            </Button>
          </DialogClose>
          <Button variant="destructive" size="sm" onClick={onClickRemove}>
            {t('COMMON.REMOVE')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
