package com.hmall.item.it;

import com.hmall.common.utils.UserContext;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.service.IItemReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-review.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ItemReviewServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IItemReviewService reviewService;

    @Test
    void listByItemId_shouldReturnReviewsOrderedByTime() {
        List<ItemReview> reviews = reviewService.listByItemId(100L);
        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getCreateTime())
                .isAfterOrEqualTo(reviews.get(1).getCreateTime());
    }

    @Test
    void listByItemId_noReviews_shouldReturnEmpty() {
        List<ItemReview> reviews = reviewService.listByItemId(999L);
        assertThat(reviews).isEmpty();
    }
}
