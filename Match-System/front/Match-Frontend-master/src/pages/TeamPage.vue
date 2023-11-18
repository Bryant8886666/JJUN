<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import TeamCardList from "../components/TeamCardList.vue";

const router=useRouter();
const searchText = ref('');
const active = ref('public')
const teamList=ref([]);
onMounted(async ()=>{
  listTeam()
})

/**
 * 切换查询状态
 * @param name
 */
const onTabChange = (name) => {
  // 查公开
  if (name === 'public') {
    listTeam(searchText.value,  0);
  } else {
    router.push({
      path: "/team/my/create"
    })
    // 查加密
    // listTeam(searchText.value,  2);
  }
}

// 搜索按钮
const onSearch=(val:any)=>{
listTeam(val)
}

// 队伍搜索方法
const listTeam =async (val='', status = 0)=>{
  const res=await myAxios.get("/team/listTeam",{
    params: {
      searchText: val,
      pageNum: 1,
      status,
    }
  });
  if (res?.code==200){
    teamList.value=res.data;
    console.log("teamList.value=res.data的值是"+teamList.value)
  }
  else {
    Toast.fail('加载队伍失败');
  }
}

// 跳转到添加队伍页面
const toAddTeam=()=>{
  router.push({
    path: "/team/addTeam"
  })
}




</script>

<template>
<div id="teamPage">
  <van-search v-model="searchText" placeholder="搜索队伍" clearable="true" @search="onSearch"></van-search>
  <div style="margin-bottom: 16px" />
  <van-button class="add-button" type="primary" icon="plus" @click="toAddTeam"></van-button>
  <van-tabs v-model:active="active" @change="onTabChange">
    <van-tab title="公开" name="public" />
    <van-tab title="个人队伍" name="self" />
  </van-tabs>

  <TeamCardList :teamList ="teamList"/>
</div>
</template>

<style scoped>

</style>