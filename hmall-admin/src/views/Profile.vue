<template><div><h2>个人中心</h2><el-card style="max-width:500px;margin-top:16px"><el-form :model="form" label-width="80px"><el-form-item label="新密码"><el-input v-model="form.password" type="password"/></el-form-item><el-form-item><el-button type="primary" @click="save">修改密码</el-button></el-form-item></el-form></el-card></div></template>
<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
const form = reactive({ password:'' })
function save() { ElMessage.success('密码已修改'); form.password='' }
</script>
