import { Badge, Popover, PopoverContent, PopoverTrigger } from '../../ui';
import { LuArrowUp } from 'react-icons/lu';
import { InspectorAgentEventViewer } from '.';

export interface TimelineEventProps {
  data: {
    startTimestamp: number;
    endTimestamp: number;
    value: {
      totalCount: number;
    };
  }[];
  range: number[];
}

export const TimelineEvent = ({ data, range: [from, to] }: TimelineEventProps) => {
  const rangeDiff = to - from;

  return (
    <div className="relative">
      {data.map(({ startTimestamp, endTimestamp, value }, i) => {
        const pos = (((startTimestamp + endTimestamp) / 2 - from) / rangeDiff) * 100;
        return (
          <div
            key={i}
            style={{ left: `${pos}%` }}
            className="absolute text-center -translate-x-1/2 -top-1"
          >
            <LuArrowUp />
            <Popover>
              <PopoverTrigger asChild>
                <div>
                  <Badge
                    className="flex items-center py-1 font-normal cursor-pointer text-xxs hover:bg-accent hover:text-accent-foreground"
                    variant="outline"
                  >
                    <span className="lg:hidden">{value.totalCount}</span>
                    <span className="hidden lg:inline">{value.totalCount} Events</span>
                  </Badge>
                </div>
              </PopoverTrigger>
              {/* deadlock 메시지(스레드 덤프)는 길어서 기본 폭(w-72)에서는 화면 밖으로 늘어진다.
                  폭은 내용만큼 쓰되 상한을 두고, 높이를 묶어 표 안에서 스크롤한다(표 머리글은 그 안에서
                  고정된다). 폭을 고정하지 않는 것은 메시지 없는 이벤트(대부분)가 빈 팝업으로 넓어지지 않게
                  하기 위해서다.
                  높이는 팝업이 놓인 쪽에 남은 공간(Radix가 주는 available-height)도 넘지 않게 한다 —
                  60vh만으로는 창이 낮을 때 팝업 아래가 화면 밖으로 잘린다.
                  min-h-0이 있어야 표가 flex 안에서 줄어들어 스크롤이 생긴다. */}
              <PopoverContent className="flex flex-col w-max max-w-[min(56rem,90vw)] max-h-[min(60vh,var(--radix-popover-content-available-height))] px-1 py-0">
                <InspectorAgentEventViewer
                  className="flex flex-col min-h-0"
                  eventRange={[startTimestamp, endTimestamp]}
                />
              </PopoverContent>
            </Popover>
          </div>
        );
      })}
    </div>
  );
};
