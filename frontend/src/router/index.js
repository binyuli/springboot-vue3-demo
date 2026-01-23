import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

// 路由配置
const routes = [
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  
  // 主布局
  {
    path: '/',
    name: 'Layout',
    component: () => import('../components/Layout.vue'),
    meta: { title: '首页', requiresAuth: true },
    children: [
      // 首页
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      
      // 用户管理
      {
        path: '/user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      

    ]
  },
  
  // 404页
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  
  // 重定向到404
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '系统管理'
  
  // 获取用户状态
  const userStore = useUserStore()
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // 已登录，放行
    if (userStore.isLoggedIn) {
      next()
    } else {
      // 未登录，重定向到登录页
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  } else {
    // 不需要登录，放行
    next()
  }
})

export default router