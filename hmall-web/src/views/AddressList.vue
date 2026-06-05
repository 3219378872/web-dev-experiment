<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span>个人中心<span class="s">/</span
      ><b style="color: var(--ink)">收货地址</b>
    </div>

    <div class="acct-layout">
      <AccountSidebar active="addresses" />

      <main>
        <div class="panel">
          <div class="panel-head">
            <h3>
              收货地址管理
              <span class="dim" style="font-weight: 400; font-size: 13px"
                >最多可保存 20 个地址</span
              >
            </h3>
            <button class="btn btn-primary btn-sm" @click="showForm = true">＋ 新增收货地址</button>
          </div>
          <div class="panel-pad">
            <!-- 新增/编辑表单 -->
            <div v-if="showForm" class="addr-form">
              <h4>{{ editingId ? '编辑收货地址' : '＋ 新增收货地址' }}</h4>
              <div class="ff-grid">
                <div class="field">
                  <label>收货人</label>
                  <input v-model="form.receiverName" class="input" placeholder="请输入收货人姓名" />
                </div>
                <div class="field">
                  <label>手机号码</label>
                  <input v-model="form.phone" class="input" placeholder="请输入手机号" />
                </div>
                <div class="field full">
                  <label>所在地区</label>
                  <div style="display: flex; gap: 10px">
                    <input v-model="form.province" class="input" placeholder="省" style="flex: 1" />
                    <input v-model="form.city" class="input" placeholder="市" style="flex: 1" />
                    <input v-model="form.district" class="input" placeholder="区" style="flex: 1" />
                  </div>
                </div>
                <div class="field full">
                  <label>详细地址</label>
                  <textarea
                    v-model="form.detail"
                    class="input"
                    rows="2"
                    placeholder="街道、门牌号、楼栋、房间号等"
                  ></textarea>
                </div>
              </div>
              <label
                style="
                  display: flex;
                  gap: 8px;
                  align-items: center;
                  font-size: 13px;
                  color: var(--ink-2);
                  margin: 16px 0;
                "
              >
                <span
                  class="checkbox"
                  :class="{ on: form.isDefault }"
                  @click="form.isDefault = !form.isDefault"
                  >{{ form.isDefault ? '✓' : '' }}</span
                >
                设为默认收货地址
              </label>
              <div style="display: flex; gap: 10px">
                <button class="btn btn-primary" @click="saveAddr">保存地址</button>
                <button class="btn btn-ghost" @click="cancelForm">取消</button>
              </div>
            </div>

            <!-- 地址列表 -->
            <div class="addr-grid">
              <div
                v-for="addr in addresses"
                :key="addr.id"
                class="ad"
                :class="{ def: addr.isDefault }"
              >
                <div class="who">
                  {{ addr.receiverName }} <span class="tel">{{ maskPhone(addr.phone) }}</span>
                </div>
                <div class="det">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
                </div>
                <div class="tags">
                  <span v-if="addr.isDefault" class="tag tag-brand">默认</span>
                </div>
                <div class="acts">
                  <a v-if="!addr.isDefault" class="set" @click="setDefault(addr.id)">设为默认</a>
                  <a v-else class="set">默认地址</a>
                  <a @click="editAddr(addr)">编辑</a>
                  <a @click="deleteAddr(addr.id)">删除</a>
                </div>
              </div>
              <div class="ad add" @click="showForm = true">
                <div style="text-align: center">
                  <div style="font-size: 30px">＋</div>
                  新增收货地址
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
  getAddresses,
  saveAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
} from '@/api/address';
import { ElMessage, ElMessageBox } from 'element-plus';
import AccountSidebar from '@/components/AccountSidebar.vue';

const addresses = ref([]);
const showForm = ref(false);
const editingId = ref(null);
const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
});

async function fetch() {
  try {
    addresses.value = await getAddresses();
  } catch (err) {
    console.error(err);
  }
}

function resetForm() {
  form.receiverName = '';
  form.phone = '';
  form.province = '';
  form.city = '';
  form.district = '';
  form.detail = '';
  form.isDefault = false;
  editingId.value = null;
}

function cancelForm() {
  resetForm();
  showForm.value = false;
}

function editAddr(addr) {
  editingId.value = addr.id;
  form.receiverName = addr.receiverName;
  form.phone = addr.phone;
  form.province = addr.province;
  form.city = addr.city;
  form.district = addr.district;
  form.detail = addr.detail;
  form.isDefault = addr.isDefault;
  showForm.value = true;
}

async function saveAddr() {
  try {
    if (editingId.value) {
      await updateAddress(editingId.value, form);
    } else {
      await saveAddress(form);
    }
    showForm.value = false;
    resetForm();
    fetch();
    ElMessage.success('已保存');
  } catch (err) {
    console.error(err);
  }
}

async function deleteAddr(id) {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteAddress(id);
    fetch();
  } catch (err) {
    if (err !== 'cancel') console.error(err);
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

function maskPhone(phone) {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

onMounted(fetch);
</script>

<style scoped>
.addr-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.ad {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 18px 20px;
  position: relative;
  background: #fff;
}
.ad.def {
  border-color: var(--brand);
}
.ad .who {
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.ad .who .tel {
  color: var(--ink-2);
  font-weight: 400;
  font-size: 13.5px;
}
.ad .det {
  font-size: 13px;
  color: var(--ink-2);
  margin-top: 10px;
  line-height: 1.6;
}
.ad .tags {
  margin-top: 12px;
  display: flex;
  gap: 6px;
}
.ad .acts {
  display: flex;
  gap: 16px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--bg);
  font-size: 12.5px;
}
.ad .acts a {
  color: var(--ink-3);
  cursor: pointer;
}
.ad .acts a:hover {
  color: var(--brand);
}
.ad .acts .set {
  color: var(--brand);
}
.ad.add {
  border-style: dashed;
  display: grid;
  place-items: center;
  color: var(--ink-3);
  cursor: pointer;
  min-height: 170px;
}
.ad.add:hover {
  border-color: var(--brand);
  color: var(--brand);
}

.addr-form {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 22px;
  background: var(--bg);
  margin-bottom: 18px;
}
.addr-form h4 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 16px;
}
.ff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.ff-grid .full {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .addr-grid {
    grid-template-columns: 1fr;
  }
  .ff-grid {
    grid-template-columns: 1fr;
  }
}
</style>
