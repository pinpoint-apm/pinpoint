import {
  SERVICE_NAME_MAX_LENGTH,
  SERVICE_NAME_MIN_LENGTH,
  isValidServiceNameLength,
  isValidServiceNamePattern,
} from './service';

describe('isValidServiceNamePattern', () => {
  it.each(['my-service', 'my_service', 'my.service', 'MyService01', 'a'])(
    'accepts "%s"',
    (name) => {
      expect(isValidServiceNamePattern(name)).toBe(true);
    },
  );

  it.each(['', 'my service', ' my-service', 'my-service ', 'my/service', 'my:service', '서비스'])(
    'rejects "%s"',
    (name) => {
      expect(isValidServiceNamePattern(name)).toBe(false);
    },
  );
});

describe('isValidServiceNameLength', () => {
  it('accepts names of min and max length', () => {
    expect(isValidServiceNameLength('a'.repeat(SERVICE_NAME_MIN_LENGTH))).toBe(true);
    expect(isValidServiceNameLength('a'.repeat(SERVICE_NAME_MAX_LENGTH))).toBe(true);
  });

  it('rejects a name shorter than min length', () => {
    expect(isValidServiceNameLength('a'.repeat(SERVICE_NAME_MIN_LENGTH - 1))).toBe(false);
  });

  it('rejects a name longer than max length', () => {
    expect(isValidServiceNameLength('a'.repeat(SERVICE_NAME_MAX_LENGTH + 1))).toBe(false);
  });
});
