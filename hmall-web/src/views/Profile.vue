<template>
  <div class="wrap">
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
                <div class="av">
                  <img v-if="form.avatar" :src="form.avatar" alt="头像" class="av-img" />
                  <span v-else>{{ avatarLetter }}</span>
                </div>
                <div>
                  <input
                    ref="avatarInput"
                    type="file"
                    accept="image/jpeg,image/png"
                    style="display: none"
                    @change="onAvatarChange"
                  />
                  <button class="btn btn-ghost btn-sm" :disabled="uploading" @click="triggerAvatar">
                    {{ uploading ? '上传中...' : '更换头像' }}
                  </button>
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
                    <input v-model="form.phone" class="input" readonly placeholder="未绑定手机号" />
                    <span class="pre dim" style="font-size: 12px">暂不支持在线更换</span>
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
              <div class="lv">◆ 好集会员</div>
              <div class="nm">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
              </div>
              <div class="pts">
                <div class="p">
                  <b>{{ (stats.balance / 100).toFixed(0) }}</b
                  ><span>余额(元)</span>
                </div>
                <div class="p">
                  <b>{{ stats.coupons }}</b
                  ><span>优惠券</span>
                </div>
                <div class="p">
                  <b>{{ stats.favorites }}</b
                  ><span>收藏</span>
                </div>
              </div>
            </div>
            <div class="quick-stat">
              <div class="s">
                <b>{{ stats.pendingPay }}</b
                ><span>待付款</span>
              </div>
              <div class="s">
                <b>{{ stats.pendingRecv }}</b
                ><span>待收货</span>
              </div>
              <div class="s">
                <b>{{ stats.favorites }}</b
                ><span>收藏</span>
              </div>
              <div class="s">
                <b>{{ stats.orders }}</b
                ><span>全部订单</span>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { getProfile, updateProfile } from '@/api/user';
import { getFavorites, uploadImage } from '@/api/common';
import { getOrders, getMyCoupons } from '@/api/order';
import { ElMessage } from 'element-plus';
import AccountSidebar from '@/components/AccountSidebar.vue';
import { countOrderPage, mergeOrderStats, hasNextPage } from '@/utils/profileStats';
import { validateAvatarFile } from '@/utils/avatarUpload';

const userStore = useUserStore();
const avatarInput = ref(null);
const uploading = ref(false);

const form = reactive({
  username: userStore.userInfo?.username || '',
  nickname: userStore.userInfo?.nickname || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '',
  avatar: userStore.userInfo?.avatar || '',
  gender: userStore.userInfo?.gender || 'N',
  birthday: userStore.userInfo?.birthday || '',
});

// 账户统计：余额取自登录用户信息，其余计数来自真实接口（收藏/优惠券/订单）
const stats = reactive({
  balance: userStore.userInfo?.balance || 0,
  coupons: 0,
  favorites: 0,
  pendingPay: 0,
  pendingRecv: 0,
  orders: 0,
});

// 从后端拉取当前用户真实资料并填充表单（登录态仅缓存了少量字段）
async function loadProfile() {
  try {
    const profile = await getProfile();
    if (!profile) return;
    form.username = profile.username || form.username;
    form.nickname = profile.nickname || '';
    form.email = profile.email || '';
    form.phone = profile.phone || '';
    form.avatar = profile.avatar || '';
    form.gender = profile.gender || 'N';
    form.birthday = profile.birthday || '';
    if (profile.balance != null) stats.balance = profile.balance;
    // 同步到全局 userStore，使其它页面也能读到最新资料
    userStore.userInfo = { ...userStore.userInfo, ...profile };
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo));
  } catch (err) {
    /* 未登录或失败时沿用缓存值，错误已由拦截器提示 */
  }
}

const PAGE_SIZE = 100;

async function loadStats() {
  try {
    const favs = await getFavorites();
    stats.favorites = (favs || []).length;
  } catch (err) {
    /* 未登录或失败时保持 0，错误已由拦截器提示 */
  }
  try {
    const coupons = await getMyCoupons();
    stats.coupons = (coupons || []).length;
  } catch (err) {
    /* 忽略 */
  }
  try {
    // 分页汇总全部订单，避免只统计第一页导致超过 100 单时计数不准
    let page = 1;
    let pages = 1;
    let total = 0;
    let agg = { pendingPay: 0, pendingRecv: 0 };
    do {
      const resp = await getOrders({ page, size: PAGE_SIZE });
      const list = resp?.list || [];
      total = resp?.total || total || list.length;
      pages = resp?.pages || 1;
      agg = mergeOrderStats(agg, countOrderPage(list));
      page += 1;
    } while (hasNextPage(page - 1, pages));
    stats.orders = total;
    stats.pendingPay = agg.pendingPay;
    stats.pendingRecv = agg.pendingRecv;
  } catch (err) {
    /* 忽略 */
  }
}

onMounted(() => {
  loadProfile();
  loadStats();
});

const avatarLetter = computed(() => {
  const name = form.nickname || form.username || '用户';
  return name.charAt(0);
});

function triggerAvatar() {
  avatarInput.value?.click();
}

async function onAvatarChange(event) {
  const file = event.target.files?.[0];
  // 清空 input 以便再次选择同一文件也能触发 change
  event.target.value = '';
  const { ok, reason } = validateAvatarFile(file);
  if (!ok) {
    if (reason === 'type') ElMessage.warning('仅支持 JPG / PNG 格式图片');
    else if (reason === 'size') ElMessage.warning('图片大小不能超过 2MB');
    return;
  }
  uploading.value = true;
  try {
    const url = await uploadImage(file);
    if (!url) {
      ElMessage.error('头像上传失败');
      return;
    }
    form.avatar = url;
    // 上传成功后立即持久化头像，避免用户忘记点保存而丢失
    await updateProfile({ avatar: url });
    userStore.userInfo = { ...userStore.userInfo, avatar: url };
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo));
    ElMessage.success('头像已更新');
  } catch (err) {
    console.error(err);
  } finally {
    uploading.value = false;
  }
}

async function save() {
  try {
    await updateProfile({
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar,
      gender: form.gender,
      birthday: form.birthday || null,
    });
    userStore.userInfo = {
      ...userStore.userInfo,
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar,
      gender: form.gender,
      birthday: form.birthday,
    };
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo));
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
  overflow: hidden;
}
.avatar-up .av .av-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
