<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f7fa">
    <el-card style="width:400px"><h2 style="text-align:center;margin-bottom:20px">管理员登录</h2>
      <el-form :model="form" label-width="0">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" @keyup.enter="doLogin" /></el-form-item>
        <el-form-item><el-button type="primary" @click="doLogin" :loading="loading" style="width:100%">登录</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
const router = useRouter(); const loading = ref(false)
const form = reactive({ username: '', password: '' })
async function doLogin() {
  loading.value = true
  try {
    const data = await login(form)
    if (data.role !== 'admin') { ElMessage.error('非管理员账号'); return }
    localStorage.setItem('adminToken', data.token)
    localStorage.setItem('adminInfo', JSON.stringify(data))
    router.push('/dashboard')
  } catch {} finally { loading.value = false }
}
</script>
