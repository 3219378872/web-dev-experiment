package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.UserFavorite;
import com.hmall.mapper.UserFavoriteMapper;
import com.hmall.service.IFavoriteService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class FavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements IFavoriteService {

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        return lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getItemId, itemId)
                .count() > 0;
    }

    @Override
    public void addFavorite(Long userId, Long itemId) {
        if (isFavorite(userId, itemId)) {
            throw new BizIllegalException("已收藏");
        }
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setItemId(itemId);
        f.setCreateTime(LocalDateTime.now());
        save(f);
    }

    @Override
    public void removeFavorite(Long userId, Long itemId) {
        lambdaUpdate()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getItemId, itemId)
                .remove();
    }
}
