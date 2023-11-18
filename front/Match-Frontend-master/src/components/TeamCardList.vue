<script setup lang="ts">

import {TeamType} from "../models/team";
import myAxios from "../plugins/myAxios";
import {Dialog, Toast} from "vant";
import {teamStatusEnum} from "../constants/team";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../service/user";
import {useRouter} from "vue-router";
import ikun from '../assets/ikun.png';
import TeamCardList from "./TeamCardList.vue";


const currentUser=ref();
const router=useRouter();
const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref(0);

// 获取用户信息
onMounted(async ()=>{
  currentUser.value=await getCurrentUser();
  console.log("TeamPage页面currentUser.value=await getCurrentUser();的值是"+currentUser.value)
  console.log("TeamCardList中props的值是"+props)
})


interface TeamCardListProps {
  teamList: TeamType[];
}

const props = withDefaults(defineProps<TeamCardListProps>(), {
  // @ts-ignore
  teamList: [] as TeamType[],
});

const VanDialog =Dialog.Component;


// 加入队伍
const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}



/**
 * 跳转至更新队伍页
 * @param id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/updateTeam',
    query: {
      id,
    }
  })
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
    location.reload();
  } else {
    Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}


// 删除
const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}


/**
 * 加入队伍
 */
const doJoinTeam = async () => {
  if (!joinTeamId.value) {
    return;
  }
  const res = await myAxios.post('/team/joinTeam', {
    teamId: joinTeamId.value,
    password: password.value
  });
  if (res?.code === 200) {
    Toast.success('加入成功');
    location.reload();
    doJoinCancel();
  } else {
    Toast.fail('加入失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 删除解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/deleteTeam', {
    id,
  });
  if (res?.code === 200) {
    Toast.success('操作成功');
    location.reload();
  } else {
    Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
  }


/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 200) {
    Toast.success('操作成功');
  } else {
    Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}



interface TeamCardListProps{
  teamList: TeamType[];
}
const props = withDefaults(defineProps<TeamCardListProps>(),{
  //@ts-ignore
  teamList: [] as TeamType[],
});

// 更新队伍
const doUpdateTeam=(id: number)=>{
  router.push({
    path:"/team/updateTeam",
    query:{
      id,
    }
  })
}

//队伍列表加入队伍
const doJoinTeam = async(id: number) =>{
  const res = await myAxios.post("/team/joinTeam",{
    teamId : id
  });
  if (res?.code === 200){
    Toast.success("加入成功")
  }else {
    Toast.fail("加入失败" + (res.description ? `， ${res.description} `:''));
  }
}}

</script>

<template>
  <div
      id="teamCardList"
  >
    <van-card
        v-for="team in props.teamList"
        :thumb="ikun"
        :desc="team.description"
        :title="`${team.name}`"
    >
      <template #tags>
        <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
          {{
            teamStatusEnum[team.status]
          }}
        </van-tag>
      </template>
      <template #bottom>
        <div>
          {{ `队伍人数: ${team.hasJoinNum}/${team.maxNum}` }}
        </div>
        <div v-if="team.expireTime">
          {{ '过期时间: ' + team.expireTime }}
        </div>
        <div>
          {{ '创建时间: ' + team.createTime }}
        </div>
      </template>
      <template #footer>
        <van-button size="small" type="primary" v-if="team.userId !== currentUser?.id && !team.hasJoin" plain
                    @click="preJoinTeam(team)">
          加入队伍
        </van-button>
        <van-button v-if="team.userId === currentUser?.id" size="small" color="blue" plain
                    @click="doUpdateTeam(team.id)">更新队伍
        </van-button>
        <!-- 仅加入队伍可见 -->
        <van-button v-if="team.userId !== currentUser?.id && team.hasJoin" size="small" plain
                    @click="doQuitTeam(team.id)">退出队伍
        </van-button>
        <van-button v-if="team.userId === currentUser?.id" size="small" type="danger" plain
                    @click="doDeleteTeam(team.id)">解散队伍
        </van-button>
      </template>
    </van-card>

  </div>
  <van-dialog v-model:show="showPasswordDialog" title="请输入密码" show-cancel-button @confirm="doJoinTeam"
              @cancel="doJoinCancel">
    <van-field v-model="password" placeholder="请输入密码"/>
  </van-dialog>
</template>

<style scoped>

</style>