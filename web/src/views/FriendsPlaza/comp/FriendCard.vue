<template>
  <div class="FriendCard">
    <div class="avater">
      <img :src="props.data.avatar" alt="" />
    </div>
    <div class="name">{{ props.data.name }}</div>
    <div
      class="collect"
      @click.stop="collect"
      :style="{ cursor: props.data.followed ? 'auto' : 'pointer' }"
    >
      <span
        class="iconfont"
        :class="props.data.followed ? 'icon-heart-filled' : 'icon-heart'"
      ></span>
    </div>
    <div class="desc">{{ props.data.intro }}</div>
    <div class="tag"><span class="symbol">#</span>{{ props.data.tag }}</div>
    <div class="badge" v-if="props.data.friendType == 2">
      <div class="text">GPTs</div>
    </div>
    <div class="badge" v-if="props.data.friendType == 3">
      <div class="text">百炼应用</div>
    </div>
    <div class="badge" v-if="props.data.friendType == 4 || props.data.friendType == 5">
      <div class="text">扣子应用</div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(["data"]);
const emit = defineEmits(["collect"]);
const collect = () => emit("collect", props.data);
</script>

<style lang="scss" scoped>
.FriendCard {
  padding: 15px;
  height: 185px;

  position: relative;
  /* border-radius: 5px; */
  box-sizing: border-box;
  color: var(--tp_textcolor);
  background-color: var(--tp_FriendCard_bg_color);
  &:hover {
    background-color: var(--tp_FriendCard_hover_bg_color);
  }

  .avater img {
    width: 50px;
    border-radius: 10px;
  }
  .name {
    font-size: 1.2rem;
    color: var(--tp_avatar_color);
    height: 50px;
  }
  .collect {
    position: absolute;
    right: 10px;
    /* top: 10px; */
    bottom: 5px;
    .iconfont.icon-heart-filled {
      color: red;
    }
  }
  .desc {
    height: 2.4rem;
    line-height: 1.2rem;
    color: var(--tp_FriendCard_color);
    display: -webkit-box;
    -webkit-line-clamp: 2; //限制文本的行数，只显示两行
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 5px 0 5px;
  }
  .tag {
    text-align: left;
    color: var(--tp_FriendCard_color);
    .symbol {
      margin-right: 2px;
    }
  }
  .badge {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    overflow: hidden;
    pointer-events: none;
    .text {
      width: 100px;
      text-align: center;
      font-size: 1rem;
      position: absolute;
      right: 0;
      top: 0;
      color: #fff;
      background-color: #007acc;
      transform: translate(30%, 50%) rotate(45deg);
    }
  }
}
</style>
