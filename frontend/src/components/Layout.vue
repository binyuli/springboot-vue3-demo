<template>
  <div class="layout-container" :class="{ 'dark-mode': isDark }">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="layout-aside">
      <div class="aside-header">
        <h1>系统管理</h1>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="layout-menu"
        router
        :collapse="isCollapse"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409eff"
        :unique-opened="true"
      >
        <!-- 首页 -->
        <el-menu-item index="/">
          <template #icon>
            <el-icon><House /></el-icon>
          </template>
          <span>首页</span>
        </el-menu-item>
        
        <!-- 用户管理 -->
        <el-menu-item index="/user">
          <template #icon>
            <el-icon><User /></el-icon>
          </template>
          <span>用户管理</span>
        </el-menu-item>
        

      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 折叠/展开按钮 -->
          <el-button
            type="text"
            @click="toggleCollapse"
            :icon="isCollapse ? 'Expand' : 'Fold'"
            class="collapse-btn"
          ></el-button>
        </div>
        
        <div class="header-right">
          <!-- 暗黑模式切换 -->
          <el-switch
            v-model="isDark"
            active-text="暗黑模式"
            inactive-text="亮色模式"
            @change="toggleDarkMode"
          ></el-switch>
          
          <!-- 下拉菜单 -->
          <el-dropdown>
            <span class="user-avatar">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo?.username || '未登录' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { House, User, Fold, Expand, Document } from '@element-plus/icons-vue'

// 获取路由和用户状态
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 暗黑模式状态
const isDark = ref(localStorage.getItem('isDark') === 'true')

// 激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 切换暗黑模式
const toggleDarkMode = () => {
  localStorage.setItem('isDark', isDark.value)
  // 切换 Element Plus 主题
  const html = document.documentElement
  if (isDark.value) {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 初始化暗黑模式
onMounted(() => {
  toggleDarkMode()
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background-color: #f5f7fa;
  transition: all 0.3s ease;
}

/* 暗黑模式样式 */
.layout-container.dark-mode {
  background-color: #1a1a1a;
  color: #fff;
}

.layout-aside {
  background-color: #001529;
  color: #fff;
  border-right: 1px solid #e6e6e6;
  transition: all 0.3s ease;
}

.layout-container.dark-mode .layout-aside {
  border-right-color: #333;
}

.aside-header {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  border-bottom: 1px solid #1f2d3d;
}

.aside-header h1 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.layout-menu {
  height: calc(100vh - 60px);
  border-right: none;
}

.layout-container.dark-mode .layout-menu {
  background-color: #1f2d3d;
}

.layout-container {
  display: flex;
}

.layout-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.layout-container.dark-mode .layout-main {
  background-color: #1a1a1a;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.layout-container.dark-mode .layout-header {
  background-color: #1f2d3d;
  border-bottom-color: #333;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  margin-right: 20px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  transition: all 0.3s ease;
}

.layout-container.dark-mode .user-avatar {
  color: #fff;
}
</style>

<style>
/* 全局样式，用于暗黑模式 */
html.dark {
  --el-bg-color: #1a1a1a;
  --el-text-color-primary: #fff;
  --el-text-color-regular: #e0e0e0;
  --el-text-color-secondary: #c0c4cc;
  --el-border-color-light: #333;
  --el-border-color-lighter: #444;
  --el-fill-color-light: #2a2a2a;
  --el-fill-color-lighter: #333;
  --el-fill-color-blank: #1a1a1a;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 暗黑模式滚动条 */
html.dark ::-webkit-scrollbar-track {
  background: #2a2a2a;
}

html.dark ::-webkit-scrollbar-thumb {
  background: #555;
}

html.dark ::-webkit-scrollbar-thumb:hover {
  background: #666;
}
</style>