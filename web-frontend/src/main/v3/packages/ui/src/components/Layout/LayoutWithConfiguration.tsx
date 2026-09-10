import { useLocation, useNavigate } from 'react-router';

import { Separator } from '..';
import { cn } from '../../lib';

export type ConfigMenu = {
  title: string;
  desc?: string;
  menus: {
    path: string | string[];
    href: string;
    name: string;
    /**
     * true면 목록에서 아예 제외한다. 렌더만 건너뛰면 `space-y-1` 이 만든 간격이 남아
     * 빈 칸처럼 보이므로 `hide` 는 map 전에 걸러낸다(사이드 네비게이션과 같은 규칙).
     *
     * 목록에서 감추기만 하므로, 감춰진 메뉴의 경로로 직접 들어와도 그 경로가 어느 그룹의
     * 것인지는 그대로 판정된다.
     */
    hide?: boolean;
  }[];
};

export interface LayoutWithConfigurationProps {
  children: React.ReactNode;
  configMenu?: ConfigMenu;
}

export const LayoutWithConfiguration = ({ children, configMenu }: LayoutWithConfigurationProps) => {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const visibleMenus = configMenu?.menus.filter((menu) => !menu.hide) ?? [];

  return (
    <div className="flex flex-col h-full p-10">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">{configMenu?.title}</h2>
        <p className="text-muted-foreground">{configMenu?.desc}</p>
      </div>
      <Separator className="my-6" />
      {/* `gap` 을 쓴다. Tailwind 4 의 `space-x-*` 는 `:where(& > :not(:last-child))` 로 바뀌어
          명시도가 0 이라, 자식(`aside` 의 `-mx-4`)의 margin 유틸리티에 밀려 간격이 사라진다. */}
      <div className="flex flex-row gap-12 h-[calc(100%-6rem)]">
        {visibleMenus.length ? (
          <aside className="-mx-4 lg:w-1/5">
            {visibleMenus.map((item, i) => {
              return (
                <nav key={i} className="flex space-x-2 lg:flex-col lg:space-x-0 lg:space-y-1">
                  <a
                    className={cn(
                      'cursor-pointer inline-flex items-center whitespace-nowrap rounded-md text-sm transition-colors focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 hover:text-accent-foreground h-9 px-4 py-2 hover:bg-muted justify-start',
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
        ) : null}
        <div className="flex-1 max-w-6xl overflow-auto">{children}</div>
      </div>
    </div>
  );
};
