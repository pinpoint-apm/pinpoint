/**
 * [MOCK #10744] 로컬 개발 서버(vite dev)에서만 동작하는 임시 403 mock.
 *
 * 이슈 #10744("한 API라도 403이면 그 페이지는 권한 없음")을 로컬에서 확인하려면 백엔드가 403을
 * 내줘야 하는데, 권한 검사(`@naverPermissionEvaluator`)의 구현이 이 저장소에 없다. 그래서 dev
 * 서버가 지정한 (화면, application) 조합의 응답을 백엔드로 넘기지 않고 403으로 대신 돌려준다.
 *
 * 기본값은 꺼져 있고, `MOCK_FORBIDDEN=1`일 때만 붙는다(= `yarn dev:forbidden`).
 * 규칙은 `forbidden.json`에 적고, **요청마다 다시 읽으므로 dev 서버를 재시작할 필요가 없다** —
 * "관리자가 권한을 주었다"를 파일 수정만으로 재현하기 위해서다.
 *
 * 이 디렉터리는 통째로 지울 수 있다. 지우는 방법은 README.md 참고.
 */

/* eslint-disable no-console */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import type { Plugin } from 'vite';

const LOG_PREFIX = '[mock #10744]';
const RULE_FILE = path.join(path.dirname(fileURLToPath(import.meta.url)), 'forbidden.json');

/**
 * 화면 → 그 화면이 부르는 API 경로 접두사.
 *
 * 실제 백엔드의 권한이 기능별로 갈리는 것을 그대로 흉내 낸다
 * (`hasInspectorPermission` / `hasExceptionTracePermission` / `hasUrlStatPermission`).
 * 그래서 "inspector 권한만 없는" 사용자가 errorAnalysis에서 사이드바 agent 목록(`/api/agents`)만
 * 403을 받는 상태도 만들어진다 — 화면 판정은 그것까지 포함해 이루어진다.
 *
 * **여기 적힌 것은 "그 권한이 막는 API"이지 "그 화면만 막는다"는 뜻이 아니다.** `/api/agents`는
 * inspector 권한이라, `inspector` 규칙 하나로 errorAnalysis·urlStatistic의 사이드바까지 막힌다.
 * 실제 백엔드도 그렇다.
 *
 * 비교 규칙(`matchesApiPath`): `/`로 끝나는 항목은 그 아래 전부, 나머지는 **그 경로 하나만**이다.
 * 앞부분만 맞춰 보면 `/api/agents`가 `/api/agents/statistics`(관리자 권한)까지 삼켜, 권한이 다른
 * API가 함께 막힌다. 하위 경로를 붙여도 같다 — statistics는 `/api/agents`의 하위 경로다.
 * 그래서 같은 권한의 하위 경로는 하나씩 적는다.
 */
const PAGE_API_PREFIXES: Record<string, string[]> = {
  inspector: [
    '/api/inspector/',
    '/api/getAgentEvents',
    '/api/getAgentStatusTimeline',
    '/api/agents',
    // 같은 inspector 권한이다(`AgentsController`). `/api/agents/statistics`는 관리자 권한이라 뺀다.
    '/api/agents/overview',
  ],
  urlStatistic: ['/api/uriStat/'],
  threadDump: ['/api/agent/activeThreadLightDump', '/api/agent/activeThreadDump', '/api/agents'],
  // `/api/errors/groups`는 권한 검사가 없는 쪽이라 일부러 뺀다. 그것만 성공하는 상태까지 그대로
  // 재현해야 "groups는 왔는데 화면은 권한 없음"이 맞는 동작인지 볼 수 있다.
  errorAnalysis: [
    '/api/errors/errorList',
    '/api/errors/errorList/groupBy',
    '/api/errors/chart',
    '/api/errors/transactionInfo',
  ],
  scatterFullScreenMode: ['/api/getScatterData', '/api/heatmap/applicationData'],
  heatmapFullScreenMode: ['/api/getScatterData', '/api/heatmap/applicationData'],
  'config/agentStatistic': ['/api/agents/statistics'],
};

interface ForbiddenRule {
  /** `PAGE_API_PREFIXES`의 key. `*`면 모든 화면. */
  page: string;
  /** 막을 application 이름. `*`거나 비어 있으면 모든 application. */
  application?: string;
}

const readRules = (): ForbiddenRule[] => {
  try {
    const parsed = JSON.parse(fs.readFileSync(RULE_FILE, 'utf-8')) as {
      forbidden?: ForbiddenRule[];
    };
    return parsed.forbidden ?? [];
  } catch (error) {
    console.error(`${LOG_PREFIX} ${RULE_FILE} 를 읽지 못했습니다. 규칙 없이 진행합니다.`, error);
    return [];
  }
};

/** 조회 대상 application. 파라미터 이름이 API마다 다르다. */
const getApplicationName = (url: URL) =>
  url.searchParams.get('applicationName') || url.searchParams.get('application') || '';

const matchesApplication = (rule: ForbiddenRule, applicationName: string) =>
  !rule.application || rule.application === '*' || rule.application === applicationName;

/** `prefix`가 `/`로 끝나면 그 아래 전부, 아니면 그 경로 하나만. */
const matchesApiPath = (pathname: string, prefix: string) =>
  prefix.endsWith('/') ? pathname.startsWith(prefix) : pathname === prefix;

const matchesPage = (rule: ForbiddenRule, pathname: string) => {
  const prefixes =
    rule.page === '*'
      ? Object.values(PAGE_API_PREFIXES).flat()
      : (PAGE_API_PREFIXES[rule.page] ?? []);

  return prefixes.some((prefix) => matchesApiPath(pathname, prefix));
};

const findRule = (url: URL) => {
  const applicationName = getApplicationName(url);

  return readRules().find(
    (rule) => matchesPage(rule, url.pathname) && matchesApplication(rule, applicationName),
  );
};

/**
 * 서버가 실제로 내려주는 모양 그대로다 — `CustomExceptionHandler#handleAccessDeniedException`.
 * 프론트는 `status`만 보고 판정하므로(문구가 아니라) 이 필드가 핵심이다.
 */
const problemDetail = (instance: string) => ({
  type: 'about:blank',
  title: 'Forbidden',
  status: 403,
  detail: 'Access denied',
  instance,
});

const isEnabled = () =>
  ['1', 'true', 'on'].includes(String(process.env.MOCK_FORBIDDEN ?? '').toLowerCase());

export const forbiddenMockPlugin = (): Plugin => ({
  name: 'pinpoint-dev-mock-forbidden',
  apply: 'serve',
  configureServer(server) {
    if (!isEnabled()) {
      return;
    }

    console.log(
      `${LOG_PREFIX} 403 mock 활성화. 규칙: ${RULE_FILE}\n` +
        `${LOG_PREFIX} 파일을 고치면 재시작 없이 바로 적용됩니다(요청마다 다시 읽습니다).`,
    );

    server.middlewares.use((req, res, next) => {
      const requestPath = req.url ?? '/';
      if (!requestPath.startsWith('/api')) {
        next();
        return;
      }

      const url = new URL(requestPath, 'http://localhost');
      const rule = findRule(url);

      if (!rule) {
        next();
        return;
      }

      console.log(
        `${LOG_PREFIX} 403   ${url.pathname} application=${getApplicationName(url) || '(none)'}`,
      );
      res.statusCode = 403;
      res.setHeader('Content-Type', 'application/problem+json; charset=utf-8');
      res.end(JSON.stringify(problemDetail(url.pathname)));
    });
  },
});
