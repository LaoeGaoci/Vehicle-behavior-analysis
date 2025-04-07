<script setup>

import {getAccount, getToken} from '@/router/index.js'
import {onMounted, ref} from 'vue'
import axios from 'axios'
import {useRouter} from 'vue-router'
import {md5} from "js-md5";

const router = useRouter()
let account = getAccount()
let password = ref('')
let phone = ref()
let email = ref()

// let layoutPassword = ref()

let tempAccount = getAccount()
let tempPassword = ref('')
let tempPhone = ref()
let tempEmail = ref()

let qrPassword = ref()

const tokenData = getToken()

const refuse = () => {
  zhuxiao.value = false
}

const continueTo = () => {
  continueto()
  zhuxiao.value = false
}

//注销
const continueto = async () => {
  try {
    const response = await axios.delete(
        'http://localhost:8080/user/logout',
        {
          params:{
            account: account,
            password: md5(qrPassword.value),
          },
          headers: {
            'Content-Type': 'application/json',
            Authorization: tokenData
          }
        }
    )
    console.log(response)
    if (response.status === 204) {
      console.log('注销成功')
      router.push('/auth/login')
    } else {
      console.log('密码错误')
    }
  } catch (error) {
    console.log('密码错误')
    console.error(error)
  }
}

//加载
onMounted(async () => {
  try {
    const response = await axios.get(
        `http://localhost:8080/user/info?account=${encodeURIComponent(account)}`,
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: tokenData
          }
        }
    )
    console.log(response.data.password)
    account = response.data.account
    password.value = response.data.password
    phone.value = response.data.phone
    email.value = response.data.email
  } catch (error) {
    console.error(error)
  }

})
//修改用户数据
const modifyUser = async () => {
  try {
    const response = await axios.put(
        'http://localhost:8080/user/modifyInfo',
        {
          account: account,
          password: md5(password.value),
          phone: phone.value,
          email: email.value,
          new_account: account
        }, {
          headers: {
            'Content-Type': 'application/json',
            Authorization: tokenData
          }
        })
    if (response.status === 200) {
      console.log('注册请求成功')
    } else if (response.status === 406) {
      alert('用户名重复')
    } else if (response.status === 411) {
      console.log('邮箱重复')
    } else if (response.status === 412) {
      console.log('电话重复')
    } else {
      console.log('未知错误')
    }
  } catch (error) {
    console.error(error)
  }
}
const editDialogVisible = ref(false)
// const editedUser = ref({})
const openEditDialog = () => {
  tempAccount = JSON.parse(JSON.stringify(account))
  tempEmail.value = JSON.parse(JSON.stringify(email.value))
  tempPassword.value = JSON.parse(JSON.stringify(password.value))
  tempPhone.value = JSON.parse(JSON.stringify(phone.value))

  editDialogVisible.value = true
}

let zhuxiao = ref(false)

const logout = () => {
  zhuxiao.value = true
}

const hideEditDialog = () => {
  editDialogVisible.value = false
  // editedUser.value = {}
}

const saveUser = () => {
  console.log(tempAccount)
  console.log(tempEmail.value)
  account = JSON.parse(JSON.stringify(tempAccount))
  email.value = JSON.parse(JSON.stringify(tempEmail.value))
  password.value = JSON.parse(JSON.stringify(tempPassword.value))
  phone.value = JSON.parse(JSON.stringify(tempPhone.value))
  modifyUser()
  hideEditDialog()
}
</script>

<template>
  <main>
    <div style="width: 100%; height: 100%">
      <!-- 左侧 -->
      <div class="">
        <div class="border-round border-1 surface-border p-4 surface-card">
          <div class="flex mb-3">
            <div class="flex align-items-center gap-2">
              <Avatar
                  image="../../../public/demo/images/login/amyelsner.png"
                  size="large"
                  shape="circle"
              />
              <span class="font-bold">Amy Elsner</span>
            </div>
          </div>

          <!-- 显示数据 -->
          <div class="manger_Data">
            <!-- 账号 -->
            <div class="card card1 justify-content-between">
              <div style="display: flex">
                <div class="manger_Property">账号ID:</div>
                <div style="margin-left: 10px">{{ account }}</div>
              </div>
              <!-- <Button label="编辑" link /> -->
            </div>
            <!-- 密码 -->
            <div class="card card1 justify-content-between">
              <div style="display: flex">
                <div class="manger_Property">密码:</div>
                <p>{{ password }}</p>
              </div>
              <!-- <Button label="编辑" link /> -->
            </div>
            <!-- 手机号 -->
            <div class="card card1 justify-content-between">
              <div style="display: flex">
                <div class="manger_Property">手机号:</div>
                <div>{{ phone }}</div>
              </div>
              <!-- <Button label="编辑" link /> -->
            </div>
            <!-- 邮箱 -->
            <div class="card card1 justify-content-between">
              <div style="display: flex">
                <div class="manger_Property">邮箱:</div>
                <div>{{ email }}</div>
              </div>
              <!-- <Button label="编辑" link /> -->
            </div>
            <!-- 编辑按钮 -->
            <Dialog
                :closable="false"
                :visible="editDialogVisible"
                header="编辑框"
                :modal="true"
            >
              <div class="p-fluid">
                <InputText v-model="tempAccount" placeholder="账号" disabled/>
                <InputText v-model="tempPassword" placeholder="密码"/>
                <InputText v-model="tempPhone" placeholder="手机号"/>
                <InputText v-model="tempEmail" placeholder="邮箱"/>
              </div>
              <template #footer>
                <Button label="取消" icon="pi pi-times" @click="hideEditDialog"/>
                <Button label="保存" icon="pi pi-check" @click="saveUser"/>
              </template>
            </Dialog>
            <!-- 注销按钮 -->
            <Dialog
                v-model:visible="zhuxiao"
                :style="{ width: '450px' }"
                header="您是否要注销账号"
                :modal="true"
            >
              <InputText v-model="qrPassword" placeholder="密码"/>
              <template #footer>
                <Button label="取消" icon="pi pi-times" text @click="refuse"/>
                <Button label="注销" icon="pi pi-check" text @click="continueTo"/>
              </template>
            </Dialog>
            <!--按钮-->
            <div class="flex justify-content-between mt-3">
              <Button label="编辑" @click="openEditDialog"/>
              <Button label="注销" @click="logout"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.card1 {
  flex-direction: row;
  display: flex;
  border: 0px;
  border-radius: 0%;
  flex: 1;
  height: 100px;
}

.manger_Data {
  display: flex;
  flex-direction: column;
}

.manger_Property {
  width: 100px;
}
</style>
