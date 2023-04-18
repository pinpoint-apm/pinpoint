import useSWR from 'swr';

/**
 * Re-usable SWR api implementation.
 *
 * @param {string} url
 * @param {object} params
 * @returns object
 */
function useApi<T = {}>(url: string, params: T) {
  const usp = new URLSearchParams(params);

  // Create a stable key for SWR
  usp.sort();
  const qs = usp.toString();

  const { data, error } = useSWR(`${url}?${qs}`);

  return {
    loading: !error && !data,
    data,
    error,
  };
}

export default useApi;