import { cn } from '@pinpoint-fe/ui';

const HeatmapVisualMapIcon = ({ type }: { type?: 'success' | 'fail' }) => {
  return (
    <div className="flex flex-col items-center justify-center text-xs text-gray-700">
      <div className="w-[70px] flex items-center justify-between">
        <span>min ~</span>
        <span>~ max</span>
      </div>
      <div
        className={cn('w-[50px] relative h-3 mx-1 rounded-full bg-gradient-to-r', {
          'from-green-50 to-green-800': type === 'success',
          'from-red-50 to-red-800': type === 'fail',
        })}
      >
        {/* 왼쪽 핸들 */}
        <div className="absolute w-2 h-4 -translate-y-1/2 border-2 border-gray-300 rounded shadow-sm top-1/2" />
        {/* 오른쪽 핸들 */}
        <div className="absolute right-0 w-2 h-4 -translate-y-1/2 border-2 border-gray-300 rounded shadow-sm top-1/2" />
      </div>
    </div>
  );
};
export default HeatmapVisualMapIcon;
