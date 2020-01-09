import { JSONTextParserPipe } from './json-text-parser.pipe';
import { SafeHtmlPipe } from './safe-html.pipe';
import { SafeStylePipe } from './safe-style.pipe';
import { HandleObsPipe } from './handle-obs.pipe';
import { PickPropsPipe } from './pick-props.pipe';

export const PIPES = [
    SafeHtmlPipe,
    SafeStylePipe,
    JSONTextParserPipe,
    HandleObsPipe,
    PickPropsPipe
];
