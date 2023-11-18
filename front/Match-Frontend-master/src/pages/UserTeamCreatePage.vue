<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import TeamCardList from "../components/TeamCardList.vue";
import {getCurrentUser} from "../service/user";

const router = useRouter();
const searchText = ref('');



/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/deleteTeam', {
    id,
  });
  if (res?.code === 200) {
    Toast.success('操作成功');
  } else {
    Toast.fail('操作失败' + (res.description ? `, ${res.description}` : ''));
  }
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quitTeam', {
    teamId: id
  });
  if (res?.code === 200) {
    Toast.success('操作成功');
  } else {
    Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}


// 跳转到加入队伍页
const doAddTeam = () => {
  router.push({
    path: "/team/add"
  })
}

const teamList = ref([]);

/**
 * 搜索自己创建的队伍
 * @param val
 * @returns {Promise<void>}
 */
const listMyTeam = async (val = '') => {
  const res = await myAxios.get("/team/list/my/createTeam", {
    params: {
      searchText: val,
      pageNum: 1,
    },
  });
  if (res?.code === 200) {
    teamList.value = res.data;
    console.log("UserTeamCreatePage中 teamList.value = res.data;的值为"+teamList.value);
  } else {
    Toast.fail('加载队伍失败，请刷新重试');
  }
}


// 页面加载时只触发一次
onMounted( () => {
  listMyTeam();
  console.log("aiosudjioasjdoiasjdioasjdaiosjdiojaoijdoiasjdoi")
})

const onSearch = (val:any) => {
  listMyTeam(val);
};

</script>

<template>
  <div id="teamPage">
    <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch" />
    <van-button type="primary" @click="doAddTeam">创建队伍</van-button>
    <TeamCardList :teamList="teamList" />
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>

<style scoped>

</style>