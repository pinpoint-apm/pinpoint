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
