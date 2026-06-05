<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>个人中心</h1>
        <p>管理账号信息、登录密码与角色权限</p>
      </div>
    </div>

    <div v-if="loading" style="text-align: center; padding: 60px 0; color: var(--ink-3)">
      加载中...
    </div>

    <template v-else>
      <div class="set-grid">
        <div>
          <div class="acard" style="margin-bottom: 16px">
            <div class="ah"><h3>账号信息</h3></div>
            <div class="ab">
              <div style="display: flex; align-items: center; gap: 18px; margin-bottom: 20px">
                <span
                  class="u-av"
                  style="
                    width: 72px;
                    height: 72px;
                    font-size: 30px;
                    border-radius: 16px;
                    background: linear-gradient(135deg, #ff7a45, var(--brand));
                  "
                  >管</span
                >
                <div>
                  <el-button class="btn-ghost" size="small">更换头像</el-button>
                  <p class="dim" style="font-size: 12px; margin-top: 8px">JPG/PNG，不超过 2MB</p>
                </div>
              </div>
              <div class="form-grid">
                <div class="field">
                  <label>管理员账号</label>
                  <el-input v-model="profileForm.account" readonly style="background: var(--bg)" />
                </div>
                <div class="field">
                  <label>姓名</label>
                  <el-input v-model="profileForm.name" />
                </div>
                <div class="field">
                  <label>手机号</label>
                  <el-input v-model="profileForm.phone" />
                </div>
                <div class="field">
                  <label>邮箱</label>
                  <el-input v-model="profileForm.email" />
                </div>
              </div>
              <el-button
                type="primary"
                style="margin-top: 16px"
                :loading="savingProfile"
                @click="saveProfile"
                >保存修改</el-button
              >
            </div>
          </div>

          <div class="acard">
            <div class="ah"><h3>修改密码</h3></div>
            <div class="ab" style="max-width: 420px">
              <div class="field" style="margin-bottom: 14px">
                <label>当前密码</label>
                <el-input v-model="pwdForm.current" type="password" placeholder="请输入当前密码" />
              </div>
              <div class="field" style="margin-bottom: 14px">
                <label>新密码</label>
                <el-input
                  v-model="pwdForm.newPwd"
                  type="password"
                  placeholder="8-20位，含字母数字与符号"
                />
              </div>
              <div class="field" style="margin-bottom: 14px">
                <label>确认新密码</label>
                <el-input
                  v-model="pwdForm.confirm"
                  type="password"
                  placeholder="请再次输入新密码"
                />
              </div>
              <el-button type="primary" :loading="savingPwd" @click="savePwd">更新密码</el-button>
            </div>
          </div>
        </div>

        <div>
          <div class="acard" style="margin-bottom: 16px">
            <div class="ah">
              <h3>角色与权限</h3>
              <span class="tag tag-brand">{{ roleName }}</span>
            </div>
            <div
              v-if="permissionsLoading"
              style="padding: 20px; text-align: center; color: var(--ink-3)"
            >
              加载中...
            </div>
            <div
              v-else-if="permissions.length === 0"
              class="ab"
              style="color: var(--ink-3); font-size: 13px"
            >
              暂无权限数据
            </div>
            <table v-else class="atable perm-table">
              <tbody>
                <tr v-for="p in permissions" :key="p">
                  <td>{{ permissionLabel(p) }}</td>
                  <td style="text-align: right">
                    <span class="sdot green">已授权</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="acard">
            <div class="ah"><h3>最近登录记录</h3></div>
            <div
              class="ab"
              style="text-align: center; padding: 20px; color: var(--ink-3); font-size: 13px"
            >
              暂无数据
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { updateAdminProfile, getAdminPermissions } from '@/api/user';
import { ElMessage } from 'element-plus';

const loading = ref(true);
const savingProfile = ref(false);
const savingPwd = ref(false);
const permissionsLoading = ref(true);
const permissions = ref([]);
const roleName = ref('超级管理员');

// 从 localStorage 加载管理员信息
const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}');

const profileForm = reactive({
  account: adminInfo.username || adminInfo.account || 'admin',
  name: adminInfo.name || '管理员',
  phone: adminInfo.phone || '—',
  email: adminInfo.email || '',
});

const pwdForm = reactive({ current: '', newPwd: '', confirm: '' });

function permissionLabel(code) {
  const labels = {
    dashboard: '数据看板',
    'item:read': '商品查看',
    'item:write': '商品管理（增删改）',
    'order:read': '订单查看',
    'order:write': '订单管理（发货/退款）',
    'user:read': '用户查看',
    'user:write': '用户管理（禁用/启用）',
    'review:write': '评价管理（删除）',
    'content:write': '内容管理（轮播/公告）',
    'system:write': '系统设置 / 管理员管理',
  };
  return labels[code] || code;
}

async function saveProfile() {
  savingProfile.value = true;
  try {
    await updateAdminProfile({
      username: profileForm.account,
      phone: profileForm.phone,
      email: profileForm.email,
    });
    ElMessage.success('信息已保存');
  } catch (err) {
    console.error(err);
    ElMessage.error('保存失败');
  } finally {
    savingProfile.value = false;
  }
}

async function savePwd() {
  if (!pwdForm.current || !pwdForm.newPwd || !pwdForm.confirm) {
    ElMessage.warning('请填写完整');
    return;
  }
  if (pwdForm.newPwd !== pwdForm.confirm) {
    ElMessage.error('两次输入不一致');
    return;
  }
  if (pwdForm.newPwd.length < 8) {
    ElMessage.error('新密码长度不少于8位');
    return;
  }
  savingPwd.value = true;
  try {
    await updateAdminProfile({
      oldPassword: pwdForm.current,
      password: pwdForm.newPwd,
    });
    ElMessage.success('密码已修改');
    pwdForm.current = '';
    pwdForm.newPwd = '';
    pwdForm.confirm = '';
  } catch (err) {
    console.error(err);
    ElMessage.error('密码修改失败');
  } finally {
    savingPwd.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    const result = await getAdminPermissions();
    if (Array.isArray(result)) {
      permissions.value = result;
    } else if (result && Array.isArray(result.data)) {
      permissions.value = result.data;
    }
  } catch (err) {
    console.error('加载权限失败', err);
    permissions.value = [];
  } finally {
    permissionsLoading.value = false;
    loading.value = false;
  }
});
</script>

<style scoped>
.adm-ph {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
}
.adm-ph h1 {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.adm-ph p {
  color: var(--ink-3);
  font-size: 13px;
  margin-top: 4px;
}

.set-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.acard {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
}
.acard .ah {
  padding: 16px 20px;
  border-bottom: 1px solid var(--admin-line);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.acard .ah h3 {
  font-size: 15px;
  font-weight: 700;
}
.acard .ab {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
}

.u-av {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.atable {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.atable thead th {
  background: var(--admin-bg);
  color: var(--ink-3);
  font-weight: 700;
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  white-space: nowrap;
  border-bottom: 1px solid var(--admin-line);
}
.atable tbody td {
  padding: 13px 16px;
  border-bottom: 1px solid var(--admin-line);
  vertical-align: middle;
}
.atable tbody tr:hover {
  background: #fbfbfc;
}
.atable tbody tr:last-child td {
  border-bottom: 0;
}

.perm-table td {
  padding: 11px 14px;
  font-size: 13px;
  border-bottom: 1px solid var(--admin-line);
}
.perm-table tr:last-child td {
  border-bottom: 0;
}

.sdot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
}
.sdot::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.sdot.green {
  color: var(--success);
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11.5px;
  font-weight: 700;
  line-height: 1.6;
}
.tag-brand {
  background: var(--brand-soft);
  color: var(--brand-700);
}

.dim {
  color: var(--ink-3);
}
.mono {
  font-family: var(--mono);
}
</style>
