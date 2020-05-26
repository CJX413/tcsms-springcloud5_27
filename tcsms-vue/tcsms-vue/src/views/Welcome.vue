<template>
  <div>
    <router-link :to="{path:'/auth/login'}">登录页</router-link>
    <router-link :to="{path:'/emulator'}">模拟器</router-link>
  </div>
</template>

<script>
  export default {
    name: "Welcome",
    methods: {}
  }
</script>

<style scoped>

</style>
