package com.hmall.item.controller;

import com.hmall.api.dto.ReviewDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.service.IItemReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final IItemReviewService reviewService;

    @GetMapping("/items/{itemId}/reviews")
    public List<ItemReview> getReviews(@PathVariable Long itemId) {
        return reviewService.listByItemId(itemId);
    }

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

    @DeleteMapping("/admin/reviews/{id}")
    public R<Void> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return R.ok();
    }
}
