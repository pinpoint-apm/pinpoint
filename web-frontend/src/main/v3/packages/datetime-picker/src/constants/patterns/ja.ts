import { TimePattern } from '../../types';

// 일본어 상대시간. 접두사 '過去'(라벨) 및 접미사 '前'(전)은 선택적으로 허용한다.
export const timePattern: TimePattern = {
  second: /^(過去)?\s*(\d+)\s*秒(\s*前)?$/,
  minute: /^(過去)?\s*(\d+)\s*分(\s*前)?$/,
  hour: /^(過去)?\s*(\d+)\s*時間(\s*前)?$/,
  day: /^(過去)?\s*(\d+)\s*日(\s*前)?$/,
  week: /^(過去)?\s*(\d+)\s*週(間)?(\s*前)?$/,
  month: /^(過去)?\s*(\d+)\s*(か月|ヶ月|カ月|ケ月)(\s*前)?$/,
  year: /^(過去)?\s*(\d+)\s*年(\s*前)?$/,
  yesterday: /^昨日$/,
  today: /^今日$/,
  lastMonth: /^先月$/,
  lastYear: /^(去年|昨年)$/,
  unixTimestampRange: /^\d{13}\s*[^\d]\s*\d{13}$/,
};
