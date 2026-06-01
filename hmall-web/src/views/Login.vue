<template>
  <div class="login-page">
    <el-card class="login-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="80px">
            <el-form-item label="用户名"><el-input v-model="loginForm.username" /></el-form-item>
            <el-form-item label="密码"
              ><el-input v-model="loginForm.password" type="password"
            /></el-form-item>
            <el-form-item
              ><el-button type="primary" :loading="loading" @click="doLogin"
                >登录</el-button
              ></el-form-item
            >
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item>
            <el-form-item label="密码"
              ><el-input v-model="registerForm.password" type="password"
            /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="registerForm.email" /></el-form-item>
            <el-form-item label="验证码">
              <el-input v-model="registerForm.code" style="width: 140px" />
              <el-button
                :disabled="codeCountdown > 0"
                style="margin-left: 8px"
                @click="sendRegisterCode"
              >
                {{ codeCountdown > 0 ? codeCountdown + 's' : '发送验证码' }}
              </el-button>
            </el-form-item>
            <el-form-item
              ><el-button type="primary" @click="doRegister">注册</el-button></el-form-item
            >
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="找回密码" name="reset">
          <el-form :model="resetForm" label-width="80px">
            <el-form-item label="邮箱"><el-input v-model="resetForm.email" /></el-form-item>
            <el-form-item label="验证码">
              <el-input v-model="resetForm.code" style="width: 140px" />
              <el-button
                :disabled="codeCountdown > 0"
                style="margin-left: 8px"
                @click="sendResetCode"
              >
                {{ codeCountdown > 0 ? codeCountdown + 's' : '发送验证码' }}
              </el-button>
            </el-form-item>
            <el-form-item label="新密码"
              ><el-input v-model="resetForm.newPassword" type="password"
            /></el-form-item>
            <el-form-item
              ><el-button type="primary" @click="doReset">重置密码</el-button></el-form-item
            >
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import { register, sendCode, resetPassword } from '@/api/user';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('login');
const loading = ref(false);
const codeCountdown = ref(0);

const loginForm = reactive({ username: '', password: '' });
const registerForm = reactive({ username: '', password: '', email: '', code: '' });
const resetForm = reactive({ email: '', code: '', newPassword: '' });

async function doLogin() {
  loading.value = true;
  try {
    await userStore.login(loginForm);
    router.push('/');
    ElMessage.success('登录成功');
  } catch (err) {
    console.error('登录失败:', err);
  } finally {
    loading.value = false;
  }
}

function startCountdown() {
  codeCountdown.value = 60;
  const timer = setInterval(() => {
    codeCountdown.value--;
    if (codeCountdown.value <= 0) clearInterval(timer);
  }, 1000);
}

async function sendRegisterCode() {
  try {
    await sendCode(registerForm.email);
    startCountdown();
    ElMessage.success('验证码已发送');
  } catch (err) {
    console.error(err);
  }
}

async function sendResetCode() {
  try {
    await sendCode(resetForm.email);
    startCountdown();
    ElMessage.success('验证码已发送');
  } catch (err) {
    console.error(err);
  }
}

async function doRegister() {
  try {
    await register(registerForm);
    ElMessage.success('注册成功，请登录');
    activeTab.value = 'login';
  } catch (err) {
    console.error(err);
  }
}

async function doReset() {
  try {
    await resetPassword(resetForm);
    ElMessage.success('密码重置成功，请登录');
    activeTab.value = 'login';
  } catch (err) {
    console.error(err);
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}
.login-card {
  width: 440px;
}
</style>
