// 头像文件校验纯逻辑：限制格式与大小，便于在视图与测试间复用。

export const AVATAR_MAX_BYTES = 2 * 1024 * 1024; // 2MB
const IMAGE_TYPE_RE = /^image\/(jpe?g|png)$/i;

/** 校验头像文件，返回 { ok, reason }。reason: 'empty' | 'type' | 'size' | null。 */
export function validateAvatarFile(file) {
  if (!file) return { ok: false, reason: 'empty' };
  if (!IMAGE_TYPE_RE.test(file.type)) return { ok: false, reason: 'type' };
  if (file.size > AVATAR_MAX_BYTES) return { ok: false, reason: 'size' };
  return { ok: true, reason: null };
}
