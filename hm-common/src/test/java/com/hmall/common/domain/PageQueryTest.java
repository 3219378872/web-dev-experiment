package com.hmall.common.domain;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageQueryTest {

    @Test
    @DisplayName("默认 pageNo=1, pageSize=20, isAsc=true")
    void defaults_match20PerPageAsc() {
        PageQuery q = new PageQuery();
        assertThat(q.getPageNo()).isEqualTo(1);
        assertThat(q.getPageSize()).isEqualTo(20);
        assertThat(q.getIsAsc()).isTrue();
        assertThat(q.getSortBy()).isNull();
    }

    @Test
    @DisplayName("from() 计算偏移量 = (pageNo-1) * pageSize")
    void from_computesOffset() {
        PageQuery q = new PageQuery().setPageNo(3).setPageSize(15);
        assertThat(q.from()).isEqualTo(30);
    }

    @Test
    @DisplayName("toMpPage(orderItems) 携带传入的所有 OrderItem")
    void toMpPage_withOrderItems_usesAllOfThem() {
        PageQuery q = new PageQuery().setPageNo(2).setPageSize(5);
        OrderItem o1 = OrderItem.asc("created_at");
        OrderItem o2 = OrderItem.desc("id");
        Page<Object> page = q.toMpPage(o1, o2);
        assertThat(page.getCurrent()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(5);
        assertThat(page.orders()).extracting(OrderItem::getColumn).containsExactly("created_at", "id");
        assertThat(page.orders()).extracting(OrderItem::isAsc).containsExactly(true, false);
    }

    @Test
    @DisplayName("toMpPage() 无 orderItems 且未指定 sortBy 时不附加排序")
    void toMpPage_withoutSortBy_addsNoOrders() {
        PageQuery q = new PageQuery();
        Page<Object> page = q.toMpPage();
        assertThat(page.orders()).isEmpty();
    }

    @Test
    @DisplayName("toMpPage() 带 sortBy 时附加用户排序")
    void toMpPage_withSortBy_addsUserOrder() {
        PageQuery q = new PageQuery().setSortBy("name").setIsAsc(false);
        Page<Object> page = q.toMpPage();
        assertThat(page.orders()).hasSize(1);
        assertThat(page.orders().get(0).getColumn()).isEqualTo("name");
        assertThat(page.orders().get(0).isAsc()).isFalse();
    }

    @Test
    @DisplayName("toMpPage(default, asc) sortBy 为空时回退默认，并以 isAsc 排序")
    void toMpPage_withDefault_fallsBackWhenSortByBlank() {
        PageQuery q = new PageQuery();
        Page<Object> page = q.toMpPage("create_time", false);
        assertThat(page.orders()).hasSize(1);
        assertThat(page.orders().get(0).getColumn()).isEqualTo("create_time");
        assertThat(page.orders().get(0).isAsc()).isFalse();
        assertThat(q.getSortBy()).isEqualTo("create_time");
        assertThat(q.getIsAsc()).isFalse();
    }

    @Test
    @DisplayName("toMpPage(default, asc) sortBy 已设置时保留用户值，不覆盖")
    void toMpPage_withDefault_keepsExplicitSortBy() {
        PageQuery q = new PageQuery().setSortBy("price").setIsAsc(true);
        Page<Object> page = q.toMpPage("create_time", false);
        assertThat(page.orders().get(0).getColumn()).isEqualTo("price");
        assertThat(page.orders().get(0).isAsc()).isTrue();
    }

    @Test
    @DisplayName("toMpPageDefaultSortByCreateTimeDesc() 按 create_time 倒序")
    void toMpPageDefaultSortByCreateTimeDesc_descByCreateTime() {
        PageQuery q = new PageQuery();
        Page<Object> page = q.toMpPageDefaultSortByCreateTimeDesc();
        assertThat(page.orders()).hasSize(1);
        assertThat(page.orders().get(0).getColumn()).isEqualTo("create_time");
        assertThat(page.orders().get(0).isAsc()).isFalse();
    }
}
