package com.hmall.item.controller;

import com.hmall.api.dto.ReviewDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.domain.vo.ReviewVO;
import com.hmall.item.service.IItemReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "商品评价")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final IItemReviewService reviewService;

    @ApiOperation("前台查询商品评价列表")
    @GetMapping("/items/{itemId}/reviews")
    public List<ItemReview> getReviews(@PathVariable Long itemId) {
        return reviewService.listByItemId(itemId);
    }

    @ApiOperation("前台新增商品评价")
    @PostMapping("/items/{itemId}/reviews")
    public R<Void> saveReview(@PathVariable Long itemId, @RequestBody ReviewDTO dto) {
        ItemReview review = new ItemReview();
        review.setUserId(UserContext.getUser());
        review.setItemId(itemId);
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setRating(dto.getRating());
        review.setCreateTime(LocalDateTime.now());
        reviewService.save(review);
        return R.ok();
    }

    @ApiOperation("管理端分页查询评价列表")
    @GetMapping("/admin/reviews")
    public PageDTO<ReviewVO> queryReviewsPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("评分") @RequestParam(required = false) Integer rating) {
        return reviewService.queryReviewsPage(page, size, rating);
    }

    @ApiOperation("管理端删除评价")
    @DeleteMapping("/admin/reviews/{id}")
    public R<Void> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return R.ok();
    }
}
