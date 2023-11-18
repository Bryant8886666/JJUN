<script setup lang="ts">


import {useRoute, useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../service/user";

const route=useRoute();
const router=useRouter();


const onSubmit = (values) => {
  //todo 把editKey currentValue editName提交到后台
  console.log('onSubmit',values);
}


const user =ref();

onMounted(async () => {
  // 获取用户信息
  if(user.value = await getCurrentUser()){
  };
//   const res = await getCurrentUser();
//   if (res){
//     console.log('res的值为'+res)
//     console.log('res.data的值为'+res.data)
//     user.value = res;
//     Toast.success('获取用户信息成功');
//   } else {
//     console.log("当前res的值是"+res)
//     Toast.fail('获取用户信息失败')
//   }
// }
})

const toEdit = (editKey: string,editName: string,currentValue: string) =>{
  router.push({
    path:'/edit',
    query: {
      editKey,
      editName,
      currentValue,
    }
  })
}

</script>

<template>
  <template v-if="user">
    <van-cell title="昵称" is-link to="/edit" :value="user.username" @click="toEdit('username','昵称',user.username)"/>
    <van-cell title="账户" :value="user.userAccount" />
    <van-cell title="头像" is-link to="/edit" >
      <img style="height: 48px" :src="user.avatarUrl">
    </van-cell>
    <van-cell title="我创建的队伍" is-link to="/team/my/create" />
    <van-cell title="我加入的队伍" is-link to="/team/my/join" />
  </template>

</template>

<style scoped>

</style>