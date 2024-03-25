import { useLocation, useNavigate } from 'react-router-dom';

import { Separator } from '..';
import { cn } from '../../lib';

export type ConfigMenu = {
  title: string;
  desc?: string;
  menus: {
    path: string | string[];
    href: string;
    name: string;
  }[];
};

export interface LayoutWithConfigurationProps {
  children: React.ReactNode;
  configMenu?: ConfigMenu;
}

export const LayoutWithConfiguration = ({ children, configMenu }: LayoutWithConfigurationProps) => {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  return (
    <div className="h-full p-10">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">{configMenu?.title}</h2>
        <p className="text-muted-foreground">{configMenu?.desc}</p>
      </div>
      <Separator className="my-6" />
      <div className="flex flex-row space-x-12">
        <aside className="-mx-4 lg:w-1/5">
          {configMenu?.menus.map((item, i) => {
            return (
              <nav key={i} className="flex space-x-2 lg:flex-col lg:space-x-0 lg:space-y-1">
                <a
                  className={cn(
                    'cursor-pointer inline-flex items-center whitespace-nowrap rounded-md text-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 hover:text-accent-foreground h-9 px-4 py-2 hover:bg-muted justify-start',
                    {
                      'bg-muted font-semibold': Array.isArray(item.path)
                        ? item.path.some((p) => pathname === p)
                        : pathname === item.path,
                    },
                  )}
                  onClick={() => {
                    navigate(item.href);
                  }}
                >
                  {item.name}
                </a>
              </nav>
            );
          })}
        </aside>
        <div className="flex-1 max-w-6xl">{children}</div>
      </div>
    </div>
  );
};

export const getLayoutWithConfiguration = (page: React.ReactNode) => {
  return <LayoutWithConfiguration>{page}</LayoutWithConfiguration>;
};
