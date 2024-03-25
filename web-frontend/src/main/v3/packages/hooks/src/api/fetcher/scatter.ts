import { GetScatter } from '@pinpoint-fe/constants';
import { getScatterData } from '@pinpoint-fe/utils';
import { Fetcher } from 'swr';

type GetScatterData = {
  data: GetScatter.Response;
  scatterData: ReturnType<typeof getScatterData>;
};

export const scatterFetcher: Fetcher<GetScatterData, string> = async (url: string) => {
  const res = await fetch(`${url}`);
  if (!res.ok) {
    throw new Error('Network response was not ok');
  }
  const result = await res.json();
  return { data: result, scatterData: getScatterData(result) };
};
