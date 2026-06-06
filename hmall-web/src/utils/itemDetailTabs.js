const SUPPORTED_TABS = ['detail', 'spec', 'reviews', 'service'];

export function productTabFromRouteQuery(query = {}) {
  return SUPPORTED_TABS.includes(query.tab) ? query.tab : 'detail';
}
