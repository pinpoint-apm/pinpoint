import React from 'react';
import { useForm } from 'react-hook-form';
import { useOnClickOutside } from 'usehooks-ts';
import { cn } from '../../../lib';
import { Button, Input } from '../..';

export type ScatterSettingValues = { yMin: number; yMax: number };

export interface ScatterSettingProps {
  className?: string;
  onApply?: (value: ScatterSettingValues) => void;
  onClose?: () => void;
  defaultValues?: ScatterSettingValues;
}

export const ScatterSetting = ({
  className,
  onApply,
  onClose,
  defaultValues = { yMin: 0, yMax: 1 },
}: ScatterSettingProps) => {
  const SCATTER_SETTING_BOX_ID = '__scatter_setting_box__';
  const Y_MIN = 'yMin';
  const Y_MAX = 'yMax';
  const Y_MIN_ID = `${SCATTER_SETTING_BOX_ID}_${Y_MIN}`;
  const Y_MAX_ID = `${SCATTER_SETTING_BOX_ID}_${Y_MAX}`;
  const containerRef = React.useRef(null);
  const {
    register,
    getValues,
    // formState: { errors },
  } = useForm({
    defaultValues: defaultValues,
  });

  const handleClickApply = () => {
    onApply?.({
      yMin: Number(getValues(Y_MIN)),
      yMax: Number(getValues(Y_MAX)),
    });
    onClose?.();
  };

  const handleClickClose = () => {
    onClose?.();
  };

  useOnClickOutside(containerRef, handleClickClose);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleClickApply();
    }
  };

  return (
    <div
      className={cn(
        'rounded shadow bg-background p-4 w-60 flex gap-3 flex-col text-sm border',
        className,
      )}
      ref={containerRef}
    >
      <div className="mb-3 font-semibold">Scatter Chart Setting</div>
      <div>
        <label className="text-xs text-muted-foreground" htmlFor={`${Y_MIN_ID}`}>
          Min of Y axis
        </label>
        <div style={{ marginBottom: 8 }}>
          <Input
            className="w-24 h-7"
            id={`${Y_MIN_ID}`}
            type="number"
            {...register(Y_MIN)}
            onKeyDown={handleKeyDown}
          />
        </div>
        <label className="text-xs text-muted-foreground" htmlFor={`${Y_MAX_ID}`}>
          Max of Y axis
        </label>
        <div>
          <Input
            className="w-24 h-7"
            id={`${Y_MAX_ID}`}
            type="number"
            {...register(Y_MAX, { min: 1 })}
            onKeyDown={handleKeyDown}
          />
        </div>
      </div>
      <div className="flex justify-end gap-1 mt-6">
        <Button className="text-xs h-7" variant="outline" onClick={handleClickClose}>
          Cancel
        </Button>
        <Button className="text-xs h-7" onClick={handleClickApply}>
          Apply
        </Button>
      </div>
    </div>
  );
};
