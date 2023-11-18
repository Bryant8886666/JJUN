<script setup lang="ts">
import {useRoute} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import qs from "qs";
import axios from "axios";

const route = useRoute();
const {tags} = route.query;

const userList = ref([]);




onMounted( async () =>{
  // 为给定 ID 的 user 创建请求
  const userListData = await  myAxios.get('/user/searchByTags',{
    withCredentials: false,
    params: {
      tagNameList: tags
    },
    //序列化
    paramsSerializer: params => {
      return qs.stringify(params,{indices:false})
    }
  })
      .then(function (response) {
        console.log('/user/searchByTags succeed',response);
        Toast.success('请求成功');
        return response?.data;
      })
      .catch(function (error) {
        console.log('/user/search/tags error',error);
        Toast.fail('请求失败');
      });
  if (userListData){
    userListData.forEach(user =>{
      if (user.tags){
        user.tags = JSON.parse(user.tags);
      }
    })
    userList.value = userListData;
  }
})
</script>

<template>
  <!--      :title="user.title"-->
  <van-card
      v-for="user in userList"
      :desc="user.tags"
      :title="`${user.username} `"
      :thumb="user.avatarUrl"
  >
    <template #tags>
      <van-tag plain type="danger" v-for="tag in tags" style="margin-right: 8px; margin-top: 8px" >
        {{tag}}
      </van-tag>
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
  <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空" />
</template>

<style scoped>

</style>