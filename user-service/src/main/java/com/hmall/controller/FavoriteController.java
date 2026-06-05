package com.hmall.controller;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.UserFavorite;
import com.hmall.domain.vo.FavoriteVO;
import com.hmall.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;
    private final ItemClient itemClient;

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
    public List<FavoriteVO> list() {
        List<UserFavorite> favorites = favoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, UserContext.getUser())
                .orderByDesc(UserFavorite::getCreateTime)
                .list();

        if (favorites == null || favorites.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询商品信息
        List<Long> itemIds = favorites.stream()
                .map(UserFavorite::getItemId)
                .collect(Collectors.toList());
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        Map<Long, ItemDTO> itemMap = items.stream()
                .collect(Collectors.toMap(ItemDTO::getId, item -> item));

        // 组装FavoriteVO
        return favorites.stream().map(fav -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(fav.getId());
            vo.setUserId(fav.getUserId());
            vo.setItemId(fav.getItemId());
            vo.setCreateTime(fav.getCreateTime());

            ItemDTO item = itemMap.get(fav.getItemId());
            if (item != null) {
                vo.setItemName(item.getName());
                vo.setItemPrice(item.getPrice());
                vo.setItemImage(item.getImage());
                vo.setItemSold(item.getSold());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @GetMapping("/check/{itemId}")
    public Boolean check(@PathVariable Long itemId) {
        return favoriteService.isFavorite(UserContext.getUser(), itemId);
    }
}
