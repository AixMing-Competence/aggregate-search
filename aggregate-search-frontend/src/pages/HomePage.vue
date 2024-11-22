<template>
  <div class="homePage">
    <a-input-search
      v-model:value="searchText"
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
import { message } from "ant-design-vue";
import { SEARCH_TYPE_ENUM } from "@/constants/search";

const router = useRouter();

const route = useRoute();

const activeKey = route.params.category;

/**
 * 初始化参数（不可修改）
 */
const initSearchParams = {
  type: activeKey,
  current: 1,
  pageSize: 20,
};

const searchText = ref(route.query.searchText);

/**
 * 搜索参数
 */
const searchParams = ref({
  ...initSearchParams,
  searchText: searchText.value,
});

// 进入页面时会触发一次加载数据参数

const postList = ref([]);

const userList = ref([]);

const pictureList = ref([]);

/**
 * 加载数据
 */
const loadData = () => {
  const type = searchParams.value.type;
  if (!type) {
    message.error("类别为空");
  }
  request.post("/search/all", searchParams.value).then((res: any) => {
    if (type === "post") {
      postList.value = res.dataList ?? [];
    } else if (type === "user") {
      userList.value = res.dataList ?? [];
    } else if (type === "picture") {
      pictureList.value = res.dataList ?? [];
    }
    console.log(res);
  });
};

watchEffect(() => {
  loadData();
});

/**
 * 点击搜索按钮时触发
 */
const onSearch = () => {
  // 修改url后面的参数
  router.push({
    query: {
      ...searchParams.value,
      searchText: searchText.value,
    },
  });
  searchParams.value = {
    ...searchParams.value,
    searchText: searchText.value,
  };
};

/**
 * 切换标签栏时触发
 * @param activeKey
 */
const onTabChange = (activeKey: string) => {
  // 修改 searchParams 的值
  searchParams.value.type = activeKey;
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
