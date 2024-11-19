<template>
  <div class="postList">
    <p>图片列表</p>
    <a-list item-layout="horizontal" :data-source="pictureList">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta :description="item.content">
            <template #title>
              {{ item.title }}
            </template>
            <template #avatar>
              <a-avatar :src="item.url" />
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { withDefaults, defineProps } from "vue";

interface Props {
  pictureList: any[];
}

const props = withDefaults(defineProps<Props>(), {
  pictureList: () => [],
});
</script>

<style scoped>
.gege {
  width: 100px;
}
</style>
