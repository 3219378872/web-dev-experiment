package com.hmall.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollUtilsTest {

    @Test
    @DisplayName("emptyList / emptySet / emptyMap 返回 JDK 不可变空集合")
    void emptyHelpers_returnImmutableEmpties() {
        assertThat(CollUtils.emptyList()).isEmpty();
        assertThat(CollUtils.emptySet()).isEmpty();
        assertThat(CollUtils.emptyMap()).isEmpty();
    }

    @Test
    @DisplayName("singletonSet / singletonList 包装单元素")
    void singletonHelpers_wrapSingleElement() {
        assertThat(CollUtils.singletonList("a")).containsExactly("a");
        assertThat(CollUtils.singletonSet(7)).containsExactly(7);
    }

    @Test
    @DisplayName("convertToInteger 把字符串列表转为 Integer 列表")
    void convertToInteger_parsesEachElement() {
        List<Integer> out = CollUtils.convertToInteger(Arrays.asList("1", "2", "3"));
        assertThat(out).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("convertToInteger 空输入返回 null（按现有契约）")
    void convertToInteger_emptyInput_returnsNull() {
        assertThat(CollUtils.convertToInteger(Collections.emptyList())).isNull();
        assertThat(CollUtils.convertToInteger(null)).isNull();
    }

    @Test
    @DisplayName("convertToLong 把字符串列表转为 Long 列表")
    void convertToLong_parsesEachElement() {
        List<Long> out = CollUtils.convertToLong(Arrays.asList("10", "20"));
        assertThat(out).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("join 用分隔符连接元素；空输入返回 null")
    void join_concatenatesWithSeparator() {
        assertThat(CollUtils.join(Arrays.asList("a", "b", "c"), ",")).isEqualTo("a,b,c");
        assertThat(CollUtils.join(Collections.emptyList(), ",")).isNull();
        assertThat(CollUtils.join((java.util.Collection<String>) null, ",")).isNull();
    }

    @Test
    @DisplayName("joinIgnoreNull 跳过 null 元素并用逗号连接")
    void joinIgnoreNull_skipsNulls() {
        assertThat(CollUtils.joinIgnoreNull(Arrays.asList("a", null, "b", null), ","))
                .isEqualTo("a,b");
    }

    @Test
    @DisplayName("joinIgnoreNull 全为 null 时返回 null（不返回空串）")
    void joinIgnoreNull_allNull_returnsNull() {
        assertThat(CollUtils.joinIgnoreNull(Arrays.asList(null, null), ",")).isNull();
        assertThat(CollUtils.joinIgnoreNull(Collections.emptyList(), ",")).isNull();
        assertThat(CollUtils.joinIgnoreNull((java.util.Collection<String>) null, ",")).isNull();
    }
}
