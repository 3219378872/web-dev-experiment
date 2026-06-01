<template>
  <div>
    <h2>收货地址</h2>
    <el-button type="primary" @click="dialogVisible = true">新增地址</el-button>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col v-for="addr in addresses" :key="addr.id" :span="8">
        <el-card
          ><p>
            <strong>{{ addr.receiverName }}</strong> {{ addr.phone }}
          </p>
          <p>{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
          <el-tag v-if="addr.isDefault" size="small" type="danger">默认</el-tag>
          <div style="margin-top: 8px">
            <el-button size="small" @click="setDefault(addr.id)">设为默认</el-button
            ><el-button size="small" @click="deleteAddr(addr.id)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog v-model="dialogVisible" title="新增地址"
      ><el-form :model="form" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="省市区"
          ><el-input v-model="form.province" style="width: 100px" /><el-input
            v-model="form.city"
            style="width: 100px; margin-left: 8px" /><el-input
            v-model="form.district"
            style="width: 100px; margin-left: 8px"
        /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detail" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saveAddr">保存</el-button></el-form-item>
      </el-form></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getAddresses, saveAddress, deleteAddress, setDefaultAddress } from '@/api/address';
import { ElMessage } from 'element-plus';
const addresses = ref([]);
const dialogVisible = ref(false);
const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
});
async function fetch() {
  try {
    addresses.value = await getAddresses();
  } catch (err) {
    console.error(err);
  }
}
async function saveAddr() {
  try {
    await saveAddress(form);
    dialogVisible.value = false;
    fetch();
    ElMessage.success('已保存');
  } catch (err) {
    console.error(err);
  }
}
async function deleteAddr(id) {
  try {
    await deleteAddress(id);
    fetch();
  } catch (err) {
    console.error(err);
  }
}
async function setDefault(id) {
  try {
    await setDefaultAddress(id);
    fetch();
  } catch (err) {
    console.error(err);
  }
}
onMounted(fetch);
</script>
