import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

export const useGetConfiguration = <T>() => {
  const { data, error, isLoading, refetch } = useQuery<T>({
    queryKey: [END_POINTS.CONFIGURATION],
    queryFn: getConfiguration,
  });
  return { data, error, isLoading, refetch };
};

export const getConfiguration = async <T>(): Promise<T> => {
  try {
    const response = await fetch(`${END_POINTS.CONFIGURATION}`);

    if (!response?.ok) {
      const errorData = await response.json();
      throw new Error(errorData?.message);
    }

    return await response.json();
  } catch (error) {
    throw error;
  }
};
