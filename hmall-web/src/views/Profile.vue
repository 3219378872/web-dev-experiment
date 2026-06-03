<template>
  <div class="wrap" style="padding: 20px 0">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span>个人中心<span class="s">/</span
      ><b style="color: var(--ink)">个人信息</b>
    </div>

    <div class="acct-layout">
      <AccountSidebar active="profile" />

      <main>
        <div class="pf-grid">
          <div class="panel">
            <div class="panel-head"><h3>个人信息</h3></div>
            <div class="panel-pad">
              <div class="avatar-up">
                <div class="av">{{ avatarLetter }}</div>
                <div>
                  <button class="btn btn-ghost btn-sm" @click="triggerAvatar">更换头像</button>
                  <p class="dim" style="font-size: 12px; margin-top: 8px">
                    支持 JPG / PNG，建议 200×200，不超过 2MB
                  </p>
                </div>
              </div>

              <div class="pf-form">
                <div class="field">
                  <label>用户名</label>
                  <input v-model="form.username" class="input" readonly />
                </div>
                <div class="field">
                  <label>昵称</label>
                  <input v-model="form.nickname" class="input" />
                </div>
                <div class="field">
                  <label>性别</label>
                  <div class="gender">
                    <span
                      class="chip"
                      :class="{ on: form.gender === 'F' }"
                      @click="form.gender = 'F'"
                      >女</span
                    >
                    <span
                      class="chip"
                      :class="{ on: form.gender === 'M' }"
                      @click="form.gender = 'M'"
                      >男</span
                    >
                    <span
                      class="chip"
                      :class="{ on: form.gender === 'N' }"
                      @click="form.gender = 'N'"
                      >保密</span
                    >
                  </div>
                </div>
                <div class="field">
                  <label>生日</label>
                  <input v-model="form.birthday" class="input" type="date" />
                </div>
                <div class="field">
                  <label>绑定邮箱</label>
                  <div class="input-group">
                    <input v-model="form.email" class="input" />
                    <span class="pre"><span class="tag tag-success">已验证</span></span>
                  </div>
                </div>
                <div class="field">
                  <label>绑定手机</label>
                  <div class="input-group">
                    <input v-model="form.phone" class="input" readonly />
                    <a class="pre" style="color: var(--brand); cursor: pointer">更换</a>
                  </div>
                </div>
                <div style="display: flex; gap: 12px; margin-top: 6px">
                  <button class="btn btn-primary" @click="save">保存修改</button>
                  <router-link class="btn btn-ghost" to="/forgot">修改登录密码</router-link>
                </div>
              </div>
            </div>
          </div>

          <aside>
            <div class="vip-card">
              <span class="b"></span>
              <div class="lv">◆ 黄金会员 V3</div>
              <div class="nm">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
              </div>
              <div class="pts">
                <div class="p"><b>1280</b><span>当前积分</span></div>
                <div class="p"><b>86</b><span>成长值</span></div>
                <div class="p"><b>32</b><span>优惠券</span></div>
              </div>
              <div class="vip-bar"><i style="width: 64%"></i></div>
              <p style="font-size: 11.5px; opacity: 0.7; margin-top: 8px">
                距升级铂金会员还需 420 成长值
              </p>
            </div>
            <div class="quick-stat">
              <div class="s"><b>2</b><span>待付款</span></div>
              <div class="s"><b>1</b><span>待收货</span></div>
              <div class="s"><b>8</b><span>收藏</span></div>
              <div class="s"><b>3</b><span>足迹</span></div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { updateProfile } from '@/api/user';
import { ElMessage } from 'element-plus';
import AccountSidebar from '@/components/AccountSidebar.vue';

const userStore = useUserStore();

const form = reactive({
  username: userStore.userInfo?.username || '',
  nickname: userStore.userInfo?.nickname || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '138****8866',
  avatar: userStore.userInfo?.avatar || '',
  gender: userStore.userInfo?.gender || 'N',
  birthday: userStore.userInfo?.birthday || '',
});

const avatarLetter = computed(() => {
  const name = form.nickname || form.username || '用户';
  return name.charAt(0);
});

function triggerAvatar() {
  ElMessage.info('头像上传功能开发中');
}

async function save() {
  try {
    await updateProfile({
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar,
    });
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}
</script>

<style scoped>
.pf-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 18px;
  align-items: start;
}
.pf-form {
  max-width: 520px;
}
.pf-form .field {
  margin-bottom: 18px;
}
.avatar-up {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
}
.avatar-up .av {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a45, var(--brand));
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 34px;
  font-weight: 900;
}
.gender {
  display: flex;
  gap: 10px;
}
.vip-card {
  background: linear-gradient(125deg, #2a1b14, #4a2c1e);
  border-radius: 14px;
  padding: 22px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.vip-card .b {
  position: absolute;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 176, 32, 0.16);
  right: -40px;
  top: -50px;
}
.vip-card .lv {
  font-size: 13px;
  color: var(--gold);
  font-weight: 700;
  letter-spacing: 1px;
}
.vip-card .nm {
  font-size: 20px;
  font-weight: 900;
  margin: 8px 0;
}
.vip-card .pts {
  display: flex;
  gap: 20px;
  margin-top: 14px;
}
.vip-card .pts .p b {
  font-size: 22px;
  font-weight: 900;
  display: block;
}
.vip-card .pts .p span {
  font-size: 11px;
  opacity: 0.7;
}
.vip-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  margin-top: 16px;
  overflow: hidden;
}
.vip-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #ffc24b, var(--gold));
}
.quick-stat {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px;
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  text-align: center;
}
.quick-stat .s b {
  font-size: 22px;
  font-weight: 900;
  color: var(--brand);
  display: block;
}
.quick-stat .s span {
  font-size: 12px;
  color: var(--ink-3);
}
@media (max-width: 1024px) {
  .pf-grid {
    grid-template-columns: 1fr;
  }
}
</style>
