import hljs from 'highlight.js/lib/core';
import typescript from 'highlight.js/lib/languages/typescript';
import sql from 'highlight.js/lib/languages/sql';
import java from 'highlight.js/lib/languages/java';
import json from 'highlight.js/lib/languages/json';
import text from 'highlight.js/lib/languages/plaintext';

// HighLightCode 가 동적으로 불러오는 모듈이다. 코드 하이라이트는 에러 상세·콜트리처럼 눌러야 보이는
// 화면에서만 쓰이므로, 첫 화면 번들에 highlight.js 를 싣지 않도록 따로 떼어 둔다.
// 추가 언어 필요시 등록
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('java', java);
hljs.registerLanguage('json', json);
hljs.registerLanguage('text', text);

export { hljs };
