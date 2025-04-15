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
} from '@pinpoint-fe/ui/src/components/ui';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

export interface HeatmapSettingProps {
  className?: string;
  onApply?: (value: HeatmapSettingType) => void;
  onClose?: () => void;
  defaultValues?: HeatmapSettingType;
}

export const DefaultValue = { yMin: 0, yMax: 10000 };

const FormSchema = z.object({
  yMin: z.coerce.number().min(0),
  yMax: z.coerce.number().min(1),
});

export type HeatmapSettingType = z.infer<typeof FormSchema>;

export const HeatmapSetting = ({
  className,
  onApply,
  onClose,
  defaultValues = DefaultValue,
}: HeatmapSettingProps) => {
  const containerRef = React.useRef(null);

  const handleClickClose = () => {
    onClose?.();
  };

  useOnClickOutside(containerRef, handleClickClose);

  const form = useForm<z.infer<typeof FormSchema>>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      yMin: defaultValues?.yMin,
      yMax: defaultValues?.yMax,
    },
  });

  function onSubmit(data: z.infer<typeof FormSchema>) {
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
                    min={1}
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
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
