<template>
  <div>
    <h2>个人信息</h2>
    <el-card style="max-width: 500px"
      ><el-form :model="form" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="头像"
          ><el-input v-model="form.avatar" placeholder="头像URL"
        /></el-form-item>
        <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
      </el-form></el-card
    >
  </div>
</template>
<script setup>
import { reactive } from 'vue';
import { updateProfile } from '@/api/user';
import { ElMessage } from 'element-plus';
const form = reactive({ nickname: '', email: '', avatar: '' });
async function save() {
  try {
    await updateProfile(form);
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}
</script>
