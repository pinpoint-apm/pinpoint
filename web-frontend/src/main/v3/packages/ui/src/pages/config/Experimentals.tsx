import { useTranslation } from 'react-i18next';
import { Checkbox } from '../../components/ui/checkbox';
import { EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useConfiguration } from '../../hooks/utility/useConfiguration';
import { useExperimentals } from '../../hooks/utility/useExperimentals';

export interface ExperimentalPageProps {
  /**
   * `enableServiceMap` 항목을 이 화면에 노출할지. 기본은 노출한다.
   *
   * 노출 여부를 저장소(naver 등)나 배포 환경에 따라 다르게 두기 위한 prop이다. 판단 기준을
   * 이 패키지가 알 필요는 없으므로(그런 값이 프론트엔드에 없다) 렌더하는 쪽에서 정해 내려준다.
   *
   * **UI 노출만 가른다.** 값 자체는 `pickEnableServiceMap`이 정하므로, 여기서 숨겨도 이미
   * localStorage에 저장된 값이 있으면 그 값이 계속 쓰인다. 숨긴 화면에는 되돌릴 UI가 없다는
   * 뜻이므로, 값까지 서버 설정으로 고정해야 한다면 그건 별도 처리가 필요하다.
   */
  showEnableServiceMap?: boolean;
}

export const ExperimentalPage = ({ showEnableServiceMap = true }: ExperimentalPageProps) => {
  const configuration = useConfiguration();
  const { t } = useTranslation();
  const experimentalMap = useExperimentals(configuration);

  // `showEnableServiceMap`이 false면 이 항목은 어떤 경우에도 렌더하지 않는다. configuration이
  // 켜 놓았든 localStorage에 저장된 값이 있든 상관없다 — 그 값들은 항목을 그릴지가 아니라
  // 체크 상태만 정한다.
  const experimentalKeys = Object.values(EXPERIMENTAL_CONFIG_KEYS).filter((key) => {
    if (key === EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP) {
      return showEnableServiceMap;
    }
    return true;
  });

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Experimental</h3>
        <p className="text-sm text-muted-foreground">{t('CONFIGURATION.EXPERIMENTAL.DESC')}</p>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <div className="space-y-2">
        {experimentalKeys.map((key) => {
          return (
            <div className="flex items-center space-x-2" key={key}>
              <Checkbox
                id={key}
                checked={experimentalMap[key].value}
                onCheckedChange={(value) => {
                  experimentalMap[key].setter(!!value);
                }}
              />
              <label htmlFor={key} className="text-sm font-medium cursor-pointer">
                {experimentalMap[key].description}
              </label>
            </div>
          );
        })}
      </div>
    </div>
  );
};
