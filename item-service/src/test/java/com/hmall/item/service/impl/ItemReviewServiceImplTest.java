package com.hmall.item.service.impl;

import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemReviewServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private IItemReviewService reviewService;

    @Autowired
    private ItemReviewMapper reviewMapper;

    @Test
    @DisplayName("listByItemId: 按商品 ID 查询评价，按创建时间降序")
    void listByItemId_returnsReviewsOrderedByTime() throws Exception {
        ItemReview r1 = review(100L, "first review");
        r1.setCreateTime(LocalDateTime.now().minusHours(1));
        reviewMapper.insert(r1);

        ItemReview r2 = review(100L, "second review");
        r2.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r2);

        ItemReview r3 = review(200L, "other item review");
        r3.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r3);

        List<ItemReview> reviews = reviewService.listByItemId(100L);
        assertThat(reviews).hasSize(2);
        // 按 createTime DESC 排序，最新的在前
        assertThat(reviews.get(0).getContent()).isEqualTo("second review");
        assertThat(reviews.get(1).getContent()).isEqualTo("first review");
    }

    @Test
    @DisplayName("listByItemId: 无评价时返回空列表")
    void listByItemId_noReviews_returnsEmpty() {
        List<ItemReview> reviews = reviewService.listByItemId(999L);
        assertThat(reviews).isEmpty();
    }

    private ItemReview review(Long itemId, String content) {
        ItemReview r = new ItemReview();
        r.setItemId(itemId);
        r.setUserId(TEST_USER_ID);
        r.setContent(content);
        r.setRating(5);
        return r;
    }
}
