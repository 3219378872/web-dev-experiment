import { describe, it, expect } from 'vitest';
import { validateAvatarFile, AVATAR_MAX_BYTES } from '@/utils/avatarUpload';

function fakeFile(type, size) {
  return { type, size };
}

describe('validateAvatarFile', () => {
  it('合法 PNG-通过', () => {
    expect(validateAvatarFile(fakeFile('image/png', 1024))).toEqual({ ok: true, reason: null });
  });

  it('合法 JPEG-通过', () => {
    expect(validateAvatarFile(fakeFile('image/jpeg', 1024)).ok).toBe(true);
    expect(validateAvatarFile(fakeFile('image/jpg', 1024)).ok).toBe(true);
  });

  it('空文件-reason=empty', () => {
    expect(validateAvatarFile(null)).toEqual({ ok: false, reason: 'empty' });
  });

  it('非图片类型-reason=type', () => {
    expect(validateAvatarFile(fakeFile('application/pdf', 1024))).toEqual({
      ok: false,
      reason: 'type',
    });
    expect(validateAvatarFile(fakeFile('image/gif', 1024)).reason).toBe('type');
  });

  it('超过 2MB-reason=size', () => {
    expect(validateAvatarFile(fakeFile('image/png', AVATAR_MAX_BYTES + 1))).toEqual({
      ok: false,
      reason: 'size',
    });
  });

  it('恰好 2MB-通过', () => {
    expect(validateAvatarFile(fakeFile('image/png', AVATAR_MAX_BYTES)).ok).toBe(true);
  });
});
