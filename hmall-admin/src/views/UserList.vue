<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="users" style="margin-top: 16px"
      ><el-table-column prop="id" label="ID" width="80" /><el-table-column
        prop="username"
        label="用户名"
      /><el-table-column prop="phone" label="手机号" /><el-table-column
        prop="email"
        label="邮箱"
      /><el-table-column label="状态" width="100"
        ><template #default="{ row }"
          ><el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'">{{
            row.status
          }}</el-tag></template
        ></el-table-column
      ><el-table-column label="操作" width="120"
        ><template #default="{ row }"
          ><el-button
            size="small"
            :type="row.status === 'FROZEN' ? 'success' : 'warning'"
            @click="toggle(row)"
            >{{ row.status === 'FROZEN' ? '启用' : '禁用' }}</el-button
          ></template
        ></el-table-column
      ></el-table
    >
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { getUsers, toggleUserStatus } from '@/api/user';
import { ElMessage } from 'element-plus';
const users = ref([]);
async function fetch() {
  try {
    const r = await getUsers({});
    users.value = r.list || [];
  } catch (err) {
    console.error(err);
  }
}
async function toggle(row) {
  try {
    await toggleUserStatus(row.id);
    row.status = row.status === 'FROZEN' ? 'NORMAL' : 'FROZEN';
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}
fetch();
</script>
