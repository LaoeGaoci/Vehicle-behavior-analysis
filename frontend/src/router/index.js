import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'
import Cookies from 'js-cookie'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '/',
          name: 'dashboard',
          component: () => import('@/components/Map.vue')
        },
      ]
    },
  ]
})

const TokenKey = 'token'
const AccountKey = 'account'

//获取token
export function getToken() {
  console.log(Cookies.get(TokenKey))
  return Cookies.get(TokenKey)
}
//设置token
export function setToken(token) {

  const expirationTime = new Date() // 获取当前时间
  expirationTime.setTime(expirationTime.getTime() + 60 * 60 * 1000 * 12) // 在当前时间的基础上增加12小时
  return Cookies.set(TokenKey, token,{ expires: expirationTime })
}
//删除token
export function removeToken() {
  return Cookies.remove(TokenKey)
}

//获取账号名
export function getAccount() {
  return Cookies.get(AccountKey)
}
//设置账号名
export function setAccount(account) {
  const expirationTime = new Date() // 获取当前时间
  expirationTime.setTime(expirationTime.getTime() + 60 * 60 * 1000 * 12) // 在当前时间的基础上增加12小时
  return Cookies.set(AccountKey, account,{ expires: expirationTime })
}
//移除账号
export function removeAccount() {
  return Cookies.remove(AccountKey)
}

export default router
