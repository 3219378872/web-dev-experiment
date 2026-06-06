export function userQueryParams({ page = 1, pageSize = 10, keyword = '', status = '' } = {}) {
  const params = { page, size: pageSize };
  if (keyword) params.keyword = keyword;
  if (status !== '' && status !== null && status !== undefined) params.status = status;
  return params;
}

export function reviewTabRating(tab) {
  if (tab === 'good') return { minRating: 4, maxRating: 5 };
  if (tab === 'mid') return { rating: 3 };
  if (tab === 'bad') return { minRating: 1, maxRating: 2 };
  return {};
}

export function reviewQueryParams({ page = 1, pageSize = 10, tab = 'all' } = {}) {
  return {
    page,
    size: pageSize,
    ...reviewTabRating(tab),
  };
}

export function buildAdminProfilePayload(form = {}) {
  return {
    nickname: form.nickname,
    email: form.email,
    avatar: form.avatar,
  };
}

export function buildAdminPasswordPayload(form = {}) {
  return {
    currentPassword: form.current,
    password: form.newPwd,
  };
}
