import { RxExclamationTriangle } from 'react-icons/rx';
import { Alert, AlertTitle, AlertDescription } from '../components';

// export interface NotFoundProps {}

export const NotFound404 = () =>
  // {}: NotFoundProps
  {
    return (
      <div className="flex items-center justify-center h-full p-5">
        <Alert className="w-full sm:w-2/3 lg:w-5/12 ">
          <RxExclamationTriangle />
          <AlertTitle className="font-semibold">404 Page Not Found</AlertTitle>
          <AlertDescription>
            <a className="underline" href="/">
              Back to main page.
            </a>
          </AlertDescription>
        </Alert>
      </div>
    );
  };
