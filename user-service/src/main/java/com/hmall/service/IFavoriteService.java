package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.UserFavorite;

public interface IFavoriteService extends IService<UserFavorite> {
    boolean isFavorite(Long userId, Long itemId);
    void addFavorite(Long userId, Long itemId);
    void removeFavorite(Long userId, Long itemId);
}
