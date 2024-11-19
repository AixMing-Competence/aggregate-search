<template>
  <div class="homePage">
    <a-input-search
      v-model:value="searchParams.searchText"
      placeholder="input search text"
      enter-button="Search"
      size="large"
      @search="onSearch"
    />
    <MyDivider />
    <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
      <a-tab-pane key="post" tab="文章">
        <PostList :postList="postList" />
      </a-tab-pane>
      <a-tab-pane key="picture" tab="图片" force-render>
        <PictureList :pictureList="pictureList" />
      </a-tab-pane>
      <a-tab-pane key="user" tab="用户">
        <UserList :user-list="userList" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import PostList from "@/components/PostList.vue";
import PictureList from "@/components/PictureList.vue";
import UserList from "@/components/UserList.vue";
import MyDivider from "@/components/MyDivider.vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/plugins/request";

const router = useRouter();

const route = useRoute();

const activeKey = route.params.category;

/**
 * 初始化参数（不可修改）
 */
const initSearchParams = {
  searchText: "",
  current: 1,
  pageSize: 20,
};

/**
 * 搜索参数
 */
const searchParams = ref({
  ...initSearchParams,
  searchText: route.query.searchText,
});

const postList = ref([]);

const userList = ref([]);

const pictureList = ref([]);

/**
 * 加载数据
 */
const loadData = () => {
  request.post("/search/all/fast", searchParams.value).then((res: any) => {
    postList.value = res.postList;
    userList.value = res.userList;
    pictureList.value = res.pictureList ?? [];
  });
};

onMounted(() => {
  loadData();
});

/**
 * 搜索时触发
 */
const onSearch = () => {
  // 修改url后面的参数
  router.push({
    query: searchParams.value,
  });
  loadData();
};

const onTabChange = (activeKey: string) => {
  // 修改url
  router.push({
    path: `/${activeKey}`,
    query: searchParams.value,
  });
};
</script>

<style>
.homePage {
}
</style>
