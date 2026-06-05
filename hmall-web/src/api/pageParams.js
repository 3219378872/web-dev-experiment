/**
 * 把前端常用的分页参数（page/size）规范化为后端 PageQuery 期望的字段
 * （pageNo/pageSize）。同时保留已经使用后端字段名的调用方，以及 sortBy/isAsc。
 *
 * 后端 com.hmall.common.domain.PageQuery 绑定 pageNo / pageSize / sortBy / isAsc。
 * 历史前端代码普遍传 page / size，导致分页字段不生效（issue #127）。
 *
 * 该函数不可变：返回一个全新对象，不修改入参。
 */
export function normalizePageParams(params) {
  if (!params || typeof params !== 'object') return params;
  const { page, size, ...rest } = params;
  const out = { ...rest };
  if (out.pageNo == null && page != null) out.pageNo = page;
  if (out.pageSize == null && size != null) out.pageSize = size;
  return out;
}
