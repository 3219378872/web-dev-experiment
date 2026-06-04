<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>个人中心</h1>
        <p>管理账号信息、登录密码与角色权限</p>
      </div>
    </div>

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
                <label>所属部门</label>
                <el-input v-model="profileForm.dept" />
              </div>
            </div>
            <el-button type="primary" style="margin-top: 16px" @click="saveProfile"
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
              <el-input v-model="pwdForm.confirm" type="password" placeholder="请再次输入新密码" />
            </div>
            <el-button type="primary" @click="savePwd">更新密码</el-button>
          </div>
        </div>
      </div>

      <div>
        <div class="acard" style="margin-bottom: 16px">
          <div class="ah">
            <h3>角色与权限</h3>
            <span class="tag tag-brand">超级管理员</span>
          </div>
          <table class="atable perm-table">
            <tbody>
              <tr v-for="p in permissions" :key="p.name">
                <td>{{ p.name }}</td>
                <td style="text-align: right">
                  <span class="sdot green">{{ p.access }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="acard">
          <div class="ah"><h3>最近登录记录</h3></div>
          <table class="atable">
            <thead>
              <tr>
                <th>登录时间</th>
                <th>IP 地址</th>
                <th>设备</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(log, i) in loginLogs" :key="i">
                <td class="dim">{{ log.time }}</td>
                <td class="mono" style="font-size: 12px">{{ log.ip }}</td>
                <td>{{ log.device }}</td>
                <td>
                  <span class="sdot" :class="log.ok ? 'green' : 'red'">{{
                    log.ok ? '成功' : '已拦截'
                  }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { ElMessage } from 'element-plus';

const profileForm = reactive({
  account: 'admin@haoji.com',
  name: '管理员',
  phone: '138****0000',
  dept: '平台运营部',
});
const pwdForm = reactive({ current: '', newPwd: '', confirm: '' });

const permissions = [
  { name: '数据看板', access: '可访问' },
  { name: '商品管理（增删改）', access: '全部权限' },
  { name: '订单管理（发货/退款）', access: '全部权限' },
  { name: '用户管理（禁用/启用）', access: '全部权限' },
  { name: '评价管理（删除）', access: '全部权限' },
  { name: '内容管理（轮播/公告）', access: '全部权限' },
  { name: '系统设置 / 管理员管理', access: '全部权限' },
];

const loginLogs = [
  { time: '2026-05-28 09:02', ip: '123.118.x.x', device: 'Chrome · macOS', ok: true },
  { time: '2026-05-27 18:40', ip: '123.118.x.x', device: 'Chrome · macOS', ok: true },
  { time: '2026-05-27 08:55', ip: '110.242.x.x', device: 'Safari · iPhone', ok: true },
  { time: '2026-05-26 22:10', ip: '58.246.x.x', device: '未知设备', ok: false },
];

function saveProfile() {
  ElMessage.success('信息已保存');
}

function savePwd() {
  if (!pwdForm.current || !pwdForm.newPwd || !pwdForm.confirm) {
    ElMessage.warning('请填写完整');
    return;
  }
  if (pwdForm.newPwd !== pwdForm.confirm) {
    ElMessage.error('两次输入不一致');
    return;
  }
  ElMessage.success('密码已修改');
  pwdForm.current = '';
  pwdForm.newPwd = '';
  pwdForm.confirm = '';
}
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
.sdot.red {
  color: var(--danger);
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
