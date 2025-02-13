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
  Checkbox,
} from '@pinpoint-fe/ui/src/components/ui';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

export interface AgentActiveSettingProps {
  className?: string;
  onApply?: (value: AgentActiveSettingType) => void;
  onClose?: () => void;
  defaultValues?: AgentActiveSettingType;
}

export const DefaultValue = { yMax: 100, isSplit: true, inactivityThreshold: 5 };

const FormSchema = z.object({
  yMax: z.coerce.number().min(1),
  isSplit: z.boolean(),
  inactivityThreshold: z.coerce.number().min(0), // minutes
});

export type AgentActiveSettingType = z.infer<typeof FormSchema>;

export const AgentActiveSetting = ({
  className,
  onApply,
  onClose,
  defaultValues = DefaultValue,
}: AgentActiveSettingProps) => {
  const containerRef = React.useRef(null);

  const handleClickClose = () => {
    onClose?.();
  };

  useOnClickOutside(containerRef, handleClickClose);

  const form = useForm<z.infer<typeof FormSchema>>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      yMax: defaultValues?.yMax,
      isSplit: defaultValues?.isSplit,
      inactivityThreshold: defaultValues?.inactivityThreshold,
    },
  });

  function onSubmit(data: z.infer<typeof FormSchema>) {
    onApply?.(data);
    handleClickClose();
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    const data = form.getValues();

    if (e.key === 'Enter') {
      onApply?.(data);
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
      <div className="mb-3 font-semibold">Agent request chart Setting</div>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="w-full space-y-6">
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
          <FormField
            control={form.control}
            name="isSplit"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-xs text-muted-foreground">
                  Split chart in 2 (&gt;=100)
                </FormLabel>
                <FormControl className="flex flex-row items-start space-x-3 space-y-0">
                  <Checkbox
                    checked={field.value}
                    onCheckedChange={(checked) => {
                      return field.onChange(checked);
                    }}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="inactivityThreshold"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-xs text-muted-foreground">
                  Inactivity Threshold (m)
                </FormLabel>
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
