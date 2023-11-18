<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../service/user";

const route=useRoute();
const router=useRouter();
console.log(route);
console.log(router);
console.log(route.query);

const onSubmit = async () => {


  //获取用户信息
  const currentUser = await getCurrentUser();

  if (!currentUser){
    Toast.fail('用户未登录')
    return;
  }

  const res = await  myAxios.post('/user/update',{
    'id': currentUser.id,
    [editUser.value.editKey]: editUser.value.currentValue
  })
  console.log(res,'更新请求')
  if (res.code === 200 && res.data >0){
    Toast.success('修改成功');
    router.back();
  } else {
    Toast.fail('修改错误');
  }

  // const res = await myAxios.post( '/user/update',{
  //   'id':7,
  //   [editUser.value.editKey as string]: editUser.value.currentValue
  // })
  // console.log(res,'更新请求')
  // if (res.code==0 && res.data){
  //   Toast.success('更新成功');
  //   router.back()
  // }
  // else {
  //   Toast.fail('更新失败');
  // }


};

const editUser  = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName,
})

console.log(route);
console.log(route.query);

</script>

<template>
  <van-form @submit="onSubmit">
    <van-field
        v-model="editUser.currentValue"
        :name="editUser.editKey"
        :label="editUser.editName"
        :placeholder="`请输入${editUser.editName}`"
    />
    <div style="margin: 18px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>

</template>

<style scoped>

</style>