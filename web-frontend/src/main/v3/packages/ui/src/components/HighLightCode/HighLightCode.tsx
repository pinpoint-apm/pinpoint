import { cn } from '@pinpoint-fe/ui/lib';
import SyntaxHighlighter from 'react-syntax-highlighter/dist/esm/default-highlight';
// import SyntaxHighlighter from 'react-syntax-highlighter';
import { atomOneLight } from 'react-syntax-highlighter/dist/esm/styles/hljs';

// 추가 언어 필요시 등록
type LanguageType = 'typescript' | 'sql' | 'java' | 'text';
export interface HighLightCodeProps {
  language?: LanguageType;
  code?: string;
  className?: string;
}

export const HighLightCode = ({ language = 'text', code = '', className }: HighLightCodeProps) => {
  return (
    <SyntaxHighlighter
      language={language}
      style={atomOneLight}
      class={cn('hljs test111', className)}
    >
      {code}
    </SyntaxHighlighter>
  );
};
