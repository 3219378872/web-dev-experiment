<template>
  <div class="auth-page">
    <div class="auth-top">
      <router-link to="/" class="logo">
        <span class="mark">集</span>
        <span class="name">好集<small>HAOJI MALL</small></span>
      </router-link>
    </div>
    <div class="auth-wrap">
      <!-- 左侧 promo 区域：根据当前 mode 展示不同文案 -->
      <div class="auth-promo">
        <template v-if="mode === 'login' || mode === 'sms'">
          <div class="eyebrow">欢迎回来</div>
          <h1>万物好集<br />登录即享会员价</h1>
          <p>登录后可享受会员专属折扣、积分商城、收藏夹同步与订单全程跟踪。</p>
          <div class="auth-feats">
            <div class="f"><span class="d">🎟</span>专属优惠券</div>
            <div class="f"><span class="d">⚡</span>限时秒杀提醒</div>
            <div class="f"><span class="d">🛡</span>账户安全保障</div>
          </div>
        </template>
        <template v-else-if="mode === 'register'">
          <div class="eyebrow">新人专享</div>
          <h1>注册好集<br />立得 90 元新人礼包</h1>
          <p>注册即送 ¥20 + ¥50 + 9折券组合，首单再享会员价，下单更省心。</p>
          <div class="auth-feats">
            <div class="f"><span class="d">🎁</span>90元新人礼包</div>
            <div class="f"><span class="d">🚚</span>新人免邮券</div>
            <div class="f"><span class="d">💎</span>会员成长体系</div>
          </div>
        </template>
        <template v-else>
          <div class="eyebrow">账户安全</div>
          <h1>找回密码<br />三步重置登录密码</h1>
          <p>通过注册邮箱或手机号验证身份，安全便捷地重置您的登录密码。</p>
          <div class="auth-feats">
            <div class="f"><span class="d">📧</span>邮箱验证</div>
            <div class="f"><span class="d">🔐</span>身份核验</div>
            <div class="f"><span class="d">✓</span>重置完成</div>
          </div>
        </template>
      </div>

      <div class="auth-card">
        <!-- 登录 -->
        <template v-if="mode === 'login'">
          <div class="tabs">
            <button class="active">账号登录</button>
            <button @click="switchMode('sms')">短信登录</button>
          </div>

          <div class="field">
            <label>账号 / 邮箱 / 手机号</label>
            <input
              v-model="loginForm.username"
              class="input"
              placeholder="请输入账号、邮箱或手机号"
            />
          </div>
          <div class="field">
            <label>登录密码</label>
            <input
              v-model="loginForm.password"
              class="input"
              type="password"
              placeholder="请输入密码"
              @keyup.enter="doLogin"
            />
          </div>
          <div class="row-between">
            <label
              style="
                display: flex;
                gap: 8px;
                align-items: center;
                color: var(--ink-2);
                font-size: 13px;
                cursor: pointer;
              "
            >
              <span class="checkbox" :class="{ on: remember }" @click.stop="remember = !remember">{{
                remember ? '✓' : ''
              }}</span
              >记住密码
            </label>
            <a style="color: var(--brand); cursor: pointer" @click="switchMode('reset')"
              >忘记密码？</a
            >
          </div>
          <button class="btn btn-hot btn-lg btn-block" :disabled="loading" @click="doLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
          <div class="other">还没有账号？<a @click="switchMode('register')">立即注册 ›</a></div>
          <div class="other" style="margin-top: 18px; position: relative">
            <span style="background: #fff; padding: 0 12px; position: relative; z-index: 2"
              >其他登录方式</span
            >
            <span
              style="
                position: absolute;
                left: 0;
                right: 0;
                top: 50%;
                height: 1px;
                background: var(--line);
              "
            ></span>
          </div>
          <div class="socials">
            <a class="s">微</a><a class="s">支</a><a class="s">Q</a><a class="s">微博</a>
          </div>
        </template>

        <!-- 短信登录 -->
        <template v-else-if="mode === 'sms'">
          <div class="tabs">
            <button @click="switchMode('login')">账号登录</button>
            <button class="active">短信登录</button>
          </div>

          <div class="field">
            <label>手机号</label>
            <input v-model="smsForm.phone" class="input" placeholder="请输入手机号" />
          </div>
          <div class="field">
            <label>验证码</label>
            <div class="code-row">
              <input
                v-model="smsForm.code"
                class="input"
                placeholder="请输入验证码"
                @keyup.enter="doSmsLogin"
              />
              <button class="btn btn-ghost" :disabled="smsCountdown > 0" @click="sendSmsCode">
                {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
              </button>
            </div>
          </div>
          <button class="btn btn-hot btn-lg btn-block" :disabled="loading" @click="doSmsLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
          <div class="other">还没有账号？<a @click="switchMode('register')">立即注册 ›</a></div>
          <div class="other" style="margin-top: 18px; position: relative">
            <span style="background: #fff; padding: 0 12px; position: relative; z-index: 2"
              >其他登录方式</span
            >
            <span
              style="
                position: absolute;
                left: 0;
                right: 0;
                top: 50%;
                height: 1px;
                background: var(--line);
              "
            ></span>
          </div>
          <div class="socials">
            <a class="s">微</a><a class="s">支</a><a class="s">Q</a><a class="s">微博</a>
          </div>
        </template>

        <!-- 注册 -->
        <template v-else-if="mode === 'register'">
          <h2>创建账号</h2>
          <div class="sub">已有账号？<a @click="switchMode('login')">直接登录 ›</a></div>

          <div class="field">
            <label>邮箱地址</label>
            <input
              v-model="registerForm.email"
              class="input"
              placeholder="用于登录与找回密码，如 name@mail.com"
            />
          </div>
          <div class="field">
            <label>手机号</label>
            <input v-model="registerForm.phone" class="input" placeholder="请输入 11 位手机号" />
          </div>
          <div class="field">
            <label>验证码</label>
            <div class="code-row">
              <input v-model="registerForm.code" class="input" placeholder="6 位验证码" />
              <button
                class="btn btn-outline"
                :disabled="codeCountdown > 0"
                @click="sendRegisterCode"
              >
                {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
              </button>
            </div>
          </div>
          <div class="field">
            <label>设置密码</label>
            <input
              v-model="registerForm.password"
              class="input"
              type="password"
              placeholder="8-20 位，含字母与数字"
            />
            <div class="strength">
              <i class="on"></i><i class="on"></i><i class="on"></i><i></i>
            </div>
            <span class="dim" style="font-size: 11.5px">密码强度：中</span>
          </div>
          <div class="field">
            <label>确认密码</label>
            <input
              v-model="registerForm.confirmPassword"
              class="input"
              type="password"
              placeholder="请再次输入密码"
            />
          </div>
          <div class="agree">
            <span
              class="checkbox"
              :class="{ on: agreed }"
              style="margin-top: 1px"
              @click="agreed = !agreed"
              >{{ agreed ? '✓' : '' }}</span
            >
            <span>我已阅读并同意《<a>好集用户协议</a>》和《<a>隐私政策</a>》</span>
          </div>
          <button class="btn btn-hot btn-lg btn-block" @click="doRegister">注 册</button>
        </template>

        <!-- 找回密码 -->
        <template v-else-if="mode === 'reset'">
          <h2>找回密码</h2>
          <div class="sub">记起密码了？<a @click="switchMode('login')">返回登录 ›</a></div>

          <div class="steps fp-steps">
            <div class="st on"><span class="n">1</span>验证身份</div>
            <div class="line"></div>
            <div class="st"><span class="n">2</span>设置新密码</div>
            <div class="line"></div>
            <div class="st"><span class="n">3</span>完成</div>
          </div>

          <div class="field">
            <label>注册邮箱 / 手机号</label>
            <input
              v-model="resetForm.email"
              class="input"
              placeholder="请输入注册时的邮箱或手机号"
            />
          </div>
          <div class="field">
            <label>图形验证码</label>
            <div class="code-row">
              <input v-model="resetForm.captcha" class="input" placeholder="请输入右侧字符" />
              <span
                style="
                  width: 96px;
                  border-radius: 8px;
                  background: linear-gradient(110deg, #88a6b8, #b79cc4);
                  display: grid;
                  place-items: center;
                  color: #fff;
                  font-weight: 900;
                  letter-spacing: 4px;
                  font-style: italic;
                  font-size: 18px;
                "
                >8 K 2 d</span
              >
            </div>
          </div>
          <div class="field">
            <label>短信/邮箱验证码</label>
            <div class="code-row">
              <input v-model="resetForm.code" class="input" placeholder="6 位验证码" />
              <button class="btn btn-outline" :disabled="codeCountdown > 0" @click="sendResetCode">
                {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
              </button>
            </div>
          </div>
          <div class="field">
            <label>设置新密码</label>
            <input
              v-model="resetForm.newPassword"
              type="password"
              class="input"
              placeholder="请输入 6-20 位新密码"
            />
          </div>

          <button class="btn btn-hot btn-lg btn-block" style="margin-top: 8px" @click="doReset">
            重置密码
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import { register, sendCode, resetPassword } from '@/api/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const mode = ref('login');
const loading = ref(false);
const remember = ref(false);
const agreed = ref(false);
const codeCountdown = ref(0);
const smsCountdown = ref(0);

const loginForm = reactive({ username: '', password: '' });
const smsForm = reactive({ phone: '', code: '' });
const registerForm = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  code: '',
  confirmPassword: '',
});
const resetForm = reactive({ email: '', code: '', newPassword: '', captcha: '' });

function switchMode(target) {
  mode.value = target;
}

onMounted(() => {
  const path = route.path;
  if (path === '/register') {
    mode.value = 'register';
  } else if (path === '/forgot') {
    mode.value = 'reset';
  } else {
    // /login 或默认
    const tab = route.query.tab;
    if (tab === 'register') mode.value = 'register';
    else if (tab === 'forgot') mode.value = 'reset';
    else if (tab === 'sms') mode.value = 'sms';
    else mode.value = 'login';
  }
});

function startCountdown(type = 'code') {
  if (type === 'sms') {
    smsCountdown.value = 60;
    const timer = setInterval(() => {
      smsCountdown.value--;
      if (smsCountdown.value <= 0) clearInterval(timer);
    }, 1000);
  } else {
    codeCountdown.value = 60;
    const timer = setInterval(() => {
      codeCountdown.value--;
      if (codeCountdown.value <= 0) clearInterval(timer);
    }, 1000);
  }
}

async function doLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码');
    return;
  }
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

async function doSmsLogin() {
  if (!smsForm.phone || !smsForm.code) {
    ElMessage.warning('请填写手机号和验证码');
    return;
  }
  loading.value = true;
  try {
    // 复用 login API，phone 映射到 username，code 映射到 password
    await userStore.login({ username: smsForm.phone, password: smsForm.code });
    router.push('/');
    ElMessage.success('登录成功');
  } catch (err) {
    console.error('短信登录失败:', err);
  } finally {
    loading.value = false;
  }
}

async function sendSmsCode() {
  if (!smsForm.phone) {
    ElMessage.warning('请输入手机号');
    return;
  }
  try {
    await sendCode(smsForm.phone);
    startCountdown('sms');
    ElMessage.success('验证码已发送');
  } catch (err) {
    console.error(err);
  }
}

async function sendRegisterCode() {
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  try {
    await sendCode(registerForm.email);
    startCountdown();
    ElMessage.success('验证码已发送');
  } catch (err) {
    console.error(err);
  }
}

async function sendResetCode() {
  if (!resetForm.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  try {
    await sendCode(resetForm.email);
    startCountdown();
    ElMessage.success('验证码已发送');
  } catch (err) {
    console.error(err);
  }
}

async function doRegister() {
  if (!agreed.value) {
    ElMessage.warning('请同意用户协议');
    return;
  }
  try {
    await register(registerForm);
    ElMessage.success('注册成功，请登录');
    switchMode('login');
  } catch (err) {
    console.error(err);
  }
}

async function doReset() {
  if (!resetForm.email || !resetForm.code || !resetForm.newPassword) {
    ElMessage.warning('请填写邮箱/手机号、验证码与新密码');
    return;
  }
  try {
    await resetPassword(resetForm);
    ElMessage.success('密码重置成功，请登录');
    switchMode('login');
  } catch (err) {
    console.error(err);
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
  box-shadow: var(--shadow-3);
}
.auth-card h2 {
  font-size: 22px;
  font-weight: 900;
  margin-bottom: 6px;
}
.auth-card .sub {
  color: var(--ink-3);
  font-size: 13px;
  margin-bottom: 22px;
}
.auth-card .tabs {
  margin-bottom: 24px;
}
.auth-card .tabs button {
  font-size: 16px;
  padding: 0 0 14px;
  margin-right: 28px;
  border: 0;
  background: none;
  cursor: pointer;
  color: var(--ink-2);
}
.auth-card .tabs button.active {
  color: var(--brand);
  font-weight: 700;
}
.auth-card .tabs button:hover {
  color: var(--brand);
}
.auth-card .field {
  margin-bottom: 16px;
}
.auth-card .field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
}
.auth-card .input {
  padding: 13px 14px;
}
.auth-card .row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  margin: 4px 0 18px;
}
.auth-card .code-row {
  display: flex;
  gap: 10px;
}
.auth-card .code-row .btn {
  white-space: nowrap;
}
.auth-card .other {
  text-align: center;
  margin-top: 22px;
  color: var(--ink-3);
  font-size: 12.5px;
}
.auth-card .other a {
  color: var(--brand);
  font-weight: 700;
  cursor: pointer;
}
.auth-card .socials {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 14px;
}
.auth-card .socials .s {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid var(--line);
  display: grid;
  place-items: center;
  font-size: 18px;
  color: var(--ink-2);
  cursor: pointer;
}
.auth-card .socials .s:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.auth-card .agree {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 12px;
  color: var(--ink-3);
  margin: 4px 0 18px;
  line-height: 1.5;
}
.auth-card .agree a {
  color: var(--brand);
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 1.5px solid var(--line-2);
  border-radius: 5px;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  flex-shrink: 0;
  background: #fff;
  font-size: 11px;
}
.checkbox.on {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 700;
  background: #fff;
  color: var(--ink);
  transition: 0.14s;
  line-height: 1;
  cursor: pointer;
}
.btn:hover {
  transform: translateY(-1px);
}
.btn-hot {
  background: linear-gradient(135deg, #ff7a45, var(--brand-700));
  color: #fff;
  box-shadow: 0 4px 14px rgba(255, 77, 46, 0.32);
}
.btn-ghost {
  background: #fff;
  border-color: var(--line-2);
  color: var(--ink);
}
.btn-ghost:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.btn-outline {
  background: #fff;
  border-color: var(--brand);
  color: var(--brand);
}
.btn-outline:hover {
  background: var(--brand-softer);
}
.btn-lg {
  padding: 14px 30px;
  font-size: 16px;
  border-radius: 10px;
}
.btn-block {
  display: flex;
  width: 100%;
}
.btn[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.input {
  width: 100%;
  border: 1px solid var(--line-2);
  border-radius: var(--radius-sm);
  padding: 11px 13px;
  font-size: 14px;
  font-family: inherit;
  background: #fff;
  color: var(--ink);
  outline: 0;
  transition: 0.14s;
}
.input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-softer);
}
.input::placeholder {
  color: var(--ink-3);
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

.strength {
  display: flex;
  gap: 5px;
  margin-top: 8px;
}
.strength i {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  background: var(--line-2);
}
.strength i.on {
  background: var(--success);
}

.fp-steps {
  display: flex;
  align-items: center;
  margin-bottom: 26px;
}
.steps {
  display: flex;
  align-items: center;
}
.steps .st {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ink-3);
}
.steps .st.on {
  color: var(--brand);
  font-weight: 700;
}
.steps .st .n {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--line-2);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
}
.steps .st.on .n {
  background: var(--brand);
}
.steps .line {
  flex: 1;
  height: 2px;
  background: var(--line-2);
  margin: 0 8px;
  min-width: 24px;
}
</style>
