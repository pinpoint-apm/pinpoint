import { PiSpinner } from 'react-icons/pi';
import { ButtonProps, Button } from '../../components/ui';
import { cn } from '../../lib';

export interface LoadingButtonProps extends ButtonProps {
  pending?: boolean;
  spinnerClassName?: string;
}

export const LoadingButton = ({
  pending,
  spinnerClassName,
  children,
  ...props
}: LoadingButtonProps) => {
  return (
    <div className="relative">
      <Button disabled={pending} {...props}>
        <span className={cn({ 'blur-xs': pending })}>{children}</span>
      </Button>
      {pending && (
        <div className="absolute top-0 left-0 flex items-center justify-center w-full h-full">
          <PiSpinner className={cn('animate-spin', spinnerClassName)} />
        </div>
      )}
    </div>
  );
};
