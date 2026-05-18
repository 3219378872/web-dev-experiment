package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("user-service")
public interface FavoriteClient {

    @PostMapping("/favorites")
    void addFavorite(@RequestParam("itemId") Long itemId);

    @DeleteMapping("/favorites/{itemId}")
    void removeFavorite(@PathVariable("itemId") Long itemId);

    @GetMapping("/favorites/check/{itemId}")
    Boolean isFavorite(@PathVariable("itemId") Long itemId);
}
