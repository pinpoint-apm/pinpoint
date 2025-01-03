import { useTranslation } from 'react-i18next';
import { Webhook } from '@pinpoint-fe/ui/constants';
import { CellContext, ColumnDef } from '@tanstack/react-table';
import { RxDotsVertical } from 'react-icons/rx';
import { Button } from '../../components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../../components/ui/dropdown-menu';

export interface WebhookTableColumnsProps {
  onClickEdit?: (data?: Webhook.WebhookData) => void;
  onClickDelete?: (data?: Webhook.WebhookData) => void;
}

export const webhookTableColumns: (
  props: WebhookTableColumnsProps,
) => ColumnDef<Webhook.WebhookData>[] = ({ onClickEdit, onClickDelete }) => [
  {
    accessorKey: 'alias',
    header: () => 'Alias',
    cell: (props) => props.getValue(),
  },

  {
    accessorKey: 'url',
    header: () => 'Url',
    cell: (props) => props.getValue(),
  },
  {
    accessorKey: 'actions',
    header: () => 'Actions',
    cell: (props) => {
      return <ActionButtons cellProps={props} columnProps={{ onClickEdit, onClickDelete }} />;
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: 'text-center px-4',
    },
  },
];

const ActionButtons = ({
  cellProps,
  columnProps,
}: {
  cellProps: CellContext<Webhook.WebhookData, unknown>;
  columnProps: WebhookTableColumnsProps;
}) => {
  const { t } = useTranslation();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" className="px-2 py-1">
          <RxDotsVertical />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem
          className="hover:cursor-pointer"
          onClick={(e) => {
            e.stopPropagation();
            columnProps.onClickEdit?.(cellProps.row.original);
          }}
        >
          {t('COMMON.EDIT')}
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          className="text-destructive focus:text-destructive hover:cursor-pointer"
          onClick={(e) => {
            e.stopPropagation();
            columnProps.onClickDelete?.(cellProps.row.original);
          }}
        >
          {t('COMMON.DELETE')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
