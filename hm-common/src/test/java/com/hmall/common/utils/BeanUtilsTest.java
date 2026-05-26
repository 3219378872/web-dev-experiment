package com.hmall.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeanUtilsTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {
        private String name;
        private int age;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Target {
        private String name;
        private int age;
        private String tag;
    }

    @Test
    @DisplayName("copyBean(null, Class) 返回 null —— 防御 mapper 上游空结果")
    void copyBean_nullSource_returnsNull() {
        assertThat(BeanUtils.copyBean(null, Target.class)).isNull();
    }

    @Test
    @DisplayName("copyBean 复制同名字段")
    void copyBean_copiesMatchingFields() {
        Target t = BeanUtils.copyBean(new Source("alice", 30), Target.class);
        assertThat(t).isNotNull();
        assertThat(t.getName()).isEqualTo("alice");
        assertThat(t.getAge()).isEqualTo(30);
        assertThat(t.getTag()).isNull();
    }

    @Test
    @DisplayName("copyBean(source, Class, convert) 在拷贝后调用 convert 补字段")
    void copyBean_withConverter_appliesAfterCopy() {
        Target t = BeanUtils.copyBean(new Source("bob", 25), Target.class,
                (s, tgt) -> tgt.setTag(s.getName() + "@" + s.getAge()));
        assertThat(t.getTag()).isEqualTo("bob@25");
        assertThat(t.getName()).isEqualTo("bob");
    }

    @Test
    @DisplayName("copyBean 三参版 null 源对象返回 null，convert 仍以 (null,null) 被调")
    void copyBean_threeArg_nullSource_invokesConverterWithNulls() {
        // 当前实现 (BeanUtils.java) 没有在 source=null 时短路 convert——
        // 它会用 source=null、target=null 调 Convert.convert，因此 lambda 必须能容忍 null。
        // 如果哪天 BeanUtils 收紧此行为，本测试需相应更新。
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        Target t = BeanUtils.copyBean(null, Target.class, (s, tgt) -> {
            calls.incrementAndGet();
            assertThat(s).isNull();
            assertThat(tgt).isNull();
        });
        assertThat(t).isNull();
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("copyList 空 / null 输入返回不可变空列表")
    void copyList_emptyOrNull_returnsEmptyList() {
        assertThat(BeanUtils.copyList(null, Target.class)).isEmpty();
        assertThat(BeanUtils.copyList(Collections.emptyList(), Target.class)).isEmpty();
    }

    @Test
    @DisplayName("copyList 复制每个元素")
    void copyList_copiesEachElement() {
        List<Source> sources = Arrays.asList(new Source("a", 1), new Source("b", 2));
        List<Target> targets = BeanUtils.copyList(sources, Target.class);
        assertThat(targets).hasSize(2);
        assertThat(targets).extracting(Target::getName).containsExactly("a", "b");
        assertThat(targets).extracting(Target::getAge).containsExactly(1, 2);
    }

    @Test
    @DisplayName("copyList(list, Class, convert) 用 convert 补充字段")
    void copyList_withConvert_appliesPerElement() {
        List<Source> sources = Arrays.asList(new Source("a", 1), new Source("b", 2));
        List<Target> targets = BeanUtils.copyList(sources, Target.class,
                (s, tgt) -> tgt.setTag("T-" + s.getName()));
        assertThat(targets).extracting(Target::getTag).containsExactly("T-a", "T-b");
    }
}
