import { useTranslation } from 'react-i18next';
import { Webhook } from '@pinpoint-fe/ui/src/constants';
import { DataTable } from '../../components/DataTable/DataTable';
import { cn } from '../../lib/utils';
import { WebhookTableColumnsProps, webhookTableColumns } from './webhookTableColumns';

export interface WebhookTableProps extends WebhookTableColumnsProps {
  data?: Webhook.Response | null;
  onClickRowItem?: (data: Webhook.WebhookData) => void;
}

export const WebhookTable = ({ data, onClickRowItem, ...props }: WebhookTableProps) => {
  const { t } = useTranslation();
  const columns = webhookTableColumns(props, t);
  return (
    <div className={cn('rounded-md border')}>
      <DataTable
        autoResize
        columns={columns}
        data={data || []}
        emptyMessage={
          data?.length === 0 ? undefined : t('CONFIGURATION.COMMON.SELECT_APPLICATION_FIRST')
        }
        onClickRow={(data) => {
          onClickRowItem?.(data.original);
        }}
      />
    </div>
  );
};
