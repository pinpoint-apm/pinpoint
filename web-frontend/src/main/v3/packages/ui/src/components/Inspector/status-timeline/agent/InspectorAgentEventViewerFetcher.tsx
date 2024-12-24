import { useGetInspectorAgentEvents } from '@pinpoint-fe/ui/hooks';
import { format } from 'date-fns';
import { DataTable } from '../../../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { InspectorAgentEvents } from '@pinpoint-fe/ui/constants';

export interface InspectorAgentEventViewerFetcherProps {
  eventRange: number[];
  className?: string;
}

export const InspectorAgentEventViewerFetcher = ({
  eventRange,
  className,
}: InspectorAgentEventViewerFetcherProps) => {
  const { data } = useGetInspectorAgentEvents({ range: eventRange });

  return (
    <div className={className}>
      <DataTable tableClassName="text-xs" columns={columns} data={data || []} />
    </div>
  );
};

const columns: ColumnDef<InspectorAgentEvents.AgentEventData>[] = [
  {
    accessorKey: 'eventTimestamp',
    header: 'Time',
    cell: (props) => {
      const timestamp = props.getValue() as number;

      return format(timestamp, 'yyyy.MM.dd HH:mm:ss XXX');
    },
  },
  {
    accessorKey: 'eventTypeDesc',
    header: 'Description',
  },
  {
    accessorKey: 'eventMessage',
    header: 'Message',
  },
];
