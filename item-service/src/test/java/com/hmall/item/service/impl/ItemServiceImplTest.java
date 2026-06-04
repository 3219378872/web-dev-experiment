package com.hmall.item.service.impl;

import com.hmall.common.exception.BizIllegalException;
import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.vo.ItemVO;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import com.hmall.item.service.impl.ItemServiceImpl;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private IItemService itemService;

    @Autowired
    private ItemMapper itemMapper;

    // ---- deductStock ----

    @Test
    @DisplayName("deductStock: 库存充足时正常扣减")
    void deductStock_sufficientStock_success() {
        Item item = new Item();
        item.setName("测试商品");
        item.setPrice(10000);
        item.setStock(10);
        item.setStatus(1);
        itemMapper.insert(item);

        List<OrderDetailDTO> items = List.of(
                new OrderDetailDTO().setItemId(item.getId()).setNum(3)
        );

        itemService.deductStock(items);

        Item updated = itemMapper.selectById(item.getId());
        assertThat(updated.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("deductStock: 空列表触发if(!r)抛出BizIllegalException")
    void deductStock_emptyList_throwsBizIllegalException() {
        List<OrderDetailDTO> items = List.of();
        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> itemService.deductStock(items));
        assertThat(ex.getMessage()).contains("库存不足");
    }

    @Test
    @DisplayName("deductStock: executeBatch抛出异常时被catch块包装")
    void deductStock_executeBatchThrows_catchesAndWraps() {
        ItemServiceImpl throwingService = new ItemServiceImpl() {
            @Override
            protected <E> boolean executeBatch(Collection<E> list, BiConsumer<SqlSession, E> consumer) {
                throw new RuntimeException("simulated batch failure");
            }
        };

        List<OrderDetailDTO> items = List.of(
                new OrderDetailDTO().setItemId(1L).setNum(1)
        );

        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> throwingService.deductStock(items));
        assertThat(ex.getMessage()).contains("更新库存异常");
        assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(ex.getCause().getMessage()).isEqualTo("simulated batch failure");
    }

    // ---- queryItemByIds ----

    @Test
    @DisplayName("queryItemByIds: 返回匹配的商品 DTO 列表")
    void queryItemByIds_returnsMatchingItems() {
        Item item1 = new Item();
        item1.setName("商品A");
        item1.setPrice(10000);
        item1.setStock(10);
        item1.setStatus(1);
        itemMapper.insert(item1);

        Item item2 = new Item();
        item2.setName("商品B");
        item2.setPrice(20000);
        item2.setStock(20);
        item2.setStatus(1);
        itemMapper.insert(item2);

        List<ItemDTO> result = itemService.queryItemByIds(List.of(item1.getId(), item2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDTO::getName)
                .containsExactlyInAnyOrder("商品A", "商品B");
    }

    @Test
    @DisplayName("queryItemByIds: 不存在的ID返回空列表")
    void queryItemByIds_nonExistentIds_returnsEmpty() {
        // Note: empty-list is not tested because MyBatis-Plus listByIds(empty)
        // generates invalid SQL (WHERE id IN ()) on H2
        List<ItemDTO> result = itemService.queryItemByIds(List.of(99999L, 99998L));
        assertThat(result).isEmpty();
    }

    // ---- queryRelatedItems ----

    @Nested
    @DisplayName("queryRelatedItems")
    class QueryRelatedItemsTests {

        @Test
        @DisplayName("查询相关推荐商品-同分类排除自身")
        void queryRelatedItems_sameCategory_excludesSelf() {
            // 创建测试商品
            Item item1 = new Item();
            item1.setName("商品A");
            item1.setCategory("电子产品");
            item1.setPrice(10000);
            item1.setStock(10);
            item1.setStatus(1);
            itemMapper.insert(item1);

            Item item2 = new Item();
            item2.setName("商品B");
            item2.setCategory("电子产品");
            item2.setPrice(20000);
            item2.setStock(20);
            item2.setStatus(1);
            itemMapper.insert(item2);

            Item item3 = new Item();
            item3.setName("商品C");
            item3.setCategory("电子产品");
            item3.setPrice(30000);
            item3.setStock(30);
            item3.setStatus(1);
            itemMapper.insert(item3);

            Item item4 = new Item();
            item4.setName("商品D");
            item4.setCategory("服装");
            item4.setPrice(40000);
            item4.setStock(40);
            item4.setStatus(1);
            itemMapper.insert(item4);

            // 查询相关商品
            List<ItemVO> result = itemService.queryRelatedItems(item1.getId(), 5);

            // 应该返回同分类的商品，排除自身
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ItemVO::getName)
                    .containsExactlyInAnyOrder("商品B", "商品C");
        }

        @Test
        @DisplayName("查询相关推荐商品-限制数量")
        void queryRelatedItems_limitsCount() {
            // 创建测试商品
            Item item1 = new Item();
            item1.setName("商品A");
            item1.setCategory("电子产品");
            item1.setPrice(10000);
            item1.setStock(10);
            item1.setStatus(1);
            itemMapper.insert(item1);

            Item item2 = new Item();
            item2.setName("商品B");
            item2.setCategory("电子产品");
            item2.setPrice(20000);
            item2.setStock(20);
            item2.setStatus(1);
            itemMapper.insert(item2);

            Item item3 = new Item();
            item3.setName("商品C");
            item3.setCategory("电子产品");
            item3.setPrice(30000);
            item3.setStock(30);
            item3.setStatus(1);
            itemMapper.insert(item3);

            // 查询相关商品，限制数量为1
            List<ItemVO> result = itemService.queryRelatedItems(item1.getId(), 1);

            // 应该只返回1个商品
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("查询相关推荐商品-商品不存在返回空列表")
        void queryRelatedItems_nonExistentItem_returnsEmpty() {
            List<ItemVO> result = itemService.queryRelatedItems(99999L, 5);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("查询相关推荐商品-无同分类商品返回空列表")
        void queryRelatedItems_noSameCategory_returnsEmpty() {
            // 创建测试商品
            Item item1 = new Item();
            item1.setName("商品A");
            item1.setCategory("电子产品");
            item1.setPrice(10000);
            item1.setStock(10);
            item1.setStatus(1);
            itemMapper.insert(item1);

            Item item2 = new Item();
            item2.setName("商品B");
            item2.setCategory("服装");
            item2.setPrice(20000);
            item2.setStock(20);
            item2.setStatus(1);
            itemMapper.insert(item2);

            // 查询相关商品
            List<ItemVO> result = itemService.queryRelatedItems(item1.getId(), 5);

            // 应该返回空列表
            assertThat(result).isEmpty();
        }
    }

    // ---- batchUpdateStatus ----

    @Nested
    @DisplayName("batchUpdateStatus")
    class BatchUpdateStatusTests {

        @Test
        @DisplayName("批量更新商品状态")
        void batchUpdateStatus_updatesAll() {
            // 创建测试商品
            Item item1 = new Item();
            item1.setName("商品A");
            item1.setPrice(10000);
            item1.setStock(10);
            item1.setStatus(1);
            itemMapper.insert(item1);

            Item item2 = new Item();
            item2.setName("商品B");
            item2.setPrice(20000);
            item2.setStock(20);
            item2.setStatus(1);
            itemMapper.insert(item2);

            // 批量更新状态为下架
            itemService.batchUpdateStatus(List.of(item1.getId(), item2.getId()), 2);

            // 验证状态已更新
            Item updated1 = itemMapper.selectById(item1.getId());
            Item updated2 = itemMapper.selectById(item2.getId());
            assertThat(updated1.getStatus()).isEqualTo(2);
            assertThat(updated2.getStatus()).isEqualTo(2);
        }

        @Test
        @DisplayName("批量更新商品状态-部分商品不存在")
        void batchUpdateStatus_partialNonExistent_updatesExisting() {
            // 创建测试商品
            Item item1 = new Item();
            item1.setName("商品A");
            item1.setPrice(10000);
            item1.setStock(10);
            item1.setStatus(1);
            itemMapper.insert(item1);

            // 批量更新状态，包含不存在的ID
            itemService.batchUpdateStatus(List.of(item1.getId(), 99999L), 2);

            // 验证存在的商品状态已更新
            Item updated1 = itemMapper.selectById(item1.getId());
            assertThat(updated1.getStatus()).isEqualTo(2);
        }
    }
}
