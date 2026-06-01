package com.hmall.item.service.impl;

import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
