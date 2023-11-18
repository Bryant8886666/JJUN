<script setup lang="ts">
import {useRoute} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import qs from "qs";
import axios from "axios";
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const {tags} = route.query;

const userList = ref([]);




onMounted( async () =>{
  // 为给定 ID 的 user 创建请求
  const userListData = await myAxios.get('/user/recommend',{
    withCredentials: true,
    params: {
      pageSize: 8,
      pageNum: 1,
    },
    //序列化
    paramsSerializer: params => {
      return qs.stringify(params,{indices:false})
    }
  })
      .then(function (response) {
        console.log('/succeed',response);
        Toast.success('请求成功');
        return response?.data.records;
      })
      .catch(function (error) {
        console.log('/user/search/tags error',error);
        Toast.fail('请求失败');
      });
  if (userListData){
    userListData.forEach(user =>{
      console.log("现在遍历数据")
      if (user.tags){
        console.log("拿到user.tags数据了")
        user.tags = JSON.parse(user.tags);
        console.log("user.tags数据是"+user.tags)
      }
    })
    userList.value = userListData;
  }
})
</script>

<template>
  <!--      :title="user.title"-->
<user-card-list :user-list="userList"/>
  <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空" />
</template>

<style scoped>

</style>