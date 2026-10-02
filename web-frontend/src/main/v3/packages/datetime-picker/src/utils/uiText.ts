import { LocaleKey, PickerErrorCode } from '../types';
import { LabelLocale, resolveLocaleKey } from './locale';

export interface UiText {
  /** 커스텀타임 툴팁 트리거 버튼 */
  customTimesTrigger: string;
  /** 커스텀타임 툴팁 내부 제목 */
  customTimesTitle: string;
  /** Copy range 버튼(기본) */
  copyRange: string;
  /** Copy range 버튼(복사 직후 피드백) */
  copied: string;
  /** Apply 버튼 */
  apply: string;
  /**
   * 오류 사유별 기본 문구.
   * Record로 둬서 코드를 추가하면 5개 로케일 전부가 컴파일 에러로 잡히게 한다.
   * `empty`는 빈 텍스트(input)와 범위 미선택(range) 양쪽에 쓰이므로 둘 다에 맞는 표현을 쓴다.
   */
  errors: Record<PickerErrorCode, string>;
}

const UI_TEXT: Record<LabelLocale, UiText> = {
  en: {
    customTimesTrigger: 'Type custom times',
    customTimesTitle: 'Type custom times like:',
    copyRange: 'Copy range',
    copied: 'Copied!',
    apply: 'Apply',
    errors: {
      empty: 'Enter or pick a date range.',
      unparseable: "We couldn't read that date.",
      'seam-conflict': 'The range separator clashes with the date.',
      incomplete: 'Enter both ends of the range.',
      reversed: 'Start time must be earlier than end time.',
      'out-of-bounds': 'That date is outside the allowed range.',
      'range-invalid': 'That range is not allowed.',
    },
  },
  ko: {
    customTimesTrigger: '직접 시간 입력',
    customTimesTitle: '이렇게 입력해 보세요:',
    copyRange: '범위 복사',
    copied: '복사됨!',
    apply: '적용',
    errors: {
      empty: '날짜 범위를 입력하거나 선택하세요.',
      unparseable: '날짜를 알아볼 수 없어요.',
      'seam-conflict': '범위 구분자가 날짜와 겹쳐요.',
      incomplete: '범위의 시작과 끝을 모두 입력하세요.',
      reversed: '시작이 종료보다 늦을 수 없어요.',
      'out-of-bounds': '선택할 수 있는 기간을 벗어났어요.',
      'range-invalid': '선택할 수 없는 범위예요.',
    },
  },
  ja: {
    customTimesTrigger: '時間を直接入力',
    customTimesTitle: '入力例:',
    copyRange: '範囲をコピー',
    copied: 'コピーしました！',
    apply: '適用',
    errors: {
      empty: '期間を入力または選択してください。',
      unparseable: '日付を認識できません。',
      'seam-conflict': '区切り文字が日付と重なっています。',
      incomplete: '期間の開始と終了を入力してください。',
      reversed: '開始は終了より前にしてください。',
      'out-of-bounds': '選択できる期間を超えています。',
      'range-invalid': '選択できない期間です。',
    },
  },
  'zh-CN': {
    customTimesTrigger: '手动输入时间',
    customTimesTitle: '输入示例：',
    copyRange: '复制范围',
    copied: '已复制！',
    apply: '应用',
    errors: {
      empty: '请输入或选择时间范围。',
      unparseable: '无法识别该日期。',
      'seam-conflict': '分隔符与日期冲突。',
      incomplete: '请输入范围的开始和结束。',
      reversed: '开始时间必须早于结束时间。',
      'out-of-bounds': '超出可选时间范围。',
      'range-invalid': '该范围不可选择。',
    },
  },
  'zh-TW': {
    customTimesTrigger: '手動輸入時間',
    customTimesTitle: '輸入範例：',
    copyRange: '複製範圍',
    copied: '已複製！',
    apply: '套用',
    errors: {
      empty: '請輸入或選擇時間範圍。',
      unparseable: '無法辨識該日期。',
      'seam-conflict': '分隔符與日期衝突。',
      incomplete: '請輸入範圍的開始與結束。',
      reversed: '開始時間必須早於結束時間。',
      'out-of-bounds': '超出可選時間範圍。',
      'range-invalid': '該範圍不可選擇。',
    },
  },
};

/** 로케일별 UI 문구를 반환한다. ('zh'=간체 별칭, 미지원 언어는 en 폴백) */
export const getUiText = (localeKey: LocaleKey): UiText => UI_TEXT[resolveLocaleKey(localeKey)];
