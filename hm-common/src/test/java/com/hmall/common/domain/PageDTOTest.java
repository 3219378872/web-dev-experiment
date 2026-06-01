package com.hmall.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageDTOTest {

    private Page<String> samplePage(List<String> records) {
        Page<String> p = new Page<>(1, 10, 100);
        p.setRecords(records);
        return p;
    }

    @Test
    @DisplayName("empty(total, pages) 返回空 list 与给定的分页摘要")
    void empty_byTotalsAndPages_returnsEmptyList() {
        PageDTO<String> dto = PageDTO.empty(42L, 5L);
        assertThat(dto.getTotal()).isEqualTo(42L);
        assertThat(dto.getPages()).isEqualTo(5L);
        assertThat(dto.getList()).isEmpty();
    }

    @Test
    @DisplayName("empty(page) 沿用 mybatis-plus Page 的 total/pages")
    void empty_byPage_inheritsTotalsAndPages() {
        Page<String> page = samplePage(Collections.emptyList());
        PageDTO<String> dto = PageDTO.empty(page);
        assertThat(dto.getTotal()).isEqualTo(100L);
        assertThat(dto.getPages()).isEqualTo(10L);
        assertThat(dto.getList()).isEmpty();
    }

    @Test
    @DisplayName("of(null) 返回完全空对象（容错 mapper 上游空查询）")
    void of_nullPage_returnsEmptyEnvelope() {
        PageDTO<String> dto = PageDTO.of((Page<String>) null);
        assertThat(dto.getTotal()).isNull();
        assertThat(dto.getPages()).isNull();
        assertThat(dto.getList()).isNull();
    }

    @Test
    @DisplayName("of(page) 透传 records 列表")
    void of_pageWithRecords_carriesRecords() {
        PageDTO<String> dto = PageDTO.of(samplePage(Arrays.asList("a", "b", "c")));
        assertThat(dto.getTotal()).isEqualTo(100L);
        assertThat(dto.getList()).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("of(page) records 为空时复用 empty(page) 路径")
    void of_pageWithEmptyRecords_returnsEmpty() {
        PageDTO<String> dto = PageDTO.of(samplePage(Collections.emptyList()));
        assertThat(dto.getList()).isEmpty();
        assertThat(dto.getTotal()).isEqualTo(100L);
    }

    @Test
    @DisplayName("of(page, mapper) 用 mapper 函数转换 records 类型")
    void of_pageWithMapper_appliesMapper() {
        Page<Integer> intPage = new Page<>(1, 10, 3);
        intPage.setRecords(Arrays.asList(1, 2, 3));
        PageDTO<String> dto = PageDTO.of(intPage, i -> "n=" + i);
        assertThat(dto.getList()).containsExactly("n=1", "n=2", "n=3");
        assertThat(dto.getTotal()).isEqualTo(3L);
    }

    @Test
    @DisplayName("of(page, mapper) page=null 返回完全空对象")
    void of_pageWithMapperNullPage_returnsEmptyEnvelope() {
        PageDTO<String> dto = PageDTO.of((Page<Integer>) null, i -> "n=" + i);
        assertThat(dto.getTotal()).isNull();
        assertThat(dto.getList()).isNull();
    }

    @Test
    @DisplayName("of(page, list) 直接用给定 list 不变换")
    void of_pageWithList_usesProvidedList() {
        Page<String> page = samplePage(Arrays.asList("ignored"));
        PageDTO<String> dto = PageDTO.of(page, Arrays.asList("x", "y"));
        assertThat(dto.getList()).containsExactly("x", "y");
        assertThat(dto.getTotal()).isEqualTo(100L);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Src {
        private String name;
        private int age;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Tgt {
        private String name;
        private int age;
        private String tag;
    }

    // ---- of(page, Class) overload ----

    @Test
    @DisplayName("of(page, Class) 用 BeanUtils.copyList 转换记录类型")
    void of_pageWithClass_copiesList() {
        Page<Src> page = new Page<>(1, 10, 2);
        page.setRecords(java.util.Arrays.asList(
                new Src("a", 1), new Src("b", 2)));
        PageDTO<Tgt> dto = PageDTO.of(page, Tgt.class);
        assertThat(dto.getTotal()).isEqualTo(2L);
        assertThat(dto.getList()).hasSize(2);
        assertThat(dto.getList().get(0).getName()).isEqualTo("a");
    }

    // ---- of(page, Class, Convert) overload ----

    @Test
    @DisplayName("of(page, Class, Convert) 用 convert 补充字段")
    void of_pageWithClassAndConvert_appliesConvert() {
        Page<Src> page = new Page<>(1, 10, 2);
        page.setRecords(java.util.Arrays.asList(
                new Src("a", 1), new Src("b", 2)));
        PageDTO<Tgt> dto = PageDTO.of(page, Tgt.class,
                (s, t) -> t.setTag("T-" + s.getName()));
        assertThat(dto.getList()).hasSize(2);
        assertThat(dto.getList().get(0).getTag()).isEqualTo("T-a");
        assertThat(dto.getList().get(1).getTag()).isEqualTo("T-b");
    }

    @Test
    @DisplayName("of(page, mapper) 空 page 走 empty 路径")
    void of_pageWithMapperEmpty_returnsEmpty() {
        Page<Integer> empty = new Page<>(1, 10, 0);
        empty.setRecords(java.util.Collections.emptyList());
        PageDTO<String> dto = PageDTO.of(empty, i -> "x");
        assertThat(dto.getList()).isEmpty();
        assertThat(dto.getTotal()).isEqualTo(0L);
    }
}
