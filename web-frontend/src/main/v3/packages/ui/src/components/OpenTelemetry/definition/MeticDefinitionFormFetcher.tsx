import React from 'react';
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
import {
  useGetOtlpMetricDefProperty,
  useGetOtlpMetricDefUserDefined,
  // useOpenTelemetrySearchParameters,
  usePatchOtlpMetricDefUserDefined,
} from '@pinpoint-fe/hooks';
import { toast } from '../../../components/Toast';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { Checkbox } from '../../ui/checkbox';
import { getNewWidgetLayout } from '../../../components/Dashboard/DashBoard';
// import { Checkbox } from '../../../components/ui';

const metricDefinitionFormSchemaFactory = (t: TFunction) => {
  return z.object({
    metricGroupName: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric group' }),
    }),
    metricName: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric name' }),
    }),
    tags: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Tag' }),
    }),
    fieldNameList: z.array(
      z.string({
        required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Field Name' }),
      }),
    ),
    aggregationFunction: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Aggregation function' }),
    }),
    chartType: z.string({
      required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Chart type' }),
    }),
    // TODO:
    // unit: z.string({
    //   required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Y-axis unit' }),
    // }),
    title: z.string({
      required_error: t('COMMON.REQUIRED', { requiredField: 'Metric title' }),
    }),
  });
};

export interface MetricDefinitionFormFetcherProps {
  metric?: OtlpMetricDefUserDefined.Metric;
  onComplete?: () => void;
  onClickCancel?: () => void;
}

export const MetricDefinitionFormFetcher = ({
  metric,
  onComplete,
  onClickCancel,
}: MetricDefinitionFormFetcherProps) => {
  const formId = '__metric_definition__';
  // TODO
  // const { application } = useOpenTelemetrySearchParameters();
  const applicationName = 'minwoo_local_app';
  const { refetch } = useGetOtlpMetricDefUserDefined();
  const setUserMetricConfig = useSetAtom(userMetricConfigAtom);
  const { t } = useTranslation();
  const metricDefinitionFormSchema = metricDefinitionFormSchemaFactory(t);
  const metricDefinitionForm = useForm<z.infer<typeof metricDefinitionFormSchema>>({
    resolver: zodResolver(metricDefinitionFormSchema),
    defaultValues: {
      metricGroupName: metric?.metricGroupName,
      metricName: metric?.metricName,
      tags: metric?.tags,
      fieldNameList: metric?.fieldNameList,
      aggregationFunction: metric?.aggregationFunction,
      chartType: metric?.chartType || 'line',
      title: metric?.title,
    },
  });
  const { data: defPropertyData } = useGetOtlpMetricDefProperty();
  const [
    metricGroupName,
    metricName,
    chartType,
    // unit,
    tags,
    title,
  ] = metricDefinitionForm.watch([
    'metricGroupName',
    'metricName',
    'chartType',
    // 'unit',
    'tags',
    'title',
  ]);
  const selectedMetricGroupItem = defPropertyData?.metricGroupList.find((metricGroupItem) => {
    return metricGroupItem.metricGroupName === metricGroupName;
  });
  const selectedMetricItem = selectedMetricGroupItem?.metricList.find((metricItem) => {
    return metricItem.metricName === metricName;
  });
  const selectedTagItem = selectedMetricItem?.tagClusterList.find((tagCluster) => {
    return tagCluster.tags === tags;
  });

  const { mutate: updateMetrics } = usePatchOtlpMetricDefUserDefined({
    onSuccess: (res) => {
      if (res.result === 'SUCCESS') {
        toast.success(`${metric?.id ? t('COMMON.UPDATE_SUCCESS') : t('COMMON.CREATE_SUCCESS')}`);
        refetch();
        onComplete?.();
      } else {
        toast.error(`${metric?.id ? t('COMMON.UPDATE_FAIL') : t('COMMON.CREATE_FAILED')}`);
      }
    },
  });
  const { data } = useGetOtlpMetricDefUserDefined();
  const metrics = data?.appMetricDefinitionList;

  React.useEffect(() => {
    setUserMetricConfig({
      chartType,
      // unit,
      title,
    });
  }, [
    chartType,
    // unit,
    title,
  ]);

  // React.useEffect(() => {
  //   console.log(metricGroupName);

  // }, [metricGroupName]);

  // React.useEffect(() => {
  //   metricDefinitionForm.setValue('tags', '');
  // }, [metricName]);

  const chartItem = {
    bar: {
      id: 'bar',
      label: 'Bar',
      icon: (props: IconBaseProps) => <FcBarChart {...props} />,
    },
    line: {
      id: 'line',
      label: 'Line',
      icon: (props: IconBaseProps) => <FcLineChart {...props} />,
    },
    area: {
      id: 'area',
      label: 'Area',
      icon: (props: IconBaseProps) => <FcAreaChart {...props} />,
    },
  };

  const handleSubmit = async (data: z.infer<typeof metricDefinitionFormSchema>) => {
    if (metric?.id) {
      // 수정
      updateMetrics({
        applicationName,
        appMetricDefinitionList: [
          ...(metrics || []).filter((m) => m.id !== metric.id),
          {
            ...data,
            id: metric.id,
            applicationName,
            layout: {
              w: metric.layout.w,
              h: metric.layout.h,
              x: metric.layout.x,
              y: metric.layout.y,
            },
            // TODO:
            unit: 'byte',
          },
        ],
      });
    } else {
      // 신규
      updateMetrics({
        applicationName,
        appMetricDefinitionList: [
          ...(metrics || []),
          {
            ...data,
            applicationName,
            layout: {
              ...getNewWidgetLayout(metrics || []),
            },
            // TODO:
            unit: 'byte',
          },
        ],
      });
    }
  };

  return (
    <Form {...metricDefinitionForm}>
      <form
        id={formId}
        className="relative flex-1 overflow-y-auto"
        onSubmit={metricDefinitionForm.handleSubmit(handleSubmit)}
      >
        <div className="p-4 space-y-4 md:p-6">
          <h3 className="py-3 font-medium">1. Data configuration</h3>
          <FormField
            name="metricGroupName"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Metric group
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <Select
                    onValueChange={(value) => {
                      metricDefinitionForm.setValue('fieldNameList', []);
                      metricDefinitionForm.setValue('tags', '');
                      metricDefinitionForm.setValue('metricName', '');
                      field.onChange(value);
                    }}
                    value={field.value}
                  >
                    <FormControl>
                      <SelectTrigger
                        className={cn('focus-visible:ring-0 max-w-90', {
                          'border-status-fail': fieldState.invalid,
                        })}
                      >
                        <SelectValue placeholder="Select metric group" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className="z-[5001]">
                      {defPropertyData?.metricGroupList?.map((metricGroup, i) => {
                        return (
                          <SelectItem key={i} value={metricGroup.metricGroupName}>
                            {metricGroup.metricGroupName}
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
          <FormField
            name="metricName"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Metric name
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <Select
                    value={field.value}
                    onValueChange={(value) => {
                      metricDefinitionForm.setValue('fieldNameList', []);
                      metricDefinitionForm.setValue('tags', '');
                      field.onChange(value);
                    }}
                    disabled={!metricGroupName}
                  >
                    <FormControl>
                      <SelectTrigger
                        className={cn('focus-visible:ring-0 max-w-90', {
                          'border-status-fail': fieldState.invalid,
                        })}
                      >
                        <SelectValue placeholder="Select metric name" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className="z-[5001]">
                      {selectedMetricGroupItem?.metricList?.map((metric, i) => {
                        return (
                          <SelectItem value={metric.metricName} key={i}>
                            {metric.metricName}
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
          <FormField
            name="tags"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Tag
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <Select
                    value={field.value}
                    onValueChange={(value) => {
                      metricDefinitionForm.setValue('fieldNameList', []);
                      field.onChange(value);
                    }}
                    disabled={!metricName}
                  >
                    <FormControl>
                      <SelectTrigger
                        className={cn('focus-visible:ring-0', {
                          'border-status-fail': fieldState.invalid,
                        })}
                      >
                        <SelectValue placeholder="Select tag" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className="z-[5001] max-w-[calc(100vw-2rem)] max-sm:max-w-[var(--radix-select-trigger-width)]">
                      {selectedMetricItem?.tagClusterList.map((tagCluster, i) => {
                        return (
                          <SelectItem
                            key={i}
                            value={tagCluster.tags}
                            // className="[&>span]:block [&>span]:truncate [&>span]:flex-1"
                          >
                            {tagCluster.tags}
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
          <FormField
            name="fieldNameList"
            control={metricDefinitionForm.control}
            render={({ field }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Field name
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8 border rounded min-h-8 max-h-96 overflow-y-auto p-2">
                  {selectedTagItem?.fieldAndUnitList?.length && (
                    <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                      <Checkbox
                        id="all"
                        checked={field?.value?.length === selectedTagItem?.fieldAndUnitList?.length}
                        onCheckedChange={(checked) => {
                          if (checked) {
                            metricDefinitionForm.setValue(
                              'fieldNameList',
                              selectedTagItem.fieldAndUnitList.map((item) => item.fieldName),
                            );
                          } else {
                            metricDefinitionForm.setValue('fieldNameList', []);
                          }
                        }}
                      />
                      <FormLabel className="text-sm font-normal" htmlFor="all">
                        all
                      </FormLabel>
                    </FormItem>
                  )}
                  {selectedTagItem?.fieldAndUnitList.map((fieldItem, i) => {
                    return (
                      <FormField
                        key={i}
                        control={metricDefinitionForm.control}
                        name="fieldNameList"
                        render={({ field }) => {
                          return (
                            <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                              <FormControl>
                                <Checkbox
                                  checked={field.value?.includes(fieldItem.fieldName)}
                                  onCheckedChange={(checked) => {
                                    console.log(field);

                                    return checked
                                      ? field.onChange([
                                          ...(field.value || []),
                                          fieldItem.fieldName,
                                        ])
                                      : field.onChange(
                                          field.value?.filter(
                                            (value) => value !== fieldItem.fieldName,
                                          ),
                                        );
                                  }}
                                />
                              </FormControl>
                              <FormLabel className="text-sm font-normal">
                                {fieldItem.fieldName}
                              </FormLabel>
                            </FormItem>
                          );
                        }}
                      />
                    );
                  })}
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
          <FormField
            name="aggregationFunction"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Aggregation function
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger
                        className={cn('focus-visible:ring-0 max-w-90', {
                          'border-status-fail': fieldState.invalid,
                        })}
                      >
                        <SelectValue placeholder="Select aggregation function" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className="z-[5001]">
                      {defPropertyData?.aggregationFunctionList.map((func) => {
                        return (
                          <SelectItem key={func} value={func}>
                            {func}
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
        </div>
        <Separator className="mt-3" />
        <div className="p-4 space-y-4 md:p-6">
          <h3 className="py-3 font-medium">2. Graph configuration</h3>
          <FormField
            name="chartType"
            control={metricDefinitionForm.control}
            render={({ field }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Chart type
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <RadioGroup className="flex gap-2" onValueChange={field.onChange}>
                    {defPropertyData?.chartTypeList?.map((chartId) => {
                      const { id, label, icon } = chartItem[chartId as keyof typeof chartItem];
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
                </div>
              </FormItem>
            )}
          />
          {/* TODO temporally */}
          {/* <FormField
            name="unit"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Y-axis unit
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
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
                </div>
              </FormItem>
            )}
          /> */}
          <FormField
            name="title"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Metric title
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <FormControl>
                    <Input
                      className={cn('focus-visible:ring-0 max-w-90', {
                        'border-status-fail': fieldState.invalid,
                      })}
                      {...field}
                      placeholder={'Input metric title'}
                    />
                  </FormControl>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
        </div>
      </form>
      <div className="flex justify-end gap-2 p-4 border-t">
        <Button
          variant="outline"
          type="button"
          onClick={() => {
            onClickCancel?.();
          }}
        >
          {t('COMMON.CANCEL')}
        </Button>
        <Button variant={'default'} type="submit" form={formId}>
          {t('COMMON.SUBMIT')}
        </Button>
      </div>
    </Form>
  );
};
