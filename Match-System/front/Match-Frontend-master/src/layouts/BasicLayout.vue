<script setup lang="ts">
import { Toast} from "vant";
import {useRouter} from "vue-router";
const router =useRouter();

const DEAFAULT_TITLE="匹配系统";
const title=ref(DEAFAULT_TITLE);
const onClickLeft = () => {
  router.back();
};
const onClickRight = () => {
  router.push('/search')
};

/**
 * 根据路由切换标题
 */
router.beforeEach((to, from) => {
  const toPath = to.path;
  const route = routes.find((route) => {
    return toPath == route.path;
  })
  title.value = "匹配系统";
})

import { ref } from 'vue';
import index from "../pages/index.vue";
import team from "../pages/TeamPage.vue";
import routes from "../config/route";

// import {showToast} from 'vant';
// const active = ref("index");
// const onChange = (index) => Toast(` ${index}`);


</script>

<template>
  <van-nav-bar
      title="title"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>
  <div id="content">
    <router-view />
  </div>

  <van-tabbar route @change="onChange">
    <van-tabbar-item  to="/" icon="home-o" name="主页">主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="team" name="队伍">队伍</van-tabbar-item>
    <van-tabbar-item  to="/user" icon="search" name="个人">个人</van-tabbar-item>
  </van-tabbar>
</template>

<style scoped>

</style>