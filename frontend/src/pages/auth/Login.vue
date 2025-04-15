<template>
  <Toast position="top-right" group="tr" />
  <div class="container" style="display: flex; position: absolute; flex-direction: row">
    <img :src="logoUrl" alt="Sakai logo" class="icon" />
  </div>
  <div class="surface-ground flex align-items-center justify-content-center min-h-screen min-w-screen overflow-hidden">
    <div class="flex flex-column align-items-center justify-content-center">
      <div
        style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%);"
      >
        <div class="w-full surface-card py-8 px-5 sm:px-8" style="border-radius: 53px">
          <div class="text-center mb-5">
            <img src="/demo/images/login/amyelsner.png" alt="Image" height="50" class="mb-3" />
          </div>
          <div class="w-full surface-card py-8 px-5 sm:px-8" style="border-radius: 53px">
            <div>
              <label for="accountName" class="block text-900 text-xl font-medium mb-2">账户名称</label>
              <InputText
                id="accountName"
                type="text"
                placeholder="请输入账户名称"
                class="w-full md:w-30rem mb-5"
                style="padding: 1rem"
                v-model="accountName"
              />

              <div class="grid">
                <div class="col-6">
                  <label for="age" class="block text-900 font-medium text-xl mb-2">年龄</label>
                  <InputNumber
                    id="age"
                    v-model="age"
                    placeholder="请输入年龄"
                    class="w-full mb-5"
                    inputClass="w-full"
                    :inputStyle="{ padding: '1rem' }"
                  />
                </div>
                <div class="col-6">
                  <label for="drivingExperience" class="block text-900 font-medium text-xl mb-2">驾龄(年)</label>
                  <InputNumber
                    id="drivingExperience"
                    v-model="drivingExperience"
                    placeholder="请输入驾龄"
                    class="w-full mb-5"
                    inputClass="w-full"
                    :inputStyle="{ padding: '1rem' }"
                  />
                </div>
              </div>

              <div class="gender">
                <label for="gender" class="block text-900 font-medium text-xl mb-2">性别</label>
                <Dropdown
                  id="gender"
                  v-model="gender"
                  :options="['男', '女']"
                  placeholder="请选择性别"
                  class="w-full mb-5"
                  :inputStyle="{ padding: '1rem' }"
                />
              </div>

              <div class="flex-container" style="width: 100%; margin-top: 30px">
                <div class="item1">
                  <Button
                    @click="register"
                    label="确认"
                    style="width: 100%"
                    class="p-3 text-xl"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <AppConfig simple />
</template>

<script setup>
import {ref, onMounted, computed} from 'vue';
import { useStore } from 'vuex';
import { useToast } from 'primevue/usetoast';
import axios from 'axios';
import { useRouter } from 'vue-router';
const logoUrl = computed(() => {
  return "/layout/images/logo-white.png"
})
const store = useStore();
const toast = useToast();
const router = useRouter();

// 注册表单数据
const accountName = ref('');
const age = ref(null);
const drivingExperience = ref(null);
const gender = ref('');

// 注册函数
const register = async () => {
  console.log(accountName.value + " " + age.value + " " + drivingExperience.value + " " + gender.value);
  if (!accountName.value || age.value === null || drivingExperience.value === null || !gender.value) {
    toast.add({ severity: 'warn', summary: 'Warn Message', detail: '请填写完整的注册信息', group: 'tr', life: 3000 });
    return;
  }
  try {
    const response = await axios.post('http://localhost:8080/user', {
      username: accountName.value,
      age: age.value,
      driver_years: drivingExperience.value,
      gender: gender.value,
    });
    if (response.status === 200) {
      // 更新 Vuex 中的账户名称
      store.dispatch('updateAccountName', accountName.value);
      // 将 accountName 保存在 localStorage 中
      localStorage.setItem('username', accountName.value);

      toast.add({ severity: 'success', summary: '注册成功', group: 'tr', life: 3000 });
      router.push('/');
    } else {
      alert('注册失败，请重试');
    }
  } catch (error) {
    console.error('注册出错：', error);
    toast.add({ severity: 'error', summary: '注册出错', detail: error, group: 'tr', life: 3000 });
  }
};
</script>

<style scoped>
.flex-container {
  display: flex;
}
.item1 {
  flex: 1;
}
.grid {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}
</style>
