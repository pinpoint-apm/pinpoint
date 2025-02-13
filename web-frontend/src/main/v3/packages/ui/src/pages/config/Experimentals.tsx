import { useTranslation } from 'react-i18next';
import { Checkbox } from '../../components';
import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useExperimentals } from '@pinpoint-fe/ui/src/hooks';

export interface ExperimentalPageProps {
  configuration?: Configuration;
}

export const ExperimentalPage = ({ configuration }: ExperimentalPageProps) => {
  const { t } = useTranslation();
  const experimentalMap = useExperimentals(configuration);

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
        {Object.values(EXPERIMENTAL_CONFIG_KEYS).map((key) => {
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
