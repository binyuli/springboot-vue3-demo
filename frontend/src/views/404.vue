<template>
  <div class="error-container">
    <el-card class="error-card" shadow="hover">
      <div class="error-content">
        <el-result
          icon="error"
          title="404"
          sub-title="抱歉，您访问的页面不存在"
        >
          <template #extra>
            <el-button type="primary" @click="goHome">返回首页</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

// 路由实例
const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.error-card {
  width: 600px;
  background-color: white;
}

.error-content {
  text-align: center;
}
</style>