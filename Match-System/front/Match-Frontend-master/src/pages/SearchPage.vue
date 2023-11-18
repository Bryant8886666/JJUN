<script setup lang="ts">

import {Toast} from "vant";
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";
const router = useRouter();





const originTagList = [
  {
    text: '性别',
    children: [
      { text: '男', id: '男' },
      { text: '女', id: '女' },
    ],
  },
  {
    text: '年级',
    children: [
      { text: '大一', id: '大一' },
      { text: '大二', id: '大二' },
    ],
  },
];

// 标签列表
let taglist=ref(originTagList);



const doSearchResult = () => {
  router.push({
    path: '/user/list',
    query: {
      tags: activeIds.value
    }
  })
}

/**
 * 搜索过滤
 * @param val
 */
const onSearch= (val) => {
  taglist.value=originTagList.map(parentTag =>{
const tempChildren =[...parentTag.children];
const tempParentTag ={...parentTag};
tempParentTag.children=tempChildren.filter(item=>item.text.includes(searchText.value));
return tempParentTag;
  });
};


//移除标签
const doClose = (tag) =>{
  activeIds.value=activeIds.value.filter(item =>{
    return item !== tag;
  })
}

//已选中的标签
const activeIds = ref([]);
const activeIndex = ref(0);





const searchText = ref('');
// const onSearch = (val) => Toast(val);
const onCancel = () => {
  searchText.value='';
  taglist.value=originTagList;
};
</script>

<template>
  <form action="/">
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入搜索关键词"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>
  <van-divider content-position="left">已选标签</van-divider>
  <div v-if="activeIds.length===0">请选择标签</div>
  <van-row gutter="16" style="padding: 0 16px">
    <van-col v-for="tag in activeIds">
      <van-tag closeable size="small" type="primary" @close="doClose(tag)">
        {{tag}}
      </van-tag>
    </van-col>
  </van-row>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="taglist"
  />
  <div style="padding: 16px">
    <van-button block type="primary" @click="doSearchResult">搜索</van-button>
  </div>
</template>

<style scoped>

</style>