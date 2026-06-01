<template>
  <div>
    <h2>意见反馈</h2>
    <el-card style="max-width: 600px">
      <el-input v-model="content" type="textarea" :rows="5" placeholder="请描述你的问题或建议" />
      <el-button type="primary" style="margin-top: 12px" @click="submit">提交反馈</el-button>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { submitFeedback } from '@/api/common';
import { ElMessage } from 'element-plus';
const content = ref('');
async function submit() {
  try {
    await submitFeedback({ content: content.value });
    content.value = '';
    ElMessage.success('反馈已提交');
  } catch (err) {
    console.error(err);
  }
}
</script>
