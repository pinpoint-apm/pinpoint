import { Webhook } from '@pinpoint-fe/ui/constants';
import { DataTable } from '../../components/DataTable/DataTable';
import { cn } from '../../lib/utils';
import { WebhookTableColumnsProps, webhookTableColumns } from './webhookTableColumns';

export interface WebhookTableProps extends WebhookTableColumnsProps {
  data?: Webhook.Response | null;
  onClickRowItem?: (data: Webhook.WebhookData) => void;
}

export const WebhookTable = ({ data, onClickRowItem, ...props }: WebhookTableProps) => {
  const columns = webhookTableColumns(props);
  return (
    <div className={cn('rounded-md border')}>
      <DataTable
        autoResize
        columns={columns}
        data={data || []}
        emptyMessage={data?.length === 0 ? undefined : 'Select your application first.'}
        onClickRow={(data) => {
          onClickRowItem?.(data.original);
        }}
      />
    </div>
  );
};
