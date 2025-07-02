<template>
  <div class="FriendsPlazaEdit tpage" :class="{ small: resize.smallRef.value }">
    <div class="content">
      <FormAvater ref="FormAvaterRef" @over="avaterOver" />
      <div class="fromWrapper">
        <FormComp
          ref="FormCompRef"
          :tags="tags"
          @close="formClose"
          @confirm="formConfirm"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useMessage } from "naive-ui";
import { useRoute, useRouter } from "vue-router";
import {
  computed,
  inject,
  onMounted,
  ref,
  watch,
  getCurrentInstance,
  onUnmounted,
  nextTick,
} from "vue";
import { routerBack } from "@/common/utils";
import { useGlobalStore, useChatStore } from "@/store";
import { queryFriend, createFriend, updateFriend } from "@/api/user";
import {
  formData,
  formRef,
  formDataClear,
  defaultFormData,
} from "./compositions/form";

import FormComp from "./comp/FormComp.vue";
import FormAvater from "./comp/FormAvater.vue";

const FormCompRef = ref(null);
const FormAvaterRef = ref(null);
const route = useRoute();
const router = useRouter();
const message = useMessage();
const useChat = useChatStore();
const resize = inject("resize");
const useGlobal = useGlobalStore();
const cxt = getCurrentInstance();
const bus = cxt.appContext.config.globalProperties.$bus;

const friendId = computed(() => route.query.id);
const tags = computed(() => useGlobal.tags);

const type = ref("add"); // add edit
const productType = computed(() => (resize.IDERef.value ? 1 : 0));

let friendData = {};
const getData = async () => {
  friendData = await queryFriend({
    productType: productType.value,
    friendId: friendId.value,
  });
  const {
    userFriendId,
    avatar,
    cssAvatar,
    name,
    friendType,
    tag = tags.value[0].name,
    intro,
    welcome,
    systemPrompt,
    contentPrompt,
    friendSource = defaultFormData.friendSource,
    conversationStart = defaultFormData.conversationStart,
    aliyunDashscopeWorkspaceId,
    aliyunDashscopeAppId,
    aliyunDashscopeApiKey,
    cozeBotId,
    cozeAccessToken,
    variables,
    messageContextSize = defaultFormData.messageContextSize,
    openaiRequestBody = JSON.parse(
      JSON.stringify(defaultFormData.openaiRequestBody)
    ),
  } = friendData;
  formData.value = {
    userFriendId,
    avatar,
    cssAvatar,
    name,
    friendType,
    tag,
    intro,
    welcome,
    systemPrompt,
    contentPrompt,
    friendSource,
    conversationStart,
    aliyunDashscopeWorkspaceId,
    aliyunDashscopeAppId,
    aliyunDashscopeApiKey,
    cozeBotId,
    cozeAccessToken,
    variables,
    messageContextSize: Number(messageContextSize),
    openaiRequestBody,
  };
};

const updateComp = () => {
  FormCompRef.value && FormCompRef.value.update();
  FormAvaterRef.value && FormAvaterRef.value.update();
};

// 数据初始化
const dataInit = () => {
  formDataClear();
  if (resize.IDERef.value && type.value == "add") {
    formData.value.tag = "编程";
  } else if (formData.value) {
    formData.value.tag = tags.value[0].name;
  }
};

const init = async () => {
  if (friendId.value) {
    type.value = "edit";
    dataInit();
    await getData();
  } else {
    type.value = "add";
    dataInit();
  }
  updateComp();
};

watch(
  () => route.query.id,
  () => {
    if (["d_friendsPlazaEdit"].includes(route.name)) {
      init();
    }
  }
);
onMounted(init);
onUnmounted(formDataClear);

const avaterOver = () => {};

const formClose = () => {
  routerBack(router, { name: "dialogue" });
};
const formConfirm = async (clickOver) => {
  const params = {
    ...formData.value,
    productType: productType.value,
  };
  let res =
    type.value === "edit"
      ? await updateFriend({ ...params, id: friendData.userFriendId })
      : await createFriend(params);
  if (res.err) {
    clickOver();
    res.errMsg && message.error(res.errMsg || "操作失败");
    return;
  }
  clickOver();
  dataInit();
  const text = type.value === "edit" ? "编辑成功" : "添加成功";
  message.success(text);
  useChat.addFriends(res); 
  routerBack(router, { name: "dialogue" }); 
  bus.emit("friendFollow", { ...res, __type: type.value });  
};
</script>

<style lang="scss" scoped>
.FriendsPlazaEdit {
  overflow-y: auto;
  height: 100%;
  .content {
    padding: 50px 0;
    .fromWrapper {
      max-width: 700px;
      margin: auto;
    }
  }

  &.small {
    .content {
      padding-bottom: 120px;
      .fromWrapper {
        padding: 0 20px;
      }
    }
  }
}
</style>
