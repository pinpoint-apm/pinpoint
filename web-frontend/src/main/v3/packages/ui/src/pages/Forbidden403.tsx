import { useTranslation } from 'react-i18next';
import { RxExclamationTriangle } from 'react-icons/rx';
import { Alert, AlertTitle, AlertDescription } from '../components';

/**
 * 조회가 403을 받아 화면에 그릴 것이 아무것도 남지 않은 페이지. (이슈 #10744)
 *
 * **페이지가 자기 본문 자리에서 그린다**(아웃렛에서 화면을 통째로 갈아 끼우지 않는다).
 * 화면을 덮으면 그 화면의 헤더에 있는 application 선택 박스와 날짜 선택기까지 사라져,
 * 사용자가 볼 수 있는 다른 대상으로 옮겨 갈 방법이 없어진다 — 사이드 네비게이션의
 * 전역 검색(`GlobalSearch`)은 메뉴와 transaction id만 찾으므로 대안이 되지 않는다.
 *
 * 이 상태에 들어가는 화면은 `useIsForbiddenPath`를 불러 이 안내를 그리는 화면이다. 그중
 * `coversPageOnForbidden`이 빼는 화면(map 계열)은 덮지 않는다. 훅을 부르지 않는 화면은 403을
 * 받아도 덮이지 않고, 조회도 캐시에 남는다.
 */
export const Forbidden403 = () => {
  const { t } = useTranslation();

  return (
    // flex-1은 페이지 본문(헤더 아래)으로 쓰일 때 남은 높이를 채우고, h-full은 flex 컨테이너가
    // 아닌 부모(설정 화면) 아래에서 높이를 잡는다. 둘 중 맞는 쪽만 적용된다.
    <div className="flex flex-1 items-center justify-center h-full p-5">
      <Alert className="w-full sm:w-2/3 lg:w-5/12">
        <RxExclamationTriangle />
        <AlertTitle className="font-semibold">{t('COMMON.FORBIDDEN_TITLE')}</AlertTitle>
        <AlertDescription>{t('COMMON.FORBIDDEN_DESCRIPTION')}</AlertDescription>
      </Alert>
    </div>
  );
};
