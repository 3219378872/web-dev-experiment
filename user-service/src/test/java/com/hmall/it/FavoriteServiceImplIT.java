package com.hmall.it;

import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.UserContext;
import com.hmall.service.IFavoriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
class FavoriteServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IFavoriteService favoriteService;

    @Test
    void addFavorite_shouldSave() {
        favoriteService.addFavorite(1L, 999L);
        assertThat(favoriteService.isFavorite(1L, 999L)).isTrue();
    }

    @Test
    void addFavorite_duplicate_shouldThrow() {
        favoriteService.addFavorite(1L, 100L);
        assertThatThrownBy(() -> favoriteService.addFavorite(1L, 100L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已收藏");
    }

    @Test
    void removeFavorite_shouldDelete() {
        favoriteService.addFavorite(1L, 888L);
        favoriteService.removeFavorite(1L, 888L);
        assertThat(favoriteService.isFavorite(1L, 888L)).isFalse();
    }

    @Test
    void isFavorite_notFavorite_shouldReturnFalse() {
        assertThat(favoriteService.isFavorite(1L, 777L)).isFalse();
    }
}
