import { Configuration } from '@pinpoint-fe/constants';
import { Users } from '../../components/Config';

export interface UsersPageProps {
  configuration?: Configuration;
}

export const UsersPage = (props: UsersPageProps) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Users</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <Users {...props} />
    </div>
  );
};
