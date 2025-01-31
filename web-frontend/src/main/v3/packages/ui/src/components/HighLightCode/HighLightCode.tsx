import 'highlight.js/styles/atom-one-light.min.css';
import React from 'react';
import hljs from 'highlight.js/lib/core';
import typescript from 'highlight.js/lib/languages/typescript';
import sql from 'highlight.js/lib/languages/sql';
import java from 'highlight.js/lib/languages/java';
import text from 'highlight.js/lib/languages/plaintext';
import { cn } from '../../lib';

// 추가 언어 필요시 등록
type LanguageType = 'typescript' | 'sql' | 'java' | 'text';
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('java', java);
hljs.registerLanguage('text', text);

export interface HighLightCodeProps {
  code: string;
  language?: LanguageType;
  className?: string;
}

export const HighLightCode = ({ language = 'text', code = '', className }: HighLightCodeProps) => {
  console.log('1', language, code, className);
  const [highLightedCode, setHighLightedCode] = React.useState(code || '');
  React.useEffect(() => {
    console.log('2');
    hljs.highlightAll();
  }, []);
  React.useEffect(() => {
    console.log('3');
    const value = hljs.highlight(code || '', { language }).value;
    setHighLightedCode(value);
    console.log('4', value);
  }, [code, language]);

  console.log('highLightedCode', highLightedCode);

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
