import { ToastContainer, ToastContainerProps, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const defaultToastContainerProps: ToastContainerProps = {
  autoClose: 2500,
  closeOnClick: false,
};

export { toast, ToastContainer, defaultToastContainerProps };
