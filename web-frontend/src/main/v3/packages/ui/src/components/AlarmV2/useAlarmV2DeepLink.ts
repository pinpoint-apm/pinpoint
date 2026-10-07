import React from 'react';
import { AlarmV2Rule, ApplicationType } from '@pinpoint-fe/ui/src/constants/types';

/**
 * What a URL asks the alarm page to show. (#14282)
 *
 * The page read `?view=`, `?ruleId=` and `?applicationName=` in three places, and the
 * order of the two effects mattered: the name picks the application, and the rules of
 * that application are where the id is then looked up. Here the order is the file's.
 */
export const useAlarmV2DeepLink = ({
  searchParams,
  applications,
  rules,
  selectApplication,
  openHistory,
}: {
  searchParams: URLSearchParams;
  /** The applications a name is resolved against; undefined until the list arrives. */
  applications?: ApplicationType[];
  rules?: AlarmV2Rule.RuleData[];
  selectApplication: React.Dispatch<React.SetStateAction<ApplicationType | undefined>>;
  openHistory: (rule: AlarmV2Rule.RuleData) => void;
}) => {
  // `?view=` drives the full-page views; `?ruleId=` keeps its existing meaning
  // (opening the history sheet), so the two never collide.
  const view = searchParams.get('view');
  const viewTemplateId = Number(searchParams.get('templateId')) || undefined;
  const viewRuleId = Number(searchParams.get('ruleId')) || undefined;
  const viewChannelId = Number(searchParams.get('channelId')) || undefined;
  const applicationNameParam = searchParams.get('applicationName');

  React.useEffect(() => {
    if (!applicationNameParam) return;

    // The type is read off the list rather than assumed: which application types can carry
    // a rule is up to the deployment, and a guess here would query for the wrong one until
    // the effect below corrected it.
    const linked = applications?.find((item) => item.applicationName === applicationNameParam);
    if (!linked) return;

    selectApplication((current) =>
      current?.applicationName === linked.applicationName &&
      current?.serviceType === linked.serviceType
        ? current
        : linked,
    );
  }, [applicationNameParam, applications, selectApplication]);

  React.useEffect(() => {
    const ruleIdParam = searchParams.get('ruleId');
    if (ruleIdParam && !view && rules) {
      const rule = rules.find((r) => String(r.id) === ruleIdParam);
      if (rule) openHistory(rule);
    }
    // `openHistory` is a setter the page keeps, so it does not belong in the list.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, view, rules]);

  return {
    view,
    viewTemplateId,
    viewRuleId,
    viewChannelId,
    isTemplateNewView: view === 'template-new',
    isTemplateEditView: view === 'template-edit' && !!viewTemplateId,
    isRuleView: view === 'rule-new' || (view === 'rule-edit' && !!viewRuleId),
    isChannelView: view === 'channel-new' || (view === 'channel-edit' && !!viewChannelId),
  };
};
