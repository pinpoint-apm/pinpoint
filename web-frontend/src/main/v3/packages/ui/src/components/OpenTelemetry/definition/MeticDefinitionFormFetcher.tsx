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
  useOpenTelemetrySearchParameters,
  // useOpenTelemetrySearchParameters,
  usePatchOtlpMetricDefUserDefined,
} from '@pinpoint-fe/hooks';
import { toast } from '../../../components/Toast';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { Checkbox } from '../../ui/checkbox';
import { getNewWidgetLayout } from '../../../components/Dashboard/DashBoard';
import { Switch } from '../../../components/ui/switch';
import { HelpPopover } from '../../../components/HelpPopover';

const metricDefinitionFormSchemaFactory = (t: TFunction) => {
  return z
    .object({
      metricGroupName: z.string({
        required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric group' }),
      }),
      metricName: z
        .string({
          required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric name' }),
        })
        .min(1, t('COMMON.REQUIRED_SELECT', { requiredField: 'Metric name' })),
      primaryForFieldAndTagRelation: z.enum(['tag', 'field']),
      tagGroupList: z.array(z.string()),
      fieldNameList: z.array(z.string()),
      aggregationFunction: z.string({
        required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Aggregation function' }),
      }),
      interval: z.string().optional(),
      chartType: z.string({
        required_error: t('COMMON.REQUIRED_SELECT', { requiredField: 'Chart type' }),
      }),
      title: z.string().min(1, t('COMMON.REQUIRED', { requiredField: 'Metric title' })),
      stack: z.boolean().default(false).optional(),
      stackInfo: z.object({
        showTotal: z.boolean().default(false).optional(),
      }),
    })
    .superRefine((data, ctx) => {
      if (data.primaryForFieldAndTagRelation === 'tag' && data?.tagGroupList?.length === 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: t('COMMON.REQUIRED_SELECT', { requiredField: 'Tag' }),
          path: ['tagGroupList'],
        });
      }
      if (data.primaryForFieldAndTagRelation === 'field' && data?.fieldNameList?.length === 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: t('COMMON.REQUIRED_SELECT', { requiredField: 'Field' }),
          path: ['fieldNameList'],
        });
      }
      return true;
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
  const { application } = useOpenTelemetrySearchParameters();
  const applicationName = application?.applicationName || '';
  const { refetch } = useGetOtlpMetricDefUserDefined();
  const setUserMetricConfig = useSetAtom(userMetricConfigAtom);
  const { t } = useTranslation();
  const metricDefinitionFormSchema = metricDefinitionFormSchemaFactory(t);
  const metricDefinitionForm = useForm<z.infer<typeof metricDefinitionFormSchema>>({
    resolver: zodResolver(metricDefinitionFormSchema),
    defaultValues: {
      metricGroupName: metric?.metricGroupName,
      metricName: metric?.metricName,
      primaryForFieldAndTagRelation: metric?.primaryForFieldAndTagRelation || 'tag',
      tagGroupList: metric?.tagGroupList || [],
      fieldNameList: metric?.fieldNameList || [],
      aggregationFunction: metric?.aggregationFunction,
      // interval: (metric as any)?.interval || '',
      chartType: metric?.chartType || 'line',
      title: metric?.title || '',
      stack: metric?.stack,
      stackInfo: {
        // showTotal: (metric as any)?.stackInfo?.showTotal,
      },
    },
  });
  const { data: defPropertyData } = useGetOtlpMetricDefProperty();

  const [
    metricGroupName,
    metricName,
    chartType,
    // unit,
    primaryForFieldAndTagRelation,
    tagGroupList,
    fieldNameList,
    title,
    stack,
  ] = metricDefinitionForm.watch([
    'metricGroupName',
    'metricName',
    'chartType',
    // 'unit',
    'primaryForFieldAndTagRelation',
    'tagGroupList',
    'fieldNameList',
    'title',
    'stack',
  ]);

  const selectedMetricGroupItem = defPropertyData?.metricGroupList.find((metricGroupItem) => {
    return metricGroupItem.metricGroupName === metricGroupName;
  });
  const selectedMetricItem = selectedMetricGroupItem?.metricList.find((metricItem) => {
    return metricItem.metricName === metricName;
  });

  const propertyList = React.useMemo(() => {
    if (primaryForFieldAndTagRelation === 'tag') {
      return selectedMetricItem?.tagClusterList?.map((tagCluster) => tagCluster?.tagGroup) || [];
    }
    return (
      selectedMetricItem?.fieldClusterList?.map((fieldCluster) => fieldCluster?.fieldName) || []
    );
  }, [primaryForFieldAndTagRelation, selectedMetricItem]);

  const propertyLegendList = React.useMemo(() => {
    if (primaryForFieldAndTagRelation === 'tag') {
      return (
        selectedMetricItem?.tagClusterList
          ?.find((tagCluster) => {
            return tagCluster.tagGroup === tagGroupList?.[0];
          })
          ?.fieldAndUnitList?.map((fieldItem) => {
            return {
              name: fieldItem?.fieldName,
              unit: fieldItem?.unit,
            };
          }) || []
      );
    }

    const fieldCluster = selectedMetricItem?.fieldClusterList?.find((fieldCluster) => {
      return fieldCluster.fieldName === fieldNameList?.[0];
    });

    return (
      fieldCluster?.tagGroupList?.map((tagItem) => {
        return {
          name: tagItem,
          unit: fieldCluster?.unit,
        };
      }) || []
    );
  }, [
    defPropertyData,
    selectedMetricItem,
    primaryForFieldAndTagRelation,
    tagGroupList,
    fieldNameList,
  ]);

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

  React.useEffect(() => {
    metricDefinitionForm.trigger('fieldNameList');
    metricDefinitionForm.trigger('tagGroupList');
  }, [primaryForFieldAndTagRelation]);

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
          ...(metrics || []).filter((m) => m.id !== metric?.id),
          {
            ...data,
            id: metric?.id,
            applicationName,
            stack: !!data.stack,
            layout: {
              w: metric?.layout.w,
              h: metric?.layout.h,
              x: metric?.layout.x,
              y: metric?.layout.y,
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
            stack: !!data.stack,
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
                      metricDefinitionForm.setValue('tagGroupList', []);
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
                      metricDefinitionForm.setValue('tagGroupList', []);
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
          <div className="sm:grid sm:grid-cols-12">
            <FormField
              name="primaryForFieldAndTagRelation"
              control={metricDefinitionForm.control}
              render={({ field, fieldState }) => (
                <FormItem className="content-center pr-4 font-normal sm:col-span-4 text-muted-foreground">
                  <div className="sm:!mt-0 sm:col-span-8">
                    <div className="flex gap-1">
                      <Select
                        value={field.value}
                        onValueChange={(value) => {
                          metricDefinitionForm.setValue('fieldNameList', []);
                          metricDefinitionForm.setValue('tagGroupList', []);
                          field.onChange(value);
                        }}
                        disabled={!metricName}
                      >
                        <SelectTrigger
                          className={cn('focus-visible:ring-0', {
                            'border-status-fail': fieldState.invalid,
                          })}
                        >
                          <SelectValue placeholder="Select property" />
                        </SelectTrigger>
                        <SelectContent className="z-[5001] max-w-[calc(100vw-2rem)] max-sm:max-w-[var(--radix-select-trigger-width)]">
                          <SelectItem value="tag">Tag</SelectItem>
                          <SelectItem value="field">Field</SelectItem>
                        </SelectContent>
                      </Select>
                      <HelpPopover
                        title={'Tag / Field'}
                        content={
                          <ul className="flex flex-col gap-2 ml-4 text-xs">
                            {t('OPEN_TELEMETRY.FIELD_OR_TAG_SELECTBOX_DESC')
                              .split('\n')
                              .map((txt, i) => (
                                <li key={i} className="list-disc">
                                  {txt}
                                </li>
                              ))}
                          </ul>
                        }
                      />
                    </div>
                    <FormDescription />
                    <FormMessage />
                  </div>
                </FormItem>
              )}
            />
            <FormField
              name={primaryForFieldAndTagRelation === 'tag' ? 'tagGroupList' : 'fieldNameList'}
              control={metricDefinitionForm.control}
              render={({ field, fieldState }) => (
                <FormItem className="sm:!mt-0 sm:col-span-8">
                  <div className="sm:!mt-0 sm:col-span-8">
                    <Select
                      value={field.value?.[0] || ''}
                      onValueChange={(value) => {
                        metricDefinitionForm.setValue('fieldNameList', []);
                        metricDefinitionForm.setValue('tagGroupList', []);
                        field.onChange([value]);
                      }}
                      disabled={!metricName}
                    >
                      <FormControl>
                        <SelectTrigger
                          className={cn('focus-visible:ring-0', {
                            'border-status-fail': fieldState.invalid,
                          })}
                        >
                          <SelectValue placeholder={`Select ${primaryForFieldAndTagRelation}`} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent className="z-[5001] max-w-[calc(100vw-2rem)] max-sm:max-w-[var(--radix-select-trigger-width)]">
                        {propertyList?.map((propertyItem, i) => {
                          return (
                            <SelectItem
                              key={i}
                              value={propertyItem}
                              // className="[&>span]:block [&>span]:truncate [&>span]:flex-1"
                            >
                              {propertyItem}
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
          <FormField
            name={primaryForFieldAndTagRelation === 'tag' ? 'fieldNameList' : 'tagGroupList'}
            control={metricDefinitionForm.control}
            render={({ field }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  {primaryForFieldAndTagRelation === 'tag' ? 'Field name' : 'Tags'}
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8 border rounded min-h-8 max-h-96 overflow-y-auto p-2">
                  {!!propertyLegendList?.length && (
                    <>
                      <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                        <Checkbox
                          id="all"
                          checked={field?.value?.length === propertyLegendList?.length}
                          onCheckedChange={(checked) => {
                            if (checked) {
                              metricDefinitionForm.setValue(
                                primaryForFieldAndTagRelation === 'tag'
                                  ? 'fieldNameList'
                                  : 'tagGroupList',
                                propertyLegendList?.map((legendItem) => legendItem?.name),
                              );
                            } else {
                              metricDefinitionForm.setValue(
                                primaryForFieldAndTagRelation === 'tag'
                                  ? 'fieldNameList'
                                  : 'tagGroupList',
                                [],
                              );
                            }
                          }}
                        />
                        <FormLabel className="text-sm font-normal" htmlFor="all">
                          <span className="font-bold">ALL</span>
                          <span className="text-muted-foreground">{`(Select all ${primaryForFieldAndTagRelation === 'tag' ? 'field' : 'tag'})`}</span>
                        </FormLabel>
                      </FormItem>
                      <Separator className="my-1" />
                    </>
                  )}
                  <div className="flex flex-col gap-1 py-1 overflow-y-scroll">
                    {propertyLegendList?.map((legendItem, i) => {
                      return (
                        <FormField
                          key={i}
                          control={metricDefinitionForm.control}
                          name={
                            primaryForFieldAndTagRelation === 'tag'
                              ? 'fieldNameList'
                              : 'tagGroupList'
                          }
                          render={({ field }) => {
                            return (
                              <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                                {/* <FormItem className="flex flex-row items-center space-x-3 space-y-0"> */}
                                <FormControl>
                                  <Checkbox
                                    checked={field?.value?.includes(legendItem?.name)}
                                    onCheckedChange={(checked) => {
                                      return checked
                                        ? field?.onChange([
                                            ...(field?.value || []),
                                            legendItem?.name,
                                          ])
                                        : field?.onChange(
                                            field?.value?.filter(
                                              (value) => value !== legendItem?.name,
                                            ),
                                          );
                                    }}
                                  />
                                </FormControl>
                                <FormLabel className="w-auto text-sm font-normal break-all">
                                  {legendItem?.name}
                                </FormLabel>
                              </FormItem>
                            );
                          }}
                        />
                      );
                    })}
                  </div>
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
          <FormField
            name="interval"
            control={metricDefinitionForm.control}
            render={({ field, fieldState }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Interval
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <FormControl>
                    <Input
                      className={cn('focus-visible:ring-0 max-w-90', {
                        'border-status-fail': fieldState.invalid,
                      })}
                      {...field}
                      placeholder={'Input interval (ex. 30s)'}
                    />
                  </FormControl>
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
          <FormField
            name="stack"
            control={metricDefinitionForm.control}
            render={({ field }) => (
              <FormItem className="sm:grid sm:grid-cols-12">
                <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                  Stack
                </FormLabel>
                <div className="sm:!mt-0 sm:col-span-8">
                  <FormControl>
                    <Switch checked={field.value} onCheckedChange={field.onChange} />
                  </FormControl>
                  <FormDescription />
                  <FormMessage />
                </div>
              </FormItem>
            )}
          />
          {stack && (
            <FormField
              name="stackInfo.showTotal"
              control={metricDefinitionForm.control}
              render={({ field }) => (
                <>
                  <FormItem className="sm:grid sm:grid-cols-12">
                    <div className="content-center font-normal sm:col-span-4 text-muted-foreground"></div>
                    <div className="sm:!mt-0 sm:col-span-8 flex flex-row gap-1">
                      <FormControl>
                        <Checkbox
                          className="content-center"
                          checked={field?.value}
                          onCheckedChange={field.onChange}
                        />
                      </FormControl>
                      <FormLabel className="content-center font-normal sm:col-span-4 text-muted-foreground">
                        Show total
                      </FormLabel>
                    </div>
                  </FormItem>
                </>
              )}
            />
          )}
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
