import { SupportLanguageType, useLanguage } from '@pinpoint-fe/ui/src/hooks';

export interface HelpDocumentProps {}

export const HelpDocument = () => {
  const [language] = useLanguage();
  const documentList: Record<SupportLanguageType, { title: string; href: string }[]> = {
    ko: [
      {
        title: 'Pinpoint 개발자가 작성한 Pinpoint 기술문서',
        href: 'https://d2.naver.com/helloworld/1194202',
      },
      {
        title: '설치 가이드 동영상 강좌 1',
        href: 'https://www.youtube.com/watch?v=hrvKaEaDEGs',
      },
      {
        title: '설치 가이드 동영상 강좌 2',
        href: 'https://www.youtube.com/watch?v=fliKPGHGXK4',
      },
      {
        title: 'Alarm 가이드',
        href: 'https://pinpoint-apm.gitbook.io/pinpoint/documents/alarm',
      },
    ],
    en: [
      {
        title: 'Quick start guide',
        href: 'https://github.com/naver/pinpoint/blob/master/quickstart/README.md',
      },
      {
        title: 'Technical overview of Pinpoint',
        href: 'https://github.com/naver/pinpoint/wiki/Technical-Overview-Of-Pinpoint',
      },
      {
        title: 'Notes on Jetty Plugin for Pinpoint',
        href: 'https://github.com/cijung/Docs/blob/master/JettyPluginNotes.md',
      },
      {
        title: 'About alarm',
        href: 'https://pinpoint-apm.gitbook.io/pinpoint/documents/alarm',
      },
    ],
  };

  return (
    <ul className="px-5 text-sm">
      {documentList[language].map(({ title, href }, i) => (
        <li key={i} className="py-2 list-disc">
          <a
            target="_blank"
            href={href}
            className="text-blue-500 hover:underline hover:text-blue-600"
          >
            {title}
          </a>
        </li>
      ))}
    </ul>
  );
};
