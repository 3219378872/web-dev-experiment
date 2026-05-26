package com.hmall.item.it;

import com.hmall.common.utils.UserContext;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-item.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ItemServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IItemService itemService;

    @Test
    void deductStock_shouldReduceStock() {
        List<OrderDetailDTO> items = List.of(
                new OrderDetailDTO().setItemId(100L).setNum(2),
                new OrderDetailDTO().setItemId(101L).setNum(3)
        );
        itemService.deductStock(items);
        Item item1 = itemService.getById(100L);
        Item item2 = itemService.getById(101L);
        assertThat(item1.getStock()).isEqualTo(98);
        assertThat(item2.getStock()).isEqualTo(47);
    }

    @Test
    void deductStock_insufficientStock_shouldThrow() {
        List<OrderDetailDTO> items = List.of(
                new OrderDetailDTO().setItemId(100L).setNum(200)
        );
        assertThatThrownBy(() -> itemService.deductStock(items))
                .hasMessageContaining("库存不足");
    }

    @Test
    void queryItemByIds_shouldReturnMatchingItems() {
        List<ItemDTO> result = itemService.queryItemByIds(List.of(100L, 101L));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDTO::getId)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    void queryItemByIds_empty_shouldReturnEmptyList() {
        List<ItemDTO> result = itemService.queryItemByIds(List.of());
        assertThat(result).isEmpty();
    }
}
