<template>
  <span class="star-rating">
    <el-icon
      v-for="i in 5"
      :key="i"
      :class="{ filled: i <= modelValue }"
      @click="$emit('update:modelValue', i)"
    >
      <StarFilled v-if="i <= modelValue" /><Star v-else />
    </el-icon>
  </span>
</template>
<script setup>
defineProps({ modelValue: { type: Number, default: 0 } });
defineEmits(['update:modelValue']);
</script>
<style scoped>
.star-rating {
  display: inline-flex;
  gap: 2px;
  cursor: pointer;
}
.filled {
  color: #f7ba2a;
}
</style>
