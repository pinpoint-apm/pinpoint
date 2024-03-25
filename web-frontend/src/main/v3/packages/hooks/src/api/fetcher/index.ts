export {};
// import { Fetcher } from 'swr';
// export const swrFetcher: Fetcher = (url, params) => {
//   const queryParamString = new URLSearchParams(params)?.toString();
//   const urlWithQueryParams = queryParamString ? `${url}?${queryParamString}` : url;

//   return fetch(`${urlWithQueryParams}`)
//     .then((res) => {
//       if (!res.ok) {
//         throw new Error('Network response was not ok');
//       }
//       return res.json();
//     })
//     .then((data) => data)
//     .catch((error) => {
//       // console.error('Error parsing JSON:', error);
//       // return {};
//       // or
//       // return [];
//     });
// };
