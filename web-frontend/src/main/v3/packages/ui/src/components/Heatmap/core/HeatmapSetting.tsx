import React from 'react';
import { useOnClickOutside } from 'usehooks-ts';
import { cn } from '@pinpoint-fe/ui/src/lib';
import {
  Button,
  Input,
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Separator,
} from '@pinpoint-fe/ui/src/components/ui';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { numberFromInput } from '@pinpoint-fe/ui/src/utils';

export interface HeatmapSettingProps {
  isRealtime?: boolean;
  className?: string;
  onApply?: (value: HeatmapSettingType) => void;
  onClose?: () => void;
  defaultValues?: HeatmapSettingType;
}

export const DefaultValue = {
  yMin: 0,
  yMax: 10000,
  visualMapSuccessMax: 5000,
  visualMapFailMax: 100,
};

const FormSchema = z.object({
  yMin: numberFromInput(z.number().min(0)),
  yMax: numberFromInput(z.number().min(200)),
  visualMapSuccessMax: numberFromInput(z.number().min(1)).optional(),
  visualMapFailMax: numberFromInput(z.number().min(1)).optional(),
});

// 숫자 필드는 입력(입력창이 주는 문자열 또는 defaultValues 가 주는 숫자)과 검증 결과(숫자)의
// 타입이 다르다. 그래서 useForm 에 <입력, 컨텍스트, 결과> 셋을 줘야 하고, handleSubmit 에 넘긴
// 콜백은 결과 타입을 받는다.
type FormInput = z.input<typeof FormSchema>;
export type HeatmapSettingType = z.output<typeof FormSchema>;

export const HeatmapSetting = ({
  isRealtime,
  className,
  onApply,
  onClose,
  defaultValues = DefaultValue,
}: HeatmapSettingProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);

  const handleClickClose = () => {
    onClose?.();
  };

  useOnClickOutside(containerRef as React.RefObject<HTMLElement>, handleClickClose);

  const form = useForm<FormInput, unknown, HeatmapSettingType>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      yMin: defaultValues?.yMin,
      yMax: defaultValues?.yMax,
      visualMapSuccessMax: defaultValues?.visualMapSuccessMax || DefaultValue.visualMapSuccessMax,
      visualMapFailMax: defaultValues?.visualMapFailMax || DefaultValue.visualMapFailMax,
    },
  });

  function onSubmit(data: HeatmapSettingType) {
    onApply?.(data);
    handleClickClose();
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      form.handleSubmit(onSubmit)();
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
      <div className="mb-3 font-semibold">Heatmap chart Setting</div>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="w-full space-y-6">
          <FormField
            control={form.control}
            name="yMin"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-xs text-muted-foreground">Min of Y axis</FormLabel>
                <FormControl>
                  <Input
                    type="number"
                    className="w-24 h-7"
                    onKeyDown={handleKeyDown}
                    min={0}
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="yMax"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-xs text-muted-foreground">Max of Y axis</FormLabel>
                <FormControl>
                  <Input
                    type="number"
                    className="w-24 h-7"
                    onKeyDown={handleKeyDown}
                    min={200}
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          {isRealtime && (
            <>
              <Separator />
              <FormField
                control={form.control}
                name="visualMapSuccessMax"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-xs text-muted-foreground">
                      VisualMap success max
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        className="w-24 h-7"
                        onKeyDown={handleKeyDown}
                        min={1}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="visualMapFailMax"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-xs text-muted-foreground">
                      VisualMap failed max
                    </FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        className="w-24 h-7"
                        onKeyDown={handleKeyDown}
                        min={1}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </>
          )}
          <div className="flex justify-end gap-1 mt-6">
            <Button className="text-xs h-7" variant="outline" onClick={handleClickClose}>
              Cancel
            </Button>
            <Button type="submit" className="text-xs h-7">
              Apply
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
};
