<template>
  <div class="userList">
    <p>用户列表</p>
    <a-list item-layout="horizontal" :data-source="userList">
      <template #renderItem="{ item }">
        <a-card style="margin-bottom: 16px">
          <a-card-meta :title="item.userName" :description="item.userProfile">
            <template #avatar>
              <a-avatar :src="item.userAvatar" />
            </template>
          </a-card-meta>
        </a-card>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { withDefaults, defineProps } from "vue";

interface Props {
  userList: any[];
}

const props = withDefaults(defineProps<Props>(), {
  userList: () => [],
});
</script>

<style scoped></style>
