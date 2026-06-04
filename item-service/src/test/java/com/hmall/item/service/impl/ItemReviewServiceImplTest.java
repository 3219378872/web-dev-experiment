package com.hmall.item.service.impl;

import com.hmall.common.domain.PageDTO;
import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.domain.vo.ReviewVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Autowired
    private ItemMapper itemMapper;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // 创建测试商品
        testItem = new Item();
        testItem.setName("测试商品");
        testItem.setPrice(10000);
        testItem.setStock(100);
        testItem.setStatus(1);
        itemMapper.insert(testItem);

        // 创建测试评价
        ItemReview review1 = new ItemReview();
        review1.setUserId(1L);
        review1.setItemId(testItem.getId());
        review1.setContent("好评");
        review1.setRating(5);
        review1.setCreateTime(LocalDateTime.now().minusHours(2));
        reviewMapper.insert(review1);

        ItemReview review2 = new ItemReview();
        review2.setUserId(2L);
        review2.setItemId(testItem.getId());
        review2.setContent("中评");
        review2.setRating(3);
        review2.setCreateTime(LocalDateTime.now().minusHours(1));
        reviewMapper.insert(review2);

        ItemReview review3 = new ItemReview();
        review3.setUserId(3L);
        review3.setItemId(testItem.getId());
        review3.setContent("差评");
        review3.setRating(1);
        review3.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review3);
    }

    @Nested
    @DisplayName("listByItemId")
    class ListByItemIdTests {

        @Test
        @DisplayName("按商品ID查询评价列表")
        void listByItemId_returnsReviews() {
            List<ItemReview> result = reviewService.listByItemId(testItem.getId());

            assertThat(result).hasSize(3);
            assertThat(result).extracting(ItemReview::getContent)
                    .containsExactlyInAnyOrder("好评", "中评", "差评");
        }

        @Test
        @DisplayName("按商品ID查询评价列表-按创建时间倒序")
        void listByItemId_orderedByCreateTimeDesc() {
            List<ItemReview> result = reviewService.listByItemId(testItem.getId());

            // 验证按创建时间倒序
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).getCreateTime())
                        .isAfterOrEqualTo(result.get(i + 1).getCreateTime());
            }
        }

        @Test
        @DisplayName("按商品ID查询评价列表-无评价返回空列表")
        void listByItemId_noReviews_returnsEmpty() {
            List<ItemReview> result = reviewService.listByItemId(99999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("queryReviewsPage")
    class QueryReviewsPageTests {

        @Test
        @DisplayName("管理端分页查询评价列表")
        void queryReviewsPage_returnsPage() {
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 10, null);

            assertThat(result.getList()).hasSize(3);
            assertThat(result.getTotal()).isEqualTo(3L);
        }

        @Test
        @DisplayName("管理端分页查询评价列表-按评分过滤")
        void queryReviewsPage_filtersByRating() {
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 10, 5);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("管理端分页查询评价列表-无评分过滤")
        void queryReviewsPage_noRatingFilter_returnsAll() {
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 10, null);

            assertThat(result.getList()).hasSize(3);
        }

        @Test
        @DisplayName("管理端分页查询评价列表-分页参数生效")
        void queryReviewsPage_pagination_works() {
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 2, null);

            assertThat(result.getList()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(3L);
        }

        @Test
        @DisplayName("管理端分页查询评价列表-包含商品信息")
        void queryReviewsPage_includesItemInfo() {
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 10, null);

            assertThat(result.getList()).hasSize(3);
            assertThat(result.getList()).allMatch(r -> r.getItemName().equals("测试商品"));
        }

        @Test
        @DisplayName("管理端分页查询评价列表-商品不存在时itemName为null")
        void queryReviewsPage_itemNotExists_itemNameIsNull() {
            // 创建一个商品，然后删除它
            Item item = new Item();
            item.setName("临时商品");
            item.setPrice(5000);
            item.setStock(50);
            item.setStatus(1);
            itemMapper.insert(item);

            // 创建评价
            ItemReview review = new ItemReview();
            review.setUserId(1L);
            review.setItemId(item.getId());
            review.setContent("评价内容");
            review.setRating(4);
            reviewMapper.insert(review);

            // 删除商品
            itemMapper.deleteById(item.getId());

            // 查询评价
            PageDTO<ReviewVO> result = reviewService.queryReviewsPage(1, 10, null);

            assertThat(result.getList()).hasSize(4);
            // 找到刚才创建的评价
            ReviewVO reviewVO = result.getList().stream()
                    .filter(r -> r.getItemId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);
            assertThat(reviewVO).isNotNull();
            assertThat(reviewVO.getItemName()).isNull();
        }
    }
}
