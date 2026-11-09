import { useGetInspectorAgentEvents, useTimezone } from '@pinpoint-fe/ui/src/hooks';
import { formatInTimeZone } from 'date-fns-tz';
import { DataTable } from '../../../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { InspectorAgentEvents } from '@pinpoint-fe/ui/src/constants';

export interface InspectorAgentEventViewerFetcherProps {
  eventRange: number[];
  className?: string;
}

export const InspectorAgentEventViewerFetcher = ({
  eventRange,
  className,
}: InspectorAgentEventViewerFetcherProps) => {
  const { data } = useGetInspectorAgentEvents({ range: eventRange });
  const [timezone] = useTimezone();

  return (
    <div className={className}>
      {/* 메시지가 여러 줄이면 행이 길어지므로, 시간·설명을 행 가운데가 아니라 덤프 첫 줄과 나란히 둔다. */}
      <DataTable
        tableClassName="text-xs [&_td]:align-top"
        columns={columns(timezone)}
        data={data || []}
      />
    </div>
  );
};

const columns = (timezone: string): ColumnDef<InspectorAgentEvents.AgentEventData>[] => [
  {
    accessorKey: 'eventTimestamp',
    header: 'Time',
    cell: (props) => {
      const timestamp = props.getValue() as number;

      return formatInTimeZone(timestamp, timezone, 'yyyy.MM.dd HH:mm:ss XXX');
    },
  },
  {
    accessorKey: 'eventTypeDesc',
    header: 'Description',
  },
  {
    accessorKey: 'eventMessage',
    header: 'Message',
    meta: {
      // deadlock 이벤트의 메시지는 스레드 덤프 전체이고 줄바꿈(\n)과 들여쓰기(\t)로 모양을 갖춘다(#8770).
      // HTML은 기본으로 공백을 하나로 접으므로 pre-wrap으로 살린다. <br>로 바꿔 HTML로 넣지 않는 것은
      // 메시지를 마크업으로 해석하지 않기 위해서다. 스택 한 줄은 공백 없이 길어서 wrap-anywhere로
      // 칸 안에서 접는다(break-words는 표의 최소 폭 계산에 반영되지 않아 표가 팝업 밖으로 넓어진다).
      cellClassName: 'whitespace-pre-wrap wrap-anywhere font-mono',
    },
  },
];
