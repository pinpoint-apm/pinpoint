import { useTranslation } from 'react-i18next';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '../../ui/form';
import { Input } from '../../ui/input';
import { Label } from '../../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../ui/select';
import { RadioGroup, RadioGroupItem } from '../../ui/radio-group';
import { TFunction } from 'i18next';
import * as z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { cn } from '../../../lib';
import { FcAreaChart, FcBarChart, FcLineChart } from 'react-icons/fc';
import { Separator } from '../../ui/separator';
import { Button } from '../../ui/button';
import { useSetAtom } from 'jotai';
import { userMetricConfigAtom } from '@pinpoint-fe/atoms';
import { IconBaseProps } from 'react-icons/lib';

const metricDefinitionFormSchemaFactory = (t: TFunction) => {
  return z.object({
    metricGroup: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric group' }),
    }),
    metricName: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric name' }),
    }),
    tag: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Tag' }),
    }),
    aggregationFunction: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Aggregation function' }),
    }),
    chartType: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Chart type' }),
    }),
    yAxisUnit: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Y-axis unit' }),
    }),
    metricTitle: z.string({
      required_error: t('COMMON.REQUIRED', { requiredField: 'Metric title' }),
    }),
  });
};

export interface MetricDefinitionFormProps {}

export const MetricDefinitionForm = () => {
  const setUserMetricConfig = useSetAtom(userMetricConfigAtom);
  const { t } = useTranslation();
  const metricDefinitionFormSchema = metricDefinitionFormSchemaFactory(t);
  const metricDefinitionForm = useForm<z.infer<typeof metricDefinitionFormSchema>>({
    resolver: zodResolver(metricDefinitionFormSchema),
    defaultValues: {
      chartType: 'line',
    },
  });
  const [chartType, yAxisUnit, metricTitle] = metricDefinitionForm.watch([
    'chartType',
    'yAxisUnit',
    'metricTitle',
  ]);
  const chartTypeItems = [
    {
      id: 'bar',
      label: 'Bar',
      icon: (props: IconBaseProps) => <FcBarChart {...props} />,
    },
    {
      id: 'line',
      label: 'Line',
      icon: (props: IconBaseProps) => <FcLineChart {...props} />,
    },
    {
      id: 'area',
      label: 'Area',
      icon: (props: IconBaseProps) => <FcAreaChart {...props} />,
    },
  ];

  setUserMetricConfig({
    chartType,
    yAxisUnit,
    metricTitle,
  });

  const handleSubmit = async () => {};

  return (
    <Form {...metricDefinitionForm}>
      <form onSubmit={metricDefinitionForm.handleSubmit(handleSubmit)} className="">
        <div className="p-4 space-y-4">
          <h3 className="py-3 font-medium">1. Data configuration</h3>
          <FormField
            name="metricGroup"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Metric group </FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger
                      className={cn('focus-visible:ring-0 w-90', {
                        'border-status-fail': fieldState.invalid,
                      })}
                    >
                      <SelectValue placeholder="Select metric group" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent className="z-[5001]">
                    <SelectItem value="group-1">Metric Group 1</SelectItem>
                    <SelectItem value="group-2">Metric Group 2</SelectItem>
                    <SelectItem value="group-3">Metric Group 3</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            name="metricName"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Metric name </FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger
                      className={cn('focus-visible:ring-0 w-90', {
                        'border-status-fail': fieldState.invalid,
                      })}
                    >
                      <SelectValue placeholder="Select metric name" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent className="z-[5001]">
                    <SelectItem value="name-1">Metric Name 1</SelectItem>
                    <SelectItem value="name-2">Metric Name 2</SelectItem>
                    <SelectItem value="name-3">Metric Name 3</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            name="tag"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Tag </FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger
                      className={cn('focus-visible:ring-0', {
                        'border-status-fail': fieldState.invalid,
                      })}
                    >
                      <SelectValue placeholder="Select tag" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent className="z-[5001]">
                    <SelectItem value="tag-1">Tag 1</SelectItem>
                    <SelectItem value="tag-2">Tag 2</SelectItem>
                    <SelectItem value="tag-3">Tag 3</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            name="aggregationFunction"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground">
                  {' '}
                  Aggregation function{' '}
                </FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger
                      className={cn('focus-visible:ring-0 w-90', {
                        'border-status-fail': fieldState.invalid,
                      })}
                    >
                      <SelectValue placeholder="Select aggregation function" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent className="z-[5001]">
                    <SelectItem value="sum">Sum</SelectItem>
                    <SelectItem value="max">Max</SelectItem>
                    <SelectItem value="min">Min</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        <Separator className="mt-3" />
        <div className="p-4 space-y-4">
          <h3 className="py-3 font-medium">2. Graph configuration</h3>
          <FormField
            name="chartType"
            control={metricDefinitionForm.control}
            render={({ field }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Chart type </FormLabel>
                <RadioGroup className="flex gap-2" onValueChange={field.onChange}>
                  {chartTypeItems.map(({ id, icon, label }) => {
                    return (
                      <FormItem key={id}>
                        <FormLabel>
                          <FormControl>
                            <RadioGroupItem value={id} id={id} className="sr-only" />
                          </FormControl>
                          <Label
                            htmlFor={id}
                            className={cn(
                              'flex flex-col items-center justify-between p-2 py-4 border-2 rounded-md cursor-pointer w-28 border-muted bg-popover hover:bg-accent hover:text-accent-foreground',
                              { 'bg-accent': field.value === id },
                            )}
                          >
                            {icon({ className: 'w-6 h-6 mb-3' })}
                            {label}
                          </Label>
                        </FormLabel>
                      </FormItem>
                    );
                  })}
                </RadioGroup>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            name="yAxisUnit"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Y-axis unit </FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger
                      className={cn('focus-visible:ring-0 w-40', {
                        'border-status-fail': fieldState.invalid,
                      })}
                    >
                      <SelectValue placeholder="Select y-axis unit" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent className="z-[5001]">
                    <SelectItem value="bytes">Byte</SelectItem>
                    <SelectItem value="count">Count</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            name="metricTitle"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel className="font-normal text-muted-foreground"> Metric title </FormLabel>
                <FormControl>
                  <Input
                    className={cn('focus-visible:ring-0 w-90', {
                      'border-status-fail': fieldState.invalid,
                    })}
                    {...field}
                    placeholder={'Input metric title'}
                  />
                </FormControl>
                <FormDescription />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        <div className="flex justify-end gap-2 p-4">
          <Button variant="outline" type="button">
            {t('COMMON.CANCEL')}
          </Button>
          <Button variant={'default'} type="submit">
            {t('COMMON.SUBMIT')}
          </Button>
        </div>
      </form>
    </Form>
  );
};
