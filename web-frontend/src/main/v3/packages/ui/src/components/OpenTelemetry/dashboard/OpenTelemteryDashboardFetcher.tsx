import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import React from 'react';
import { useBlocker } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Button,
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '../../../components/ui';
import {
  useGetOtlpMetricDefUserDefined,
  useOpenTelemetrySearchParameters,
  usePatchOtlpMetricDefUserDefined,
} from '@pinpoint-fe/ui/hooks';
import { BiSolidSave } from 'react-icons/bi';
import { RxPlusCircled } from 'react-icons/rx';
import { useReactToastifyToast } from '../../../components/Toast';
import { LoadingButton } from '../../../components/Button';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { DashBoard } from '../../Dashboard/DashBoard';
import { MetricDefinitionSheet } from '../definition/MetricDefinitionSheet';
import { OpenTelemetryAlertDialog } from './OpenTelemetryAlertDialog';
import { isEqual, sortBy } from 'lodash';
import { cn } from '../../../lib/utils';
import { Widget } from '../../Dashboard/Widget';
import { OpenTelemetryMetric } from '../charts/OpenTelemetryMetric';

export interface OpenTelemetryDashboardFetcherProps {}

export const OpenTelemetryDashboardFetcher = () => {
  const toast = useReactToastifyToast();
  const { application } = useOpenTelemetrySearchParameters();
  const applicationName = application?.applicationName || '';
  const { t } = useTranslation();
  const { data, refetch } = useGetOtlpMetricDefUserDefined();
  const metrics = React.useMemo(
    () => sortBy(data?.appMetricDefinitionList, ['layout.y', 'layout.x']),
    [data],
  );
  const { mutate: updateMetrics, isPending } = usePatchOtlpMetricDefUserDefined({
    onSuccess: (res) => {
      if (res.result === 'SUCCESS') {
        refetch();
        setCurrentDeletingTarget(undefined);
        setCurrentEditingTarget(undefined);
      }
    },
  });
  const [currentDeletingTarget, setCurrentDeletingTarget] =
    React.useState<OtlpMetricDefUserDefined.Metric>();
  const [currentEditingTarget, setCurrentEditingTarget] =
    React.useState<OtlpMetricDefUserDefined.Metric>();

  const [state, setState] = React.useState<{
    layouts: ReactGridLayout.Layouts;
  }>({
    layouts: { sm: [], xxs: [] },
  });
  const prevLayouts = React.useRef<ReactGridLayout.Layouts>();
  const [isChanged, setIsChanged] = React.useState(false);

  const updateMetricsWithToastMessage = (
    props: Parameters<typeof updateMetrics>[0],
    {
      successMessage,
      errorMessage,
    }: {
      successMessage: string;
      errorMessage: string;
    },
  ) => {
    updateMetrics(props, {
      onSuccess: () => {
        toast.success(successMessage);
      },
      onError: () => {
        toast.error(errorMessage);
      },
    });
  };

  // grid-layout 변경 시 사용
  const onLayoutChange = (
    currentLayout: ReactGridLayout.Layout[],
    allLayouts: ReactGridLayout.Layouts,
  ) => {
    console.log('layouts', currentLayout, allLayouts);
    setState((prev) => ({
      ...prev,
      layouts: allLayouts,
    }));
  };

  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) =>
      isChanged && currentLocation.pathname !== nextLocation.pathname,
  );

  React.useEffect(() => {
    const layoutForCompare = state?.layouts?.sm?.map((l) => {
      return {
        i: l?.i,
        x: l?.x,
        y: l?.y,
        w: l?.w,
        h: l?.h,
      };
    });
    setIsChanged(!isEqual(layoutForCompare, prevLayouts.current?.sm));
  }, [state]);

  React.useEffect(() => {
    const handleBeforeUnload = (event: Event) => {
      event.preventDefault();
      event.returnValue = false; // Chrome requires returnValue to be set.
    };
    const onPreventLeave = () => {
      window.addEventListener('beforeunload', handleBeforeUnload);
    };
    const offPreventLeave = () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
    const fn = isChanged ? onPreventLeave : offPreventLeave;
    fn();
    return () => {
      offPreventLeave();
    };
  }, [isChanged]);

  React.useEffect(() => {
    if (metrics && metrics?.length > 0) {
      const newLayouts = {
        sm: metrics?.map((metric) => {
          const metricLayout = metric.layout;
          return {
            i: metric.id || '',
            x: metricLayout.x,
            y: metricLayout.y,
            w: metricLayout.w,
            h: metricLayout.h,
          };
        }),
      };

      prevLayouts.current = newLayouts;

      setState({
        layouts: newLayouts,
      });
    }
  }, [metrics]);

  const handleClickSaveMetric = () => {
    const gridLayouts = state.layouts.sm;
    if (gridLayouts && metrics) {
      const newMetics = metrics.map((metric) => {
        const newLayout = gridLayouts.find((gl) => gl.i === metric.id);

        return {
          ...metric,
          layout: {
            x: newLayout?.x,
            y: newLayout?.y,
            w: newLayout?.w,
            h: newLayout?.h,
          },
        };
      });
      updateMetricsWithToastMessage(
        {
          applicationName,
          appMetricDefinitionList: newMetics as OtlpMetricDefUserDefined.Metric[],
        },
        {
          successMessage: t('OPEN_TELEMETRY.SAVE_DASHBOARD_SUCCESS'),
          errorMessage: t('SAVE_DASHBOARD_FAIL.SAVE_DASHBOARD_FAIL'),
        },
      );
    }
  };

  return (
    <div className="flex items-center justify-center flex-1">
      {metrics &&
        (metrics.length > 0 ? (
          <div className="w-full h-full">
            <div className="flex justify-between mb-1">
              <div className="flex gap-2 ml-auto">
                <Button
                  variant="default"
                  className="gap-2 px-2 py-1 h-7"
                  onClick={() => {
                    setCurrentEditingTarget({} as OtlpMetricDefUserDefined.Metric);
                  }}
                >
                  <RxPlusCircled />{' '}
                  <span className="text-xs">{t('OPEN_TELEMETRY.CREATE_METRIC_BUTTON')}</span>
                </Button>
                <LoadingButton
                  pending={isPending}
                  variant={'outline'}
                  className={cn('gap-2 px-2 py-1 bg-white h-7', {
                    'animate-blinkWithBgColor': isChanged,
                  })}
                  onClick={handleClickSaveMetric}
                >
                  <div>
                    <BiSolidSave />{' '}
                    <span className="text-xs">{t('OPEN_TELEMETRY.SAVE_DASHBOARD')}</span>
                  </div>
                </LoadingButton>
              </div>
            </div>
            <DashBoard layouts={state.layouts} onLayoutChange={onLayoutChange}>
              {state.layouts.sm.length > 0 &&
                metrics.map((metric) => {
                  return (
                    <div key={metric?.id}>
                      <Widget
                        title={metric.title}
                        onClickDelete={() => {
                          setCurrentDeletingTarget(metric);
                        }}
                        onClickEdit={() => {
                          setCurrentEditingTarget(metric);
                        }}
                      >
                        <OpenTelemetryMetric
                          inView={true}
                          metricDefinition={metric}
                          dashboardId={applicationName}
                        />
                      </Widget>
                    </div>
                  );
                })}
            </DashBoard>
          </div>
        ) : (
          <Card className="rounded-sm w-96">
            <CardHeader>
              <CardTitle>OpenTelemetry</CardTitle>
              {/* <CardDescription>Card Description</CardDescription> */}
            </CardHeader>
            <CardContent>
              <p className="text-sm">{t('OPEN_TELEMETRY.CREATE_METRIC_DESC')}</p>
            </CardContent>
            <CardFooter>
              <Button
                className="text-xs"
                onClick={() => {
                  setCurrentEditingTarget({} as OtlpMetricDefUserDefined.Metric);
                }}
              >
                {t('OPEN_TELEMETRY.CREATE_METRIC_BUTTON')}
              </Button>
            </CardFooter>
          </Card>
        ))}
      <AlertDialog
        open={!!currentDeletingTarget}
        onOpenChange={(open) => {
          !open && setCurrentDeletingTarget(undefined);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {t('COMMON.DELETE_CONFIRM_QUESTION', {
                target: `${currentDeletingTarget?.title} (${currentDeletingTarget?.id})`,
              })}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {t('COMMON.DELETE_CONFIRM_DESCRIPTION', { target: t('OPEN_TELEMETRY.METRIC') })}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t('COMMON.CANCEL')}</AlertDialogCancel>
            <AlertDialogAction
              // disabled={!permissionContext.delete}
              buttonVariant={{ variant: 'destructive' }}
              onClick={() => {
                if (currentDeletingTarget && metrics) {
                  const targetExceptedMetrics = metrics
                    .filter((m) => m.id !== currentDeletingTarget.id)
                    ?.map((m) => {
                      const layoutByMetric = state?.layouts?.sm?.find(
                        (layout) => layout?.i === m?.id,
                      );

                      return {
                        ...m,
                        layout: layoutByMetric
                          ? {
                              w: layoutByMetric?.w,
                              h: layoutByMetric?.h,
                              x: layoutByMetric?.x,
                              y: layoutByMetric?.y,
                            }
                          : m?.layout,
                      };
                    });
                  updateMetricsWithToastMessage(
                    {
                      applicationName,
                      appMetricDefinitionList: targetExceptedMetrics,
                    },
                    {
                      successMessage: t('COMMON.TARGET_REMOVE_SUCCESS', {
                        target: `${currentDeletingTarget.title}`,
                      }),
                      errorMessage: t('COMMON.REMOVE_FAIL'),
                    },
                  );
                }
              }}
            >
              {t('COMMON.CONTINUE')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
      <MetricDefinitionSheet
        layouts={state?.layouts}
        metric={currentEditingTarget}
        open={!!currentEditingTarget}
        onOpenChange={(open) => {
          !open && setCurrentEditingTarget(undefined);
        }}
        onCancel={() => {
          setCurrentEditingTarget(undefined);
        }}
      />
      <OpenTelemetryAlertDialog
        open={isChanged && blocker?.state === 'blocked'}
        onCancel={() => blocker?.reset?.()}
        onContinue={() => blocker?.proceed?.()}
      />
    </div>
  );
};
