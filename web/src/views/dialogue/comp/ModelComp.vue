<template>
  <div class="model_wrapper" :class="{ showUpload: props.showUpload }" :style="{
    backgroundColor: modelInfo.showModelSelect ? 'var(--tp_d_filelist_bgColor)' : 'transparent'
  }">
    <n-popover
      placement="top-start"
      :show="showModelList"
      :style="{
        padding: '4px',
        'border-radius': '0px',
        'background-color': 'var(--tp_userList_bgcolor)',
      }"
    >
      <template #trigger>
        <div>
          <div
            class="model_text flex_sx_center"
            v-if="modelInfo && modelInfo.friendType === 1"
            @click.stop="modelClick"
            :style="{ 
              cursor: modelInfo.showModelSelect ? 'pointer' : 'not-allowed'
            }"
          >
            <img
              v-if="modelInfo.icon && modelInfo.showModelSelect"
              class="model_icon"
              :src="modelInfo.icon"
            />
            <span>{{ modelInfo.model }}</span>
          </div>
        </div>
      </template>
      <div class="list_wrapper">
        <ModelList v-model:show="showModelList" />
      </div>
    </n-popover>
  </div>
</template>

<script setup>
import { NPopover } from "naive-ui";
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useChatStore, useLoginStore } from "@/store";
import ModelList from "./ModelList.vue";

const useChat = useChatStore();
const useLogin = useLoginStore();
const showModelList = ref(false);
const props = defineProps(["showUpload"]);
const curr = computed(() => useChat.currFriendInfo);
const modelClick = () => {
  if (!modelInfo.value.showModelSelect) return;
  showModelList.value = !showModelList.value;
};

const modelInfo = computed(() => {
  if (useLogin.user) {
    console.log(`current: ${JSON.stringify(curr.value)}`)
    const modelName = curr.value.fixedModel || useLogin.user.model;
    return { 
      model: modelName, 
      icon: useLogin.user.modelIcon, 
      showModelSelect: curr.value.showModelSelect,
      friendType: curr.value.friendType
     };
  }
  // 返回默认值，确保即使在 useLogin.user 不存在时也有有效的对象
  return { model: '', icon: '', showModelSelect: false };
});
const bodyClick = () => {
  showModelList.value = false;
};
onMounted(() => {
  window.addEventListener("click", bodyClick);
});
onUnmounted(() => {
  window.removeEventListener("click", bodyClick);
});
</script>

<style lang="scss" scoped>
.model_wrapper {
  position: absolute;
  bottom: 15px;
  left: 5px;
  padding: 0 3px;
  border-radius: 5px;
  background-color: var(--tp_d_filelist_bgColor);
  &.showUpload {
    left: 30px;
  }
  .model_text {
    /* color: #7a8084; //#C5C9CC */
    color: var(--tp_d_filelist_color);
    font-size: var(--tp_size);
    .model_icon {
      width: 20px;
    }
  }
  .list_wrapper {
    width: 200px;
    position: absolute;
    left: 0;
    bottom: 100%;
    z-index: 10;
  }
}
</style>
