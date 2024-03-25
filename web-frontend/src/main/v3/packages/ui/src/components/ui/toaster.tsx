import {
  Toast,
  ToastClose,
  ToastDescription,
  ToastProvider,
  ToastTitle,
  ToastViewport,
} from './toast';
import * as ToastPrimitives from '@radix-ui/react-toast';
import { useToast } from '../../lib/use-toast';

export function Toaster(prop: ToastPrimitives.ToastProviderProps) {
  const { toasts } = useToast();

  return (
    <ToastProvider {...prop}>
      {toasts.map(function ({ id, title, description, action, ...props }) {
        return (
          <Toast key={id} {...props}>
            <div className="grid gap-1">
              {title && <ToastTitle>{title}</ToastTitle>}
              {description && <ToastDescription>{description}</ToastDescription>}
            </div>
            {action}
            <ToastClose />
          </Toast>
        );
      })}
      <ToastViewport />
    </ToastProvider>
  );
}
