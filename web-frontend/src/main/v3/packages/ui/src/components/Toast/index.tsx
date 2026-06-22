import React from 'react';
import {
  ToastContainer,
  ToastContainerProps,
  toast,
  ToastOptions,
  ToastContent,
} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { toastCountAtom } from '@pinpoint-fe/ui/src/atoms';
import { useAtom } from 'jotai';
import { Button } from '../../components';
import { cn } from '../../lib/utils';

const defaultToastContainerProps: ToastContainerProps = {
  className: 'text-sm',
  autoClose: 2500,
  closeOnClick: false,
  stacked: true,
};

const ReactToastContainer = () => {
  const [show, setShow] = React.useState(false);
  const [toastCount, setToastCount] = useAtom(toastCountAtom);

  const handleClearToasts = () => {
    toast.dismiss();
    setToastCount(0);
    setShow(false);
  };

  return (
    <div onMouseEnter={() => toastCount > 1 && setShow(true)} onMouseLeave={() => setShow(false)}>
      <div
        style={{
          position: 'absolute',
          top: 3,
          right: 275,
          zIndex: 9999,
          // Radix modal(Sheet/Dialog)이 열리면 body에 pointer-events:none이 걸리므로,
          // 그 위에서도 "Clear All" 버튼을 클릭할 수 있도록 명시적으로 auto로 복원한다.
          pointerEvents: 'auto',
        }}
      >
        {show && (
          <Button
            variant={'outline'}
            size={'xs'}
            onClick={handleClearToasts}
            className="rounded-xl"
          >
            Clear All
          </Button>
        )}
      </div>
      <ToastContainer
        {...defaultToastContainerProps}
        style={{
          position: 'absolute',
          zIndex: 9998,
        }}
      />
    </div>
  );
};

function useReactToastifyToast() {
  const [, setToastCount] = useAtom(toastCountAtom);

  const createToast = (type: 'loading' | 'success' | 'error' | 'info' | 'warning' | 'warn') => {
    return (content: ToastContent, options?: ToastOptions) => {
      toast[type](content, {
        ...defaultToastContainerProps,
        ...options,
        // Radix modal(Sheet/Dialog) 오픈 시 body의 pointer-events:none 때문에 토스트가
        // hover/클릭 불가가 되는 것을 막기 위해 토스트 자체는 항상 클릭 가능하게 둔다.
        className: cn('pointer-events-auto', defaultToastContainerProps.className, options?.className),
        onOpen: (data) => {
          setToastCount((prev) => prev + 1);
          options?.onOpen?.(data);
        },
        onClose: (data) => {
          setToastCount((prev) => {
            if (prev === 0) {
              return prev;
            }
            return prev - 1;
          });
          options?.onClose?.(data);
        },
      });
    };
  };

  return {
    loading: createToast('loading'),
    success: createToast('success'),
    error: createToast('error'),
    info: createToast('info'),
    warn: createToast('warn'),
    warning: createToast('warning'),
  };
}

export { useReactToastifyToast, ReactToastContainer };
