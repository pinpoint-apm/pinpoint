import type { TFunction } from 'i18next';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { ColumnDef } from '@tanstack/react-table';
import { Badge } from '@pinpoint-fe/ui/src/components/ui';
import { cn } from '@pinpoint-fe/ui/src/lib';
import { ChannelMethodIcon } from './ChannelMethodIcon';
import { AlarmV2TableActions } from './AlarmV2TableActions';

export interface AlarmV2ChannelTableColumnsProps {
  disabled?: boolean;
  onClickEdit?: (data: AlarmV2Channel.ChannelData) => void;
  onClickDelete?: (data: AlarmV2Channel.ChannelData) => void;
}

export const alarmV2ChannelTableColumns = (
  { disabled, onClickEdit, onClickDelete }: AlarmV2ChannelTableColumnsProps,
  t: TFunction,
): ColumnDef<AlarmV2Channel.ChannelData>[] => [
  {
    accessorKey: 'channelName',
    header: () => t('CONFIGURATION.COMMON.NAME'),
    cell: (props) => <span className="font-medium">{props.getValue() as string}</span>,
    // No width: this is the column that should absorb the leftover space, since
    // every other column is capped below.
  },
  {
    accessorKey: 'methodType',
    header: () => t('CONFIGURATION.COMMON.TYPE'),
    cell: (props) => (
      <Badge variant="outline" className="text-xs font-medium">
        <span className="flex items-center">
          <ChannelMethodIcon type={props.getValue() as string} className="mr-1" />
          {props.getValue() as string}
        </span>
      </Badge>
    ),
    meta: {
      headerClassName: 'w-36',
    },
  },
  {
    accessorKey: 'destination',
    header: () => t('CONFIGURATION.ALARM_V2.DESTINATION'),
    cell: (props) => {
      const row = props.row.original;
      const destination =
        row.methodType === 'WEBHOOK'
          ? (row.webhookAlias ?? row.webhookUrl ?? (props.getValue() as string))
          : (props.getValue() as string);
      return (
        <span className="block truncate text-muted-foreground" title={destination}>
          {destination}
        </span>
      );
    },
    meta: {
      // Capped and ellipsised: a long webhook URL must not eat the name column.
      headerClassName: 'w-64',
      cellClassName: 'max-w-64',
    },
  },
  {
    accessorKey: 'affectedRuleCount',
    header: () => t('CONFIGURATION.ALARM_V2.USED_BY'),
    cell: (props) => {
      const templateCount = props.row.original.templateCount ?? 0;
      const affectedRuleCount = props.row.original.affectedRuleCount ?? 0;
      const enabledAffectedRuleCount = props.row.original.enabledAffectedRuleCount ?? 0;

      // Fixed-width numeric slots keep the three counts lined up down the column.
      return (
        <span className="flex items-baseline text-muted-foreground">
          {(
            [
              ['CONFIGURATION.ALARM_V2.TEMPLATES_SHORT', templateCount],
              ['CONFIGURATION.ALARM_V2.RULES', affectedRuleCount],
              ['CONFIGURATION.ALARM_V2.ENABLED_SHORT', enabledAffectedRuleCount],
            ] as const
          ).map(([labelKey, value], index) => (
            <span
              key={labelKey}
              className={cn(
                'flex items-baseline gap-1.5 pr-3',
                index > 0 && 'border-l border-border pl-3',
              )}
            >
              <span>{t(labelKey)}</span>
              <span className="w-6 text-right tabular-nums">{value}</span>
            </span>
          ))}
        </span>
      );
    },
    meta: {
      headerClassName: 'w-72',
    },
  },
  {
    accessorKey: 'actions',
    header: () => t('CONFIGURATION.ALARM_V2.ACTIONS'),
    cell: (props) => {
      return (
        <AlarmV2TableActions
          data={props.row.original}
          actions={[
            {
              label: t('COMMON.EDIT'),
              disabled,
              onClick: onClickEdit,
            },
            {
              label: t('COMMON.DELETE'),
              disabled,
              destructive: true,
              separatorBefore: true,
              onClick: onClickDelete,
            },
          ]}
        />
      );
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: 'text-center px-4',
    },
  },
];
