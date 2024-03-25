import { IconBaseProps } from 'react-icons/lib';
import { FaComments, FaGithub, FaUsers } from 'react-icons/fa';

export interface HelpCommunityProps {}

export const HelpCommunity = () => {
  const communityList = [
    {
      title: 'FAQ',
      href: 'https://github.com/naver/pinpoint/wiki/FAQ',
      icon: (props: IconBaseProps) => <FaComments {...props} />,
    },
    {
      title: 'Issues',
      href: 'https://github.com/pinpoint-apm/pinpoint/issues',
      icon: (props: IconBaseProps) => <FaGithub {...props} />,
    },
    {
      title: 'User Community',
      href: 'https://groups.google.com/forum/#!forum/pinpoint_user',
      icon: (props: IconBaseProps) => <FaUsers {...props} />,
    },
  ];
  return (
    <ul className="flex gap-2.5 text-sm">
      {communityList.map(({ title, href, icon }, i) => (
        <li key={i} className="py-2">
          <a
            target="_blank"
            href={href}
            className="flex flex-col items-center justify-center w-40 gap-2 px-4 py-3 border-2 rounded-md border-muted bg-popover hover:bg-accent hover:text-accent-foreground"
          >
            {icon({ className: 'w-6 h-6' })}
            {title}
          </a>
        </li>
      ))}
    </ul>
  );
};
