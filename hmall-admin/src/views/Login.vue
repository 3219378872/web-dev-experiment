<template>
  <div class="auth-page">
    <div class="auth-top">
      <router-link to="/" class="logo">
        <span class="mark">集</span>
        <span class="name">好集<small>HAOJI MALL</small></span>
      </router-link>
    </div>
    <div class="auth-wrap">
      <div class="auth-promo">
        <div class="eyebrow">管理员控制台</div>
        <h1>好集后台<br />数据驱动决策</h1>
        <p>登录后可查看实时销售数据、管理商品订单、处理用户反馈与系统配置。</p>
        <div class="auth-feats">
          <div class="f"><span class="d">📊</span>实时数据看板</div>
          <div class="f"><span class="d">📦</span>商品订单管理</div>
          <div class="f"><span class="d">👥</span>用户权限控制</div>
        </div>
      </div>

      <div class="auth-card">
        <h2 style="font-size: 22px; font-weight: 900; margin-bottom: 6px">管理员登录</h2>
        <p class="sub" style="color: var(--ink-3); font-size: 13px; margin-bottom: 22px">
          请输入管理员账号和密码
        </p>
        <div class="field">
          <label>用户名</label>
          <input
            v-model="form.username"
            class="input"
            placeholder="请输入用户名"
            @keyup.enter="doLogin"
          />
        </div>
        <div class="field">
          <label>密码</label>
          <input
            v-model="form.password"
            class="input"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="doLogin"
          />
        </div>
        <div class="row-between" style="margin-top: 4px">
          <label
            style="
              display: flex;
              gap: 8px;
              align-items: center;
              color: var(--ink-2);
              font-size: 13px;
            "
          >
            <span class="checkbox" :class="{ on: remember }" @click="remember = !remember">{{
              remember ? '✓' : ''
            }}</span
            >记住密码
          </label>
        </div>
        <button
          class="btn btn-hot btn-lg btn-block"
          style="margin-top: 18px"
          :disabled="loading"
          @click="doLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import { ElMessage } from 'element-plus';
const router = useRouter();
const loading = ref(false);
const remember = ref(false);
const form = reactive({ username: '', password: '' });
async function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码');
    return;
  }
  loading.value = true;
  try {
    const data = await login(form);
    if (data.role !== 'admin') {
      ElMessage.error('非管理员账号');
      return;
    }
    localStorage.setItem('adminToken', data.token);
    localStorage.setItem('adminInfo', JSON.stringify(data));
    router.push('/dashboard');
    ElMessage.success('登录成功');
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(120deg, #ff7a45, var(--brand) 55%, var(--brand-700));
}
.auth-top {
  padding: 20px 40px;
}
.auth-top .logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.auth-top .logo .mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, var(--brand), var(--brand-700));
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 900;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(255, 77, 46, 0.35);
}
.auth-top .logo .name {
  font-weight: 900;
  font-size: 21px;
  color: #fff;
  letter-spacing: -0.5px;
}
.auth-top .logo .name small {
  display: block;
  font-size: 10px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 3px;
}

.auth-wrap {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 440px;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  align-items: center;
  gap: 40px;
  padding: 0 24px 40px;
}
.auth-promo {
  color: #fff;
}
.auth-promo .eyebrow {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 4px;
  opacity: 0.85;
}
.auth-promo h1 {
  font-size: 46px;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 1.12;
  margin: 16px 0;
}
.auth-promo p {
  font-size: 16px;
  opacity: 0.92;
  max-width: 420px;
  line-height: 1.7;
}
.auth-feats {
  display: flex;
  gap: 24px;
  margin-top: 34px;
}
.auth-feats .f {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
.auth-feats .f .d {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.18);
  display: grid;
  place-items: center;
  font-size: 19px;
}
.auth-card {
  background: #fff;
  border-radius: 18px;
  padding: 36px 34px;
  box-shadow: 0 18px 48px rgba(60, 35, 20, 0.14);
}
.auth-card .field {
  margin-bottom: 16px;
}
.auth-card .field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
  margin-bottom: 7px;
  display: block;
}
.auth-card .input {
  padding: 13px 14px;
}
.auth-card .row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

@media (max-width: 900px) {
  .auth-wrap {
    grid-template-columns: 1fr;
    padding: 24px;
  }
  .auth-promo {
    display: none;
  }
}
</style>
