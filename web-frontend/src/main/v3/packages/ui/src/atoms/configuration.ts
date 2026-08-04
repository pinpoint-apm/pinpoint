import { atom } from 'jotai';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

/**
 * 모든 화면이 공유하는 단일 configuration 저장소.
 *
 * 타입은 OSS 기준인 `Configuration`으로 고정한다. `Configuration`을 확장한 저장소
 * (naver 등)도 구조적 타이핑상 확장 객체를 그대로 넣을 수 있으므로 쓰기에는 제약이 없다.
 * 확장 필드를 "읽는" 쪽만 타입을 넓혀야 하는데, 그건 이 atom을 넓히는 대신
 * `useConfiguration<T>()`에서 처리한다. atom 자체를 넓히면(예: 인덱스 시그니처)
 * 기존 필드의 오타 검출과 확장 필드의 정확한 타입을 모두 잃는다.
 */
export const configurationAtom = atom<Configuration | undefined>(undefined);
