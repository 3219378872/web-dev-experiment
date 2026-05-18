package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.UserFavorite;
import com.hmall.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping
    public R<Void> add(@RequestParam("itemId") Long itemId) {
        favoriteService.addFavorite(UserContext.getUser(), itemId);
        return R.ok();
    }

    @DeleteMapping("/{itemId}")
    public R<Void> remove(@PathVariable Long itemId) {
        favoriteService.removeFavorite(UserContext.getUser(), itemId);
        return R.ok();
    }

    @GetMapping
    public List<UserFavorite> list() {
        return favoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, UserContext.getUser())
                .orderByDesc(UserFavorite::getCreateTime)
                .list();
    }

    @GetMapping("/check/{itemId}")
    public Boolean check(@PathVariable Long itemId) {
        return favoriteService.isFavorite(UserContext.getUser(), itemId);
    }
}
