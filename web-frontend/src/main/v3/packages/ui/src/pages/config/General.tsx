import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '../../components';
import { useLanguage, SupportLanguageType } from '@pinpoint-fe/hooks';
import { useTranslation } from 'react-i18next';

export interface GeneralPageProps {}

export const GeneralPage = () => {
  const [language, setLanguage] = useLanguage();
  const { t } = useTranslation();

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">General</h3>
        <p className="text-sm text-muted-foreground">{t('CONFIGURATION.GENERAL.DESC')}</p>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <div className="space-y-2">
        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          Language
        </label>
        <Select
          value={language}
          onValueChange={(value) => setLanguage(value as SupportLanguageType)}
        >
          <SelectTrigger className="w-52">
            <SelectValue placeholder="Select language" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>Languages</SelectLabel>
              <SelectItem value="en">English</SelectItem>
              <SelectItem value="ko">한국어</SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
        {/* <p id=":r61:-form-item-description" className="text-[0.8rem] text-muted-foreground">
          This is your public display name. It can be your real name or a pseudonym. You can only
          change this once every 30 days.
        </p> */}
      </div>
    </div>
  );
};
