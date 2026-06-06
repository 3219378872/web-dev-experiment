import { describe, expect, it } from 'vitest';
import {
  applyAdminProfile,
  buildAdminPasswordPayload,
  buildAdminProfilePayload,
  reviewQueryParams,
  reviewTabRating,
  userQueryParams,
} from '@/utils/adminOpsActions';

describe('adminOpsActions', () => {
  it('builds user list params from the backend contract only', () => {
    expect(
      userQueryParams({
        page: 2,
        pageSize: 20,
        keyword: 'alice',
        status: 1,
      })
    ).toEqual({ page: 2, size: 20, keyword: 'alice', status: 1 });

    expect(userQueryParams({ status: 0 })).toEqual({ page: 1, size: 10, status: 0 });
  });

  it('maps review tabs to backend rating filters and size pagination', () => {
    expect(reviewTabRating('good')).toEqual({ minRating: 4, maxRating: 5 });
    expect(reviewTabRating('mid')).toEqual({ rating: 3 });
    expect(reviewTabRating('bad')).toEqual({ minRating: 1, maxRating: 2 });
    expect(reviewTabRating('all')).toEqual({});

    expect(reviewQueryParams({ page: 3, pageSize: 15, tab: 'mid' })).toEqual({
      page: 3,
      size: 15,
      rating: 3,
    });
  });

  it('builds profile payload with existing persisted fields including avatar', () => {
    expect(
      buildAdminProfilePayload({
        nickname: '管理员',
        email: 'admin@example.com',
        avatar: '/files/avatars/a.jpg',
        loginRecords: [{ ip: '127.0.0.1' }],
      })
    ).toEqual({
      nickname: '管理员',
      email: 'admin@example.com',
      avatar: '/files/avatars/a.jpg',
    });
  });

  it('applies persisted admin profile values over local defaults', () => {
    const form = {
      account: 'local-admin',
      nickname: '本地昵称',
      email: '',
      avatar: '',
    };

    applyAdminProfile(form, {
      username: 'server-admin',
      nickname: '服务端昵称',
      email: 'admin@example.com',
      avatar: '/files/avatars/server.jpg',
    });

    expect(form).toEqual({
      account: 'server-admin',
      nickname: '服务端昵称',
      email: 'admin@example.com',
      avatar: '/files/avatars/server.jpg',
    });
  });

  it('includes current password when building password change payload', () => {
    expect(
      buildAdminPasswordPayload({
        current: 'old-password',
        newPwd: 'new-password',
      })
    ).toEqual({
      currentPassword: 'old-password',
      password: 'new-password',
    });
  });
});
