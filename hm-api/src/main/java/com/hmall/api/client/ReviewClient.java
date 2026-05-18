package com.hmall.api.client;

import com.hmall.api.dto.ReviewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("item-service")
public interface ReviewClient {

    @GetMapping("/items/{itemId}/reviews")
    List<ReviewDTO> getReviews(@PathVariable("itemId") Long itemId);

    @PostMapping("/items/{itemId}/reviews")
    void saveReview(@PathVariable("itemId") Long itemId, @RequestBody ReviewDTO review);

    @DeleteMapping("/admin/reviews/{id}")
    void deleteReview(@PathVariable("id") Long id);
}
