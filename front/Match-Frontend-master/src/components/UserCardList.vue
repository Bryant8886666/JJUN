
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

import {UserType} from "../models/user";
import UserCardList from "./UserCardList.vue";
interface UserCardListProps{
  loading:boolean;
  userList: UserType[];
}

const props =withDefaults(defineProps<UserCardListProps>(),{
  loading: true,
  userList: [] as UserType[],
});




</script>

<template>
  <!--      :title="user.title"-->
  <van-skeleton title avatar :row="3" :loading="props.loading" v-for="user in props.userList">
  <van-card
      v-for="user in props.userList"
      :desc="user.tags"
      :title="`${user.username} `"
      :thumb="user.avatarUrl"
  >
    <template #tags>
      <van-tag plain type="danger" v-for="tag in user.tags" style="margin-right: 8px; margin-top: 8px" >
        {{ tag }}
      </van-tag>
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
  </van-skeleton>
</template>

<style scoped>

</style>