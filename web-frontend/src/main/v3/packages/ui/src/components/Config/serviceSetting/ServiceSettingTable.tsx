import { useTranslation } from 'react-i18next';
import { ColumnDef } from '@tanstack/react-table';
import { useAtom, useAtomValue, useSetAtom } from 'jotai';
import { LuCheck, LuPlus } from 'react-icons/lu';
import { HiOutlineSwitchHorizontal } from 'react-icons/hi';
import { FaRegTrashCan } from 'react-icons/fa6';
import {
  DEFAULT_SERVICE,
  isReservedServiceName,
  isServiceAddSheetOpenAtom,
  selectedServiceAtom,
  servicesAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { END_POINTS, GetServices } from '@pinpoint-fe/ui/src/constants';
import { queryClient, useDeleteService, useGetServices } from '@pinpoint-fe/ui/src/hooks';
import { DataTable } from '../../DataTable/DataTable';
import { RemovePopup } from '../../Popup';
import { ServiceAddSheet } from '../../Service/ServiceAddSheet';
import { useReactToastifyToast } from '../../Toast';
import {
  Button,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '../../ui';

type ServiceRow = { name: string };

export const ServiceSettingTable = () => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const selectedService = useAtomValue(selectedServiceAtom);
  const setSelectedService = useSetAtom(selectedServiceAtom);
  const setServices = useSetAtom(servicesAtom);
  const [isAddSheetOpen, setAddSheetOpen] = useAtom(isServiceAddSheetOpenAtom);

  const { data } = useGetServices();
  const rows: ServiceRow[] = [...(data ?? [])]
    .sort((a, b) => {
      // Order: selected service first, then DEFAULT, then the rest sorted by name.
      if (a === selectedService) return -1;
      if (b === selectedService) return 1;
      if (a === DEFAULT_SERVICE) return -1;
      if (b === DEFAULT_SERVICE) return 1;
      return a.localeCompare(b);
    })
    .map((name) => ({ name }));

  const { mutate: deleteService } = useDeleteService({
    onSuccess: (_res, params) => {
      toast.success(
        t('CONFIGURATION.SERVICE_SETTING.DELETE_SUCCESS', { name: params.serviceName }),
      );
      queryClient.setQueryData<GetServices.Response>([END_POINTS.SERVICES], (prev) =>
        prev ? prev.filter((name) => name !== params.serviceName) : prev,
      );
      setServices((prev) => (prev ? prev.filter((name) => name !== params.serviceName) : prev));
      if (params.serviceName === selectedService) {
        setSelectedService(DEFAULT_SERVICE);
      }
      queryClient.invalidateQueries({ queryKey: [END_POINTS.SERVICES] });
    },
  });

  const handleSwitchTo = (serviceName: string) => {
    setSelectedService(serviceName);
    toast.success(t('CONFIGURATION.SERVICE_SETTING.SWITCH_SUCCESS', { name: serviceName }));
  };

  const columns: ColumnDef<ServiceRow>[] = [
    {
      accessorKey: 'name',
      header: t('CONFIGURATION.SERVICE_SETTING.LABEL.NAME') || 'Name',
      cell: ({ row }) => {
        const service = row.original;
        const isCurrent = service.name === selectedService;
        return (
          <span className="flex items-center gap-2">
            <span>{service.name}</span>
            {isCurrent && <LuCheck className="w-4 h-4 text-[var(--blue-700)]" strokeWidth={3} />}
          </span>
        );
      },
    },
    {
      id: 'actions',
      header: t('CONFIGURATION.COMMON.LABEL.ACTIONS') || 'Actions',
      meta: {
        headerClassName: 'w-40 text-center',
        cellClassName: 'text-center',
      },
      cell: ({ row }) => {
        const service = row.original;
        const isCurrent = service.name === selectedService;
        if (isCurrent) {
          return (
            <span className="inline-flex items-center px-2 text-sm font-medium text-muted-foreground">
              {t('CONFIGURATION.SERVICE_SETTING.CURRENT') || 'Current'}
            </span>
          );
        }
        return (
          <Button
            variant="ghost"
            className="px-2 text-[var(--blue-700)] hover:text-[var(--blue-600)]"
            onClick={(e) => {
              e.stopPropagation();
              handleSwitchTo(service.name);
            }}
          >
            <HiOutlineSwitchHorizontal className="mr-1" />
            {t('CONFIGURATION.SERVICE_SETTING.SWITCH_TO') || 'Switch To'}
          </Button>
        );
      },
    },
    {
      id: 'delete',
      header: t('CONFIGURATION.SERVICE_SETTING.LABEL.DELETE') || 'Delete',
      meta: {
        headerClassName: 'w-20',
      },
      cell: ({ row }) => {
        const service = row.original;
        const isReserved = isReservedServiceName(service.name);
        if (isReserved) {
          return (
            <Tooltip>
              <TooltipTrigger asChild>
                <span className="inline-block">
                  <Button
                    variant="ghost"
                    className="px-3 pointer-events-none"
                    disabled
                    tabIndex={-1}
                  >
                    <FaRegTrashCan />
                  </Button>
                </span>
              </TooltipTrigger>
              <TooltipContent>
                {t('CONFIGURATION.SERVICE_SETTING.DELETE_RESERVED_DISABLED')}
              </TooltipContent>
            </Tooltip>
          );
        }
        return (
          <RemovePopup
            popupTrigger={
              <Button
                variant="ghost"
                className="px-3"
                onClick={(e) => e.stopPropagation()}
              >
                <FaRegTrashCan />
              </Button>
            }
            popupTitle={t('CONFIGURATION.SERVICE_SETTING.DELETE_TITLE')}
            popupDesc={t('CONFIGURATION.SERVICE_SETTING.DELETE_DESC')}
            popupContents={<div className="text-sm font-semibold">{service.name}</div>}
            onClickRemove={() => deleteService({ serviceName: service.name })}
          />
        );
      },
    },
  ];

  return (
    <TooltipProvider delayDuration={200}>
      <div className="space-y-2">
        <div className="flex justify-end">
          <Button onClick={() => setAddSheetOpen(true)}>
            <LuPlus className="mr-0.5" />
            {t('CONFIGURATION.SERVICE_SETTING.NEW_SERVICE') || 'New Service'}
          </Button>
        </div>
        <div className="overflow-hidden border rounded-md">
          <DataTable
            autoResize={true}
            columns={columns}
            data={rows}
            emptyMessage={t('COMMON.NO_DATA')}
          />
        </div>
        <ServiceAddSheet open={isAddSheetOpen} onOpenChange={setAddSheetOpen} />
      </div>
    </TooltipProvider>
  );
};
