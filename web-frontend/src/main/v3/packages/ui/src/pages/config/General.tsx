import { DATE_FORMATS } from '@pinpoint-fe/ui/src/constants';
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '../../components';
import {
  useLanguage,
  SupportLanguageType,
  useDateFormat,
  useTimezone,
} from '@pinpoint-fe/ui/src/hooks';
import { format } from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';

export const GeneralPage = () => {
  const [language, setLanguage] = useLanguage();
  const [dateFormat, setDateFormat] = useDateFormat();
  const [timezone, setTimezone] = useTimezone();
  const { t } = useTranslation();
  const now = new Date();

  return (
    <div className="pl-1 space-y-6">
      <div>
        <h3 className="text-lg font-semibold">General</h3>
        <p className="text-sm text-muted-foreground">{t('CONFIGURATION.GENERAL.DESC')}</p>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <div className="space-y-2 ">
        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          {t('CONFIGURATION.GENERAL.LANGUAGE')}
        </label>
        <Select
          value={language}
          onValueChange={(value) => setLanguage(value as SupportLanguageType)}
        >
          <SelectTrigger className="w-72">
            <SelectValue placeholder="Select language" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>{t('CONFIGURATION.GENERAL.LANGUAGE')}</SelectLabel>
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
      <div className="space-y-2">
        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          {t('CONFIGURATION.GENERAL.DATE_FORMAT')}
        </label>
        <Select
          value={dateFormat}
          onValueChange={(value: keyof typeof DATE_FORMATS) => {
            setDateFormat(DATE_FORMATS[value]);
          }}
        >
          <SelectTrigger className="w-72">
            <SelectValue placeholder="Select language" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>{t('CONFIGURATION.GENERAL.DATE_FORMAT')}</SelectLabel>
              {Object.keys(DATE_FORMATS)
                .filter((v) => isNaN(Number(v)))
                .map((key, i) => {
                  return (
                    <SelectItem key={i} value={key}>
                      {format(now, key)}
                    </SelectItem>
                  );
                })}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          Timezone
        </label>
        <Select value={timezone} onValueChange={(value) => setTimezone(value)}>
          <SelectTrigger className="w-72">
            <SelectValue placeholder="Select a timezone" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>North America</SelectLabel>
              <SelectItem value="America/New_York">(GMT-05:00) New York (EST)</SelectItem>
              <SelectItem value="America/Chicago">(GMT-06:00) Chicago (CST)</SelectItem>
              <SelectItem value="America/Denver">(GMT-07:00) Denver (MST)</SelectItem>
              <SelectItem value="America/Los_Angeles">(GMT-08:00) Los Angeles (PST)</SelectItem>
              <SelectItem value="America/Anchorage">(GMT-09:00) Anchorage (AKST)</SelectItem>
              <SelectItem value="Pacific/Honolulu">(GMT-10:00) Honolulu (HST)</SelectItem>
            </SelectGroup>
            <SelectGroup>
              <SelectLabel>Europe & Africa</SelectLabel>
              <SelectItem value="Europe/Lisbon">(GMT+00:00) Lisbon (WET)</SelectItem>
              <SelectItem value="Europe/London">(GMT+00:00) London (GMT)</SelectItem>
              <SelectItem value="Europe/Berlin">(GMT+01:00) Berlin (CET)</SelectItem>
              <SelectItem value="Europe/Paris">(GMT+01:00) Paris (CET)</SelectItem>
              <SelectItem value="Africa/Lagos">(GMT+01:00) Lagos (WAT)</SelectItem>
              <SelectItem value="Europe/Helsinki">(GMT+02:00) Helsinki (EET)</SelectItem>
              <SelectItem value="Africa/Cairo">(GMT+02:00) Cairo (EET)</SelectItem>
              <SelectItem value="Africa/Johannesburg">(GMT+02:00) Johannesburg (SAST)</SelectItem>
              <SelectItem value="Europe/Moscow">(GMT+03:00) Moscow (MSK)</SelectItem>
            </SelectGroup>
            <SelectGroup>
              <SelectLabel>Asia</SelectLabel>
              <SelectItem value="Asia/Dubai">(GMT+04:00) Dubai (GST)</SelectItem>
              <SelectItem value="Asia/Kolkata">(GMT+05:30) Kolkata (IST)</SelectItem>
              <SelectItem value="Asia/Bangkok">(GMT+07:00) Bangkok (ICT)</SelectItem>
              <SelectItem value="Asia/Jakarta">(GMT+07:00) Jakarta (WIB)</SelectItem>
              <SelectItem value="Asia/Shanghai">(GMT+08:00) Shanghai (CST)</SelectItem>
              <SelectItem value="Asia/Hong_Kong">(GMT+08:00) Hong Kong (HKT)</SelectItem>
              <SelectItem value="Asia/Singapore">(GMT+08:00) Singapore (SGT)</SelectItem>
              <SelectItem value="Asia/Seoul">(GMT+09:00) Seoul (KST)</SelectItem>
              <SelectItem value="Asia/Tokyo">(GMT+09:00) Tokyo (JST)</SelectItem>
              <SelectItem value="Asia/Vladivostok">(GMT+10:00) Vladivostok (VLAT)</SelectItem>
            </SelectGroup>
            <SelectGroup>
              <SelectLabel>Australia & Pacific</SelectLabel>
              <SelectItem value="Australia/Perth">(GMT+08:00) Perth (AWST)</SelectItem>
              <SelectItem value="Australia/Adelaide">(GMT+09:30) Adelaide (ACST)</SelectItem>
              <SelectItem value="Australia/Sydney">(GMT+10:00) Sydney (AEST)</SelectItem>
              <SelectItem value="Pacific/Auckland">(GMT+12:00) Auckland (NZST)</SelectItem>
              <SelectItem value="Pacific/Fiji">(GMT+12:00) Fiji (FJT)</SelectItem>
            </SelectGroup>
            <SelectGroup>
              <SelectLabel>South America</SelectLabel>
              <SelectItem value="America/Sao_Paulo">(GMT-03:00) São Paulo (BRT)</SelectItem>
              <SelectItem value="America/Argentina/Buenos_Aires">
                (GMT-03:00) Buenos Aires (ART)
              </SelectItem>
              <SelectItem value="America/Santiago">(GMT-04:00) Santiago (CLT)</SelectItem>
              <SelectItem value="America/La_Paz">(GMT-04:00) La Paz (BOT)</SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>
    </div>
  );
};
