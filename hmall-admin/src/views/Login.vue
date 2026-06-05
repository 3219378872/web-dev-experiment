<template>
  <div class="alogin">
    <div class="al-side">
      <span class="blob b1"></span>
      <span class="blob b2"></span>
      <div class="brand">
        <span class="mark">集</span>
        <span class="nm">好集后台<small>HAOJI ADMIN</small></span>
      </div>
      <div class="mid">
        <h1>综合百货<br />运营管理后台</h1>
        <p>商品、订单、用户、营销内容一站式管理。数据驱动决策，让运营更高效。</p>
      </div>
      <div class="copyright">© 2026 好集 HAOJI · 仅授权管理员访问</div>
    </div>

    <div class="al-main">
      <div class="al-card">
        <h2>欢迎登录 👋</h2>
        <div class="sub">请输入管理员账号信息进入后台</div>

        <div class="field">
          <label>管理员账号</label>
          <input
            v-model="form.username"
            class="input"
            placeholder="请输入管理员账号"
            @keyup.enter="doLogin"
          />
        </div>
        <div class="field">
          <label>登录密码</label>
          <input
            v-model="form.password"
            class="input"
            type="password"
            placeholder="请输入登录密码"
            @keyup.enter="doLogin"
          />
        </div>
        <div class="field">
          <label>验证码</label>
          <div class="cap-row">
            <input
              v-model="form.captcha"
              class="input"
              placeholder="请输入右侧字符"
              @keyup.enter="doLogin"
            />
            <canvas
              ref="captchaRef"
              class="cap-canvas"
              width="110"
              height="44"
              title="点击刷新验证码"
              @click="refreshCaptcha"
            />
          </div>
        </div>

        <div class="row-between">
          <label class="remember-label">
            <span class="checkbox" :class="{ on: remember }" @click="remember = !remember">
              {{ remember ? '✓' : '' }}
            </span>
            记住登录状态
          </label>
          <a class="forgot-link">忘记密码？</a>
        </div>

        <button class="btn btn-hot btn-lg btn-block" :disabled="loading" @click="doLogin">
          {{ loading ? '登录中...' : '登录后台' }}
        </button>

        <p class="hint">本系统仅限授权人员使用，所有操作将被记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const remember = ref(true);
const captchaRef = ref(null);
const captchaCode = ref('');
const form = reactive({ username: '', password: '', captcha: '' });

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code = '';
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

function drawCaptcha() {
  const canvas = captchaRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  // background
  ctx.fillStyle = '#f3f4f6';
  ctx.fillRect(0, 0, w, h);

  // noise lines
  for (let i = 0; i < 6; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255},0.3)`;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(Math.random() * w, Math.random() * h);
    ctx.lineTo(Math.random() * w, Math.random() * h);
    ctx.stroke();
  }

  // noise dots
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255},0.5)`;
    ctx.beginPath();
    ctx.arc(Math.random() * w, Math.random() * h, 1, 0, 2 * Math.PI);
    ctx.fill();
  }

  // text
  captchaCode.value = generateCaptcha();
  const colors = ['#374151', '#dc2626', '#2563eb', '#059669', '#d97706'];
  for (let i = 0; i < captchaCode.value.length; i++) {
    ctx.font = `bold ${22 + Math.random() * 6}px sans-serif`;
    ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)];
    ctx.textBaseline = 'middle';
    const x = 15 + i * 22 + Math.random() * 4;
    const y = h / 2 + Math.random() * 6 - 3;
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate((Math.random() - 0.5) * 0.4);
    ctx.fillText(captchaCode.value[i], 0, 0);
    ctx.restore();
  }
}

function refreshCaptcha() {
  form.captcha = '';
  drawCaptcha();
}

async function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码');
    return;
  }
  if (!form.captcha) {
    ElMessage.warning('请输入验证码');
    return;
  }
  if (form.captcha.toUpperCase() !== captchaCode.value) {
    ElMessage.error('验证码错误');
    refreshCaptcha();
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
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  drawCaptcha();
});
</script>

<style scoped>
.alogin {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}

.al-side {
  background: linear-gradient(150deg, #1e2128, #2a1b14 70%, #46201a);
  color: #fff;
  padding: 54px 60px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.al-side .blob {
  position: absolute;
  border-radius: 50%;
}

.al-side .b1 {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(255, 77, 46, 0.3), transparent 70%);
  right: -120px;
  top: -80px;
}

.al-side .b2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 176, 32, 0.18), transparent 70%);
  left: -100px;
  bottom: -80px;
}

.al-side .brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.al-side .brand .mark {
  width: 48px;
  height: 48px;
  border-radius: 13px;
  background: linear-gradient(135deg, var(--brand), var(--brand-700));
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 24px;
  box-shadow: 0 6px 20px rgba(255, 77, 46, 0.4);
}

.al-side .brand .nm {
  font-weight: 900;
  font-size: 22px;
}

.al-side .brand .nm small {
  display: block;
  font-size: 10px;
  color: #8b919b;
  letter-spacing: 2px;
}

.al-side .mid {
  margin-top: auto;
  margin-bottom: auto;
  position: relative;
  z-index: 2;
}

.al-side .mid h1 {
  font-size: 40px;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 1.15;
}

.al-side .mid p {
  color: #aeb4be;
  font-size: 15px;
  margin-top: 14px;
  max-width: 380px;
  line-height: 1.7;
}

.copyright {
  color: #5b6370;
  font-size: 12px;
  position: relative;
  z-index: 2;
}

.al-main {
  display: grid;
  place-items: center;
  padding: 40px;
  background: #fff;
}

.al-card {
  width: 100%;
  max-width: 380px;
}

.al-card h2 {
  font-size: 26px;
  font-weight: 900;
}

.al-card .sub {
  color: var(--ink-3);
  font-size: 13.5px;
  margin: 6px 0 30px;
}

.al-card .field {
  margin-bottom: 18px;
}

.al-card .field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
  margin-bottom: 7px;
  display: block;
}

.al-card .input {
  padding: 13px 14px;
}

.cap-row {
  display: flex;
  gap: 10px;
}

.cap-canvas {
  width: 110px;
  height: 44px;
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid var(--line-2);
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  margin: 4px 0 22px;
}

.remember-label {
  display: flex;
  gap: 8px;
  align-items: center;
  color: var(--ink-2);
}

.forgot-link {
  color: var(--brand);
  cursor: pointer;
}

.hint {
  font-size: 11.5px;
  text-align: center;
  margin-top: 16px;
  color: var(--ink-3);
}

@media (max-width: 900px) {
  .alogin {
    grid-template-columns: 1fr;
  }
  .al-side {
    display: none;
  }
}
</style>
