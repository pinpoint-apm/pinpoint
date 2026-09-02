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
import { numberFromInput } from '@pinpoint-fe/ui/src/utils';

export interface AgentActiveSettingProps {
  className?: string;
  onApply?: (value: AgentActiveSettingType) => void;
  onClose?: () => void;
  defaultValues?: AgentActiveSettingType;
}

export const DefaultValue = { yMax: 100, isSplit: true, inactivityThreshold: 5 };

const FormSchema = z.object({
  yMax: numberFromInput(z.number().min(1)),
  isSplit: z.boolean(),
  inactivityThreshold: numberFromInput(z.number().min(0)), // minutes
});

// 숫자 필드는 입력(입력창이 주는 문자열 또는 defaultValues 가 주는 숫자)과 검증 결과(숫자)의
// 타입이 다르다. 그래서 useForm 에 <입력, 컨텍스트, 결과> 셋을 줘야 하고, handleSubmit 에 넘긴
// 콜백은 결과 타입을 받는다.
type FormInput = z.input<typeof FormSchema>;
export type AgentActiveSettingType = z.output<typeof FormSchema>;

export const AgentActiveSetting = ({
  className,
  onApply,
  onClose,
  defaultValues = DefaultValue,
}: AgentActiveSettingProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);

  const handleClickClose = () => {
    onClose?.();
  };

  useOnClickOutside(containerRef as React.RefObject<HTMLElement>, handleClickClose);

  const form = useForm<FormInput, unknown, AgentActiveSettingType>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      yMax: defaultValues?.yMax,
      isSplit: defaultValues?.isSplit,
      inactivityThreshold: defaultValues?.inactivityThreshold,
    },
  });

  function onSubmit(data: AgentActiveSettingType) {
    onApply?.(data);
    handleClickClose();
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      // Apply 버튼과 같은 경로를 타야 검증과 숫자 변환을 거친 값이 나간다.
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
