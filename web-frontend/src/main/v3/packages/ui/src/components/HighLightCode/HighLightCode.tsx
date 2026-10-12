import 'highlight.js/styles/atom-one-light.min.css';
import React from 'react';
import { cn } from '../../lib';

// 추가 언어는 ./hljs 에 등록한다
type LanguageType = 'typescript' | 'sql' | 'java' | 'json' | 'text';

const loadHljs = () => import('./hljs').then((module) => module.hljs);

// The highlighted output is rendered via dangerouslySetInnerHTML. hljs.highlight() escapes
// its output, but the fallback path uses the raw code, so it must be escaped to avoid XSS.
// The initial state is rendered before highlight.js finishes loading, so it is escaped too.
const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');

export interface HighLightCodeProps {
  language?: LanguageType;
  code?: string;
  className?: string;
  // Preserve original whitespace/indentation (e.g. pretty-printed JSON) instead of
  // collapsing it. Renders as a single pre-wrapped block and wraps on word boundaries.
  wrap?: boolean;
}

export const HighLightCode = ({
  language = 'text',
  code = '',
  className,
  wrap = false,
}: HighLightCodeProps) => {
  const [highLightedCode, setHighLightedCode] = React.useState(() => escapeHtml(code || ''));
  React.useEffect(() => {
    loadHljs()
      .then((hljs) => hljs.highlightAll())
      .catch((e) => console.error('Highlight Error', e));
  }, []);

  React.useEffect(() => {
    let cancelled = false;
    loadHljs()
      .then((hljs) => {
        if (cancelled) {
          return;
        }
        try {
          const value = hljs.highlight(code, { language }).value;
          setHighLightedCode(value);
        } catch (e) {
          console.error('Highlight Error', e);
          setHighLightedCode(escapeHtml(code || ''));
        }
      })
      .catch((e) => {
        // highlight.js 를 불러오지 못하면 하이라이트 없이 escape 된 원문을 보여준다.
        console.error('Highlight Error', e);
        if (!cancelled) {
          setHighLightedCode(escapeHtml(code || ''));
        }
      });
    return () => {
      cancelled = true;
    };
  }, [code, language]);

  if (wrap) {
    return (
      <div className={cn('hljs', className)}>
        <code
          className="block whitespace-pre-wrap wrap-break-word"
          dangerouslySetInnerHTML={{ __html: highLightedCode }}
        ></code>
      </div>
    );
  }

  return (
    // <pre>
    //   <code>{code}</code>
    // </pre>
    <div className={cn('hljs', className)}>
      {highLightedCode.split('\n').map((str, i) => (
        <React.Fragment key={i}>
          <code dangerouslySetInnerHTML={{ __html: str }} className="break-all"></code>
          <br />
        </React.Fragment>
      ))}
    </div>
  );
};
