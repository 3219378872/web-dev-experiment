package com.hmall.service.impl;

import com.hmall.UserServiceTestBase;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.UserFavorite;
import com.hmall.mapper.UserFavoriteMapper;
import com.hmall.service.IFavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavoriteServiceImplTest extends UserServiceTestBase {

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    private void insertFavorite(Long userId, Long itemId) {
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setItemId(itemId);
        f.setCreateTime(java.time.LocalDateTime.now());
        userFavoriteMapper.insert(f);
    }

    // ───────────────────── isFavorite ─────────────────────

    @Nested
    @DisplayName("isFavorite")
    class IsFavoriteTests {

        @Test
        @DisplayName("已收藏-返回true")
        void alreadyFavorited_returnsTrue() {
            insertFavorite(1L, 100L);

            boolean result = favoriteService.isFavorite(1L, 100L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("未收藏-返回false")
        void notFavorited_returnsFalse() {
            boolean result = favoriteService.isFavorite(1L, 100L);

            assertThat(result).isFalse();
        }
    }

    // ───────────────────── addFavorite ─────────────────────

    @Nested
    @DisplayName("addFavorite")
    class AddFavoriteTests {

        @Test
        @DisplayName("已收藏 → BizIllegalException")
        void alreadyFavorited_throws() {
            insertFavorite(1L, 100L);

            assertThatThrownBy(() -> favoriteService.addFavorite(1L, 100L))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("已收藏");
        }

        @Test
        @DisplayName("未收藏-保存成功")
        void notFavorited_saves() {
            favoriteService.addFavorite(1L, 100L);

            boolean exists = favoriteService.isFavorite(1L, 100L);
            assertThat(exists).isTrue();
        }
    }

    // ───────────────────── removeFavorite ─────────────────────

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavoriteTests {

        @Test
        @DisplayName("删除已存在的收藏-成功删除")
        void remove_existing_deletes() {
            insertFavorite(1L, 100L);

            favoriteService.removeFavorite(1L, 100L);

            boolean exists = favoriteService.isFavorite(1L, 100L);
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("删除不存在的收藏-无异常")
        void removeNonExistent_noException() {
            favoriteService.removeFavorite(1L, 999L);

            boolean exists = favoriteService.isFavorite(1L, 999L);
            assertThat(exists).isFalse();
        }
    }
}
