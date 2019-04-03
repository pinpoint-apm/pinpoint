import { JSONTextParserPipe } from './json-text-parser.pipe';
import { SafeHtmlPipe } from './safe-html.pipe';
import { SafeStylePipe } from './safe-style.pipe';

export const PIPES = [
    SafeHtmlPipe,
    SafeStylePipe,
    JSONTextParserPipe
];
