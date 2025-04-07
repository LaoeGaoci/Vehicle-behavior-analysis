<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';
import AppFooter from './AppFooter.vue';

const model = ref([
  {
    label: 'Home',
    items: [{ label: '主界面', icon: 'pi pi-fw pi-home', to: '/' }]
  },
  {
    label: 'Pages',
    icon: 'pi pi-fw pi-briefcase',
    to: '/pages',
    items: [
      {
        label: '用户',
        icon: 'pi pi-fw pi-user',
        items: [
          {
            label: '历史记录',
            icon: 'pi pi-fw pi-star',
            to: '/auth/collections'
          },
          {
            label: '个人信息',
            icon: 'pi pi-fw pi-home',
            to: '/auth/usermanger'
          },

        ]
      },
    ]
  },
]);
</script>

<template>
  <ul class="layout-menu">
    <template v-for="(item, i) in model" :key="item">
      <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
      <li v-if="item.separator" class="menu-separator"></li>
    </template>
  </ul>
  <div class = "footer">
    <app-footer></app-footer>
  </div>

</template>

<style lang="scss" scoped></style>
