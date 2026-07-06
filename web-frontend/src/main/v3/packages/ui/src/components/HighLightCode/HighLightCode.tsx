import 'highlight.js/styles/atom-one-light.min.css';
import React from 'react';
import hljs from 'highlight.js/lib/core';
import typescript from 'highlight.js/lib/languages/typescript';
import sql from 'highlight.js/lib/languages/sql';
import java from 'highlight.js/lib/languages/java';
import json from 'highlight.js/lib/languages/json';
import text from 'highlight.js/lib/languages/plaintext';
import { cn } from '../../lib';

// 추가 언어 필요시 등록
type LanguageType = 'typescript' | 'sql' | 'java' | 'json' | 'text';
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('java', java);
hljs.registerLanguage('json', json);
hljs.registerLanguage('text', text);

// The highlighted output is rendered via dangerouslySetInnerHTML. hljs.highlight() escapes
// its output, but the fallback path uses the raw code, so it must be escaped to avoid XSS.
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
  const [highLightedCode, setHighLightedCode] = React.useState(code);
  React.useEffect(() => {
    hljs.highlightAll();
  }, []);

  React.useEffect(() => {
    try {
      const value = hljs.highlight(code, { language }).value;
      setHighLightedCode(value);
    } catch (e) {
      console.error('Highlight Error', e);
      setHighLightedCode(escapeHtml(code || ''));
    }
  }, [code, language]);

  if (wrap) {
    return (
      <div className={cn('hljs', className)}>
        <code
          className="block whitespace-pre-wrap break-words"
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
