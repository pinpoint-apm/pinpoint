import { MdOutlineEmail, MdOutlineSms, MdOutlineWebhook } from 'react-icons/md';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

const ICONS = {
  EMAIL: MdOutlineEmail,
  SMS: MdOutlineSms,
  WEBHOOK: MdOutlineWebhook,
} as const;

/**
 * Icon for a channel's delivery method. Method types come from the server, so an
 * unknown one renders nothing rather than a gap where an icon was expected.
 */
export const ChannelMethodIcon = ({ type, className }: { type: string; className?: string }) => {
  const Icon = ICONS[type as keyof typeof ICONS];
  return Icon ? <Icon className={cn('h-4 w-4', className)} /> : null;
};
